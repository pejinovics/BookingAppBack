package com.booking.project.controller;

import com.booking.project.model.Analytics;
import com.booking.project.model.AnnualAnalytics;
import com.booking.project.service.interfaces.IAnalyticsService;
import com.booking.project.validation.IdentityConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Controller
@Validated
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private IAnalyticsService analyticsService;

    @PreAuthorize("hasRole('HOST_ANALYTICS_READ')")
    @GetMapping(value = "/annualAnalytics/{year}/{accommodationId}/{hostUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAnnualAnalytics(@PathVariable int year, @IdentityConstraint @PathVariable Long accommodationId, @IdentityConstraint @PathVariable Long hostUserId){
        AnnualAnalytics annualAnalytics = analyticsService.getAnnualAnalytics(year, accommodationId, hostUserId);

        return new ResponseEntity<AnnualAnalytics>(annualAnalytics, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOST_ANALYTICS_READ')")
    @GetMapping(value = "/allAccommodations/{hostUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAnalyticsForAll(@NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  @PathParam("startDate") LocalDate startDate,
                                                @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathParam("endDate") LocalDate endDate,
                                                @IdentityConstraint @PathVariable Long hostUserId){
        List<Analytics> analytics = analyticsService.getAnalyticsForAll(startDate, endDate, hostUserId);

        return new ResponseEntity<Collection<Analytics>>(analytics, HttpStatus.OK);
    }
}
