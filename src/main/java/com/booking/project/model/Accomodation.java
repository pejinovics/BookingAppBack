package com.booking.project.model;

import com.booking.project.model.enums.AccomodationType;
import com.booking.project.model.enums.Amenities;
import com.booking.project.model.enums.CancellationPolicy;
import com.booking.project.model.enums.ReservationMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Accomodation {
    private Long id;
    private String title;
    private String description;
    private Address address;
    private Point location;
    private List<Amenities> amenities;
    private Long minGuests;
    private Long maxGuests;
    private AccomodationType type;
    private CancellationPolicy cancellationPolicy;
    private boolean isAvailableForReservation;
    private ReservationMethod reservationMethod;
    private Host host;
    private boolean priceForEntireAcc;
    private List<PriceList> prices;


    public void copyValues(Accomodation accomodation){
        this.title = accomodation.title;
        this.description = accomodation.description;
        this.minGuests = accomodation.minGuests;
        this.maxGuests = accomodation.maxGuests;
    }
}