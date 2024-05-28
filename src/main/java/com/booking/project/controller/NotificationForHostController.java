package com.booking.project.controller;

import com.booking.project.dto.CreateNotificationForHostDTO;
import com.booking.project.dto.NotificationForHostDTO;
import com.booking.project.dto.NotificationTypeStatusDTO;
import com.booking.project.model.NotificationForHost;
import com.booking.project.model.NotificationTypeStatus;
import com.booking.project.service.NotificationForHostService;
import com.booking.project.service.interfaces.INotificationTypeStatusService;
import com.booking.project.validation.IdentityConstraint;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@Validated
@RequestMapping("/api/notificationsForHost")
public class NotificationForHostController {

    @Autowired
    private NotificationForHostService notificationForHostService;
    @Autowired
    private INotificationTypeStatusService notificationTypeStatusService;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationForHost> createNotificationForHost
            (@Valid @RequestBody CreateNotificationForHostDTO createNotificationForHostDTO) throws Exception {
        NotificationForHost savedNotificationForHost = notificationForHostService.create(createNotificationForHostDTO);
        return new ResponseEntity<NotificationForHost>(savedNotificationForHost, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('HOST_NOTIFICATION_READ')")
    @GetMapping(value = "/host/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<NotificationForHostDTO>> getNotificationsForHost(@IdentityConstraint  @PathVariable Long id){
        Collection<NotificationForHostDTO> notificationForHost = notificationForHostService.findByHost(id);
        return new ResponseEntity<Collection<NotificationForHostDTO>>(notificationForHost, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_NOTIFICATION_UPDATE')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> markNotificationAsRead(@IdentityConstraint @PathVariable("id") Long id){
        NotificationForHostDTO notificationForHostDTO = notificationForHostService.markAsRead(id);
        return new ResponseEntity<NotificationForHostDTO>(notificationForHostDTO,HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_NOTIFICATION_STATUS_READ')")
    @GetMapping(value="/hostNotificationStatus/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHostNotificationsStatus(@IdentityConstraint @PathVariable Long id){
        Collection<NotificationTypeStatus> notificationsTypeStatus = notificationTypeStatusService.findByUser(id);
        return new ResponseEntity<Collection<NotificationTypeStatus>>(notificationsTypeStatus, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_NOTIFICATION_STATUS_UPDATE')")
    @PutMapping(value="/changeNotificationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeHostNotificationStatus(@Valid @RequestBody NotificationTypeStatusDTO notificationTypeStatusDTO){
        notificationTypeStatusService.changeNotificationStatus(notificationTypeStatusDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
