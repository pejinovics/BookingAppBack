package com.booking.project.controller;

import com.booking.project.dto.AccommodationApprovalStatusDTO;
import com.booking.project.dto.AccommodationCardDTO;
import com.booking.project.dto.AccommodationDTO;
import com.booking.project.dto.IntervalPriceDTO;
import com.booking.project.model.Accommodation;
import com.booking.project.model.Host;
import com.booking.project.model.User;
import com.booking.project.model.enums.AccommodationApprovalStatus;
import com.booking.project.model.enums.AccommodationType;
import com.booking.project.model.enums.Amenities;
import com.booking.project.model.enums.ReservationMethod;
import com.booking.project.service.interfaces.IAccommodationService;
import com.booking.project.service.interfaces.IGuestService;
import com.booking.project.service.interfaces.IHostService;
import com.booking.project.service.interfaces.IPriceListService;
import com.booking.project.validation.IdentityConstraint;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/accommodations")
public class AccommodationController {

    @Autowired
    private IAccommodationService accommodationService;
    @Autowired
    private IHostService hostService;
    @Autowired
    private IPriceListService priceListService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccommodations(){
        Collection<AccommodationDTO> accommodations = accommodationService.findAll();
        return new ResponseEntity<Collection<AccommodationDTO>>(accommodations, HttpStatus.OK);
    }
    @GetMapping(value = "/cards",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccommodationsCards() throws IOException{
        Collection<AccommodationCardDTO> accommodations = accommodationService.findAllCards();
        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodations, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccommodationDetails(@IdentityConstraint @PathVariable("id") Long id) throws IOException {
        AccommodationDTO accommodationDTO = accommodationService.findAccommodationsDetails(id);
        if(accommodationDTO == null)    return new ResponseEntity<AccommodationDTO>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<AccommodationDTO>(accommodationDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN_ACCOMMODATION_STATUS_UPDATE')")
    @GetMapping(value = "/adminApproving", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccommodationsForApproving() throws IOException {
        Collection<AccommodationCardDTO> accommodationCards = accommodationService.findApprovalPendingAccommodations();
        if(accommodationCards == null)    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodationCards, HttpStatus.OK);
    }

    @GetMapping(value = "/popular", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPopularAccommodations() throws IOException {
        Collection<AccommodationCardDTO> accommodationCards = accommodationService.findPopularAccommodations();
        if(accommodationCards == null)    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodationCards, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOST_ACCOMMODATION_WRITE')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAccommodation(@RequestBody AccommodationDTO accommodationDTO) throws Exception {
        Accommodation accommodationNew = new Accommodation(accommodationDTO);
        Host host = hostService.findByUser(accommodationDTO.getHostId());
        accommodationNew.setHost(host);
        Accommodation savedAccommodation = accommodationService.save(accommodationNew);
        AccommodationDTO newAccommodationDTO = new AccommodationDTO(savedAccommodation);
        return new ResponseEntity<AccommodationDTO>(newAccommodationDTO, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('HOST_ACCOMMODATION_DELETE')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Accommodation> deleteAccommodation(@IdentityConstraint @PathVariable("id") Long id){
        accommodationService.deleteById(id);
        return new ResponseEntity<Accommodation>(HttpStatus.NO_CONTENT);
    }
    @PreAuthorize("hasRole('ADMIN_ACCOMMODATION_STATUS_UPDATE')")
    @PutMapping(value ="/{id}/approvalStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeAccommodationAvailableStatus(@IdentityConstraint @PathVariable Long id,
                                                                @RequestBody AccommodationApprovalStatusDTO approvalStatus) throws Exception {
        AccommodationDTO accommodationDTO = accommodationService.changeAvailableStatus(id, approvalStatus.getApprovalStatus());

        if(accommodationDTO == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<AccommodationDTO>(accommodationDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('GUEST_CALENDAR_READ')")
    @GetMapping(value = "/{id}/availableDates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAvailableDates(@IdentityConstraint @PathVariable("id") Long id) throws IOException {
        List<LocalDate> availableDates = accommodationService.getAvailableDates(id);

        return new ResponseEntity<List<LocalDate>>(availableDates, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('HOST_ACCOMMODATION_READ')")
    @GetMapping(value = "/host/{id_host}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHostAccommodations(@IdentityConstraint @PathVariable("id_host") Long id) throws IOException {
        Collection<AccommodationCardDTO> accommodations = accommodationService.findAccomodationsByHostId(id);
        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodations, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_ACCOMMODATION_UPDATE')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAccommodation(@RequestBody AccommodationDTO accommodationDTO,
                                                 @IdentityConstraint @PathVariable Long id) throws Exception{
        Optional<AccommodationDTO> accommodation = accommodationService.changeAccommodations(accommodationDTO,id);

        if (accommodation.get() == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<AccommodationDTO>(accommodation.get(), HttpStatus.CREATED);
    }
//    @PreAuthorize("hasRole('HOST') OR hasRole('GUEST')")
    @GetMapping(value = "/cards/filter",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterAccommodations(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer numberOfGuests,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
            @RequestParam(required = false) Integer startPrice,
            @RequestParam(required = false) Integer endPrice,
            @RequestParam(required = false) Collection<Amenities> amenities,
            @RequestParam(required = false) AccommodationType accommodationType
    ) throws IOException {
        Collection<AccommodationCardDTO> accommodations = accommodationService.filterAccommodations(startDate,endDate,numberOfGuests,city,startPrice,endPrice,amenities,accommodationType);

        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodations, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_RESERVATION_UPDATE')")
    @PutMapping(value ="/{id}/reservationMethod/{reservationMethod}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeAccommodationReservationMethod(@IdentityConstraint @PathVariable Long id, @PathVariable ReservationMethod reservationMethod) throws Exception {
        AccommodationDTO accommodationDTO = accommodationService.changeAccommodationReservationMethod(id,reservationMethod);

        if(accommodationDTO == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<AccommodationDTO>(accommodationDTO, HttpStatus.OK);
    }
    @GetMapping(value="/minMaxPrice",produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<?> getMinMaxPrices(){
        Collection<Double> minMax = accommodationService.getMinMaxPrice();
        return new ResponseEntity<Collection<Double>>(minMax, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_ACCOMMODATION_PRICE_LIST_WRITE')")
    @PostMapping(value = "/priceList/{accommodationId}", consumes = "application/json")

    public ResponseEntity<Integer> addPriceList(@IdentityConstraint @PathVariable Long accommodationId,
                                                @RequestBody List<IntervalPriceDTO> dtos) throws Exception {

        Optional<Accommodation> accommodation = accommodationService.findById(accommodationId);
        if(accommodation.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        accommodation.get().setPrices(priceListService.getPriceList(dtos));
        accommodationService.save(accommodation.get());

        return new ResponseEntity<>(accommodation.get().getPrices().size(), HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_ACCOMMODATION_PRICE_LIST_UPDATE')")
    @PutMapping(value = "/priceList/{accommodationId}", consumes = "application/json")
    public ResponseEntity<Integer> updatePriceList(@IdentityConstraint @PathVariable Long accommodationId,
                                                   @RequestBody List<IntervalPriceDTO> dtos) {

        int length = 0;
        try {
            length = priceListService.updatePriceList(accommodationId, dtos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<>(length, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_ACCOMMODATION_PRICE_LIST_READ')")
    @GetMapping(value = "priceList/{id}")
    public ResponseEntity<?> getPriceList(@IdentityConstraint @PathVariable Long id) {

        Accommodation accommodation = accommodationService.findById(id).get();
        return new ResponseEntity<>(accommodation.getPrices(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOST_ACCOMMODATION_PRICE_LIST_READ')")
    @GetMapping(value = "intervalPrices/{id}")
    public ResponseEntity<List<IntervalPriceDTO>> getIntervalPrices(@IdentityConstraint @PathVariable Long id) {
        Optional<Accommodation> accommodation = accommodationService.findById(id);
        if(accommodation.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(priceListService.getIntervalPrices(id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('GUEST_ACCOMMODATION_READ')")
    @GetMapping(value = "/guest/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGuestAccommodations(@IdentityConstraint @PathVariable("id") Long id){
        List<AccommodationDTO> accommodationDTOS = accommodationService.getGuestAccommodations(id);
        if(accommodationDTOS == null)    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(accommodationDTOS, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('GUEST_ACCOMMODATION_READ')")
    @GetMapping(value = "/guest/comment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGuestAccommodationsForComment(@IdentityConstraint @PathVariable("id") Long id) throws IOException {
        List<AccommodationDTO> accommodationDTOS = accommodationService.getGuestAccommodationsForComment(id);
        if(accommodationDTOS == null)    return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(accommodationDTOS, HttpStatus.OK);
    }
}
