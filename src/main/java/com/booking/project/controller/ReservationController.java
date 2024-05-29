package com.booking.project.controller;

import com.booking.project.dto.CreateReservationDTO;
import com.booking.project.dto.ReservationDTO;
import com.booking.project.model.Accommodation;
import com.booking.project.model.Guest;
import com.booking.project.model.Reservation;
import com.booking.project.model.enums.AccommodationStatus;
import com.booking.project.model.enums.ReservationMethod;
import com.booking.project.model.enums.ReservationStatus;
import com.booking.project.service.interfaces.IAccommodationService;
import com.booking.project.service.interfaces.IGuestService;
import com.booking.project.service.interfaces.IReservationService;
import com.booking.project.validation.IdentityConstraint;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private IReservationService reservationService;

    @Autowired
    private IAccommodationService accommodationService;

    @Autowired
    private IGuestService guestService;

    @PreAuthorize("hasRole('GUEST_RESERVATION_READ')")
    @GetMapping(value = "/filterGuest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterGuestReservations(@PathParam("title") String title,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  @PathParam("startDate") LocalDate startDate,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathParam("endDate") LocalDate endDate,
                                                     @PathParam("status") ReservationStatus status,
                                                     @IdentityConstraint  @PathParam("guestId") Long guestId){
        List<ReservationDTO> reservations = reservationService.filterGuestReservations(title, startDate, endDate, status, guestId);

        return new ResponseEntity<Collection<ReservationDTO>>(reservations, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOST_RESERVATION_READ')")
    @GetMapping(value = "/filterHost", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterHostReservations(@PathParam("title") String title,
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathParam("startDate") LocalDate startDate,
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathParam("endDate") LocalDate endDate,
                                                    @PathParam("status") ReservationStatus status,
                                                    @IdentityConstraint @PathParam("hostId") Long hostId){
        List<ReservationDTO> reservations = reservationService.filterHostReservations(title, startDate, endDate, status, hostId);

        return new ResponseEntity<Collection<ReservationDTO>>(reservations, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('GUEST_RESERVATION_WRITE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReservation(@Valid  @RequestBody CreateReservationDTO createReservationDTO) throws Exception {
        List<Object> reservationResponse = accommodationService.reservate(createReservationDTO.getAccommodationId(), createReservationDTO.getStartDate(), createReservationDTO.getEndDate(), createReservationDTO.getNumberOfGuests());

        if(reservationResponse.size() == 1) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        double price = (double) reservationResponse.get(1);
        ReservationMethod reservationMethod = (ReservationMethod) reservationResponse.get(2);
        Reservation createdReservation = reservationService.create(createReservationDTO, price, reservationMethod);
        return new ResponseEntity<ReservationMethod>(reservationMethod, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('GUEST_RESERVATION_PRICE_READ')")
    @PostMapping(value = "/reservationPrice",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getReservationPrice(@Valid @RequestBody CreateReservationDTO createReservationDTO ) {
        List<Object> reservationPrice = accommodationService.calculateReservationPrice(createReservationDTO.getStartDate(), createReservationDTO.getEndDate(), createReservationDTO.getAccommodationId(), createReservationDTO.getNumberOfGuests());

        if(reservationPrice.size() == 1) return new ResponseEntity<Double>(Double.valueOf(0), HttpStatus.OK );

        return new ResponseEntity<Double>((Double) reservationPrice.get(1), HttpStatus.OK );
    }

    @PreAuthorize("hasRole('HOST_RESERVATION_STATUS_UPDATE')")
    @PutMapping(value = "/{id}/{reservationStatus}",  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateReservationStatus(@PathVariable Long id, @PathVariable ReservationStatus reservationStatus) throws Exception{
        Reservation reservation = reservationService.updateStatus(id, reservationStatus);
        if (reservation == null){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<ReservationDTO>(new ReservationDTO(reservation), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('GUEST_RESERVATION_PENDING_DELETE')")
    @DeleteMapping(value = "/pending/{id}")
    public ResponseEntity<?> deletePendingReservation(@IdentityConstraint @PathVariable("id") Long id){
        Boolean isDeleted = reservationService.deleteById(id);

        if(isDeleted.equals(false)){
            return new ResponseEntity<Boolean>(false, HttpStatus.ALREADY_REPORTED);
        }

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('GUEST_RESERVATION_ACCEPT_DELETE')")
    @PutMapping(value = "/cancelAccepted/{id}")
    public ResponseEntity<?> cancelAcceptedReservation(@IdentityConstraint @PathVariable("id") Long id) throws Exception {
        Reservation reservation = reservationService.cancelAcceptedReservation(id);

        if(reservation == null){
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }

        Guest guest = reservation.getGuest();
        guestService.addNumberOfCancellation(guest.getId());

        accommodationService.changePriceList(reservation.getStartDate(), reservation.getEndDate(), reservation.getAccommodation().getId(), AccommodationStatus.AVAILABLE);

        return new ResponseEntity<>(new ReservationDTO(reservation), HttpStatus.OK);
    }




}
