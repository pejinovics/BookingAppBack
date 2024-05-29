package com.booking.project.controller;

import com.booking.project.dto.AccommodationCardDTO;
import com.booking.project.model.Guest;
import com.booking.project.model.User;
import com.booking.project.service.interfaces.IGuestService;
import com.booking.project.validation.IdentityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    @Autowired
    private IGuestService guestService;
    @PreAuthorize("hasRole('GUEST_FAVORITE_ACCOMMODATION_READ')")
    @GetMapping(value = "/{id}/favourites", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<AccommodationCardDTO>> getFavouritesAccommodations(@IdentityConstraint @PathVariable("id") Long id)
            throws IOException {
        Collection<AccommodationCardDTO> accommodationCardDTOS = guestService.findFavourites(id);

        return new ResponseEntity<Collection<AccommodationCardDTO>>(accommodationCardDTOS, HttpStatus.OK);
    }


    @PreAuthorize(("hasRole('GUEST_FAVORITE_ACCOMMODATION_WRITE')"))
    @PostMapping(value = "/favourites/{guestUserId}/{accId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addFavourite(@IdentityConstraint  @PathVariable("guestUserId") Long guestUserId,
                                          @IdentityConstraint @PathVariable("accId") Long accId) throws Exception {
        boolean result = guestService.addFavourite(accId, guestUserId);

        if (!result) return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @PreAuthorize(("hasRole('GUEST_FAVORITE_READ')"))
    @GetMapping(value = "/isFavourite/{guestUserId}/{accId}")
    public ResponseEntity<?> isFavourite(@IdentityConstraint @PathVariable("guestUserId") Long guestUserId,
                                         @IdentityConstraint @PathVariable("accId") Long accId) throws Exception {
        boolean result = guestService.isFavourite(accId, guestUserId);

        if (!result) return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @PreAuthorize(("hasRole('GUEST_FAVORITE_ACCOMMODATION_DELETE')"))
    @DeleteMapping(value = "/favourites/{guestUserId}/{accId}")
    public ResponseEntity<?> removeFavourite(@IdentityConstraint @PathVariable("guestUserId") Long guestUserId,
                                             @IdentityConstraint @PathVariable("accId") Long accId) throws Exception {
        boolean result = guestService.removeFavourite(accId, guestUserId);

        if (!result) return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
//    @PreAuthorize(("hasRole('GUEST')"))
    @GetMapping(value = "/favourites/{id}")
    public ResponseEntity<?> getAllAccommodations(@PathVariable("id") Long id) throws IOException {
        Collection<AccommodationCardDTO> allAccommodations = guestService.findAllAccommodationsWithFavourites(id);

        return new ResponseEntity<Collection<AccommodationCardDTO>>(allAccommodations,HttpStatus.OK);
    }
}
