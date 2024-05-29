package com.booking.project.controller;

import com.booking.project.dto.*;
import com.booking.project.model.*;
import com.booking.project.model.enums.UserStatus;
import com.booking.project.model.enums.UserType;
import com.booking.project.service.interfaces.IGuestService;
import com.booking.project.service.interfaces.IHostService;
import com.booking.project.service.interfaces.IUserReportService;
import com.booking.project.service.interfaces.IUserService;
import com.booking.project.validation.IdentityConstraint;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGuestService guestService;

    @Autowired
    private IHostService hostService;

    @Autowired
    private IUserReportService userReportService;
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsers(){
        Collection<User> users = userService.findAll();
        return new ResponseEntity<Collection<User>>(users, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUser(@IdentityConstraint @PathVariable("id") Long id){
        Optional<User> user = userService.findById(id);

        if(user.isEmpty()){
            return new ResponseEntity<Optional<User>>(HttpStatus.NOT_FOUND);
        }
        if(user.get().getUserType().equals(UserType.GUEST)){

            Optional<Guest> guest =  guestService.findByUser(user.get().getId());
            UserInfoDTO userInfoDTO = new UserInfoDTO(guest.get(),user.get());
            return new ResponseEntity<UserInfoDTO>(userInfoDTO, HttpStatus.OK);

        }else if(user.get().getUserType().equals(UserType.HOST)){

            Host host = hostService.findByUser(user.get().getId());
            UserInfoDTO userInfoDTO = new UserInfoDTO(host,user.get());
            return new ResponseEntity<UserInfoDTO>(userInfoDTO, HttpStatus.OK);
        }

        return new ResponseEntity<Optional<User>>(user, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_USER_DELETE') OR hasRole('GUEST_USER_DELETE')")
    @PutMapping (value = "/delete/{id}")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody UserDeleteDTO userDeleteDTO,
                                        @IdentityConstraint @PathVariable("id") Long id) throws Exception {
        Boolean isUserDeleted = userService.deleteUserById(userDeleteDTO,id);

        if(isUserDeleted.equals(false)){
            return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN_USER_UPDATE')")
    @PutMapping(value = "/admin/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAdmin(@Valid @RequestBody UserAdminUpdateDTO userAdminDTO,
                                         @IdentityConstraint @PathVariable Long id) throws Exception{
        User userForUpdate = userService.updateAdmin(userAdminDTO, id);

        if(userForUpdate == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<User>(userForUpdate, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('GUEST_USER_UPDATE') OR hasRole('HOST_USER_UPDATE')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO,
                                        @IdentityConstraint @PathVariable Long id) throws Exception{
        User userForUpdate = userService.update(userUpdateDTO, id);

        if(userForUpdate == null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<User>(userForUpdate, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('HOST_USER_STATUS_UPDATE')")
    @PutMapping(value = "/host/{id}/userStatus/{status}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeHostStatus(@PathVariable Long id,@PathVariable UserStatus status) throws Exception {
        UserDTO userDTO = userService.changeStatus(id,status, UserType.HOST);

        if(userDTO == null) return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('GUEST_USER_STATUS_UPDATE')")
    @PutMapping(value = "/guest/{id}/userStatus/{status}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeGuestStatus(@PathVariable Long id,@PathVariable UserStatus status) throws Exception {
        UserDTO userDTO = userService.changeStatus(id,status,UserType.GUEST);

        if(userDTO == null) return new ResponseEntity<UserDTO>(userDTO,HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN_BLOCK_USER')")
    @PutMapping(value = "/block/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> blockUser(@IdentityConstraint @PathVariable Long id) throws Exception {
        UserDTO userDTO = userReportService.blockUser(id);

        if(userDTO == null) return new ResponseEntity<UserDTO>(userDTO,HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('GUEST_REPORT_USER_WRITE') OR hasRole('HOST_REPORT_USER_WRITE')")
    @PutMapping(value = "/report/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reportUser(@IdentityConstraint @PathVariable Long id) throws Exception {
        UserDTO userDTO = userService.report(id);

        if(userDTO == null) return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN_REPORT_USER')")
    @GetMapping(value = "/reported",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getReportedUsers() throws Exception{
        Collection<UserBlockDTO> reportedUsersDTOs = userReportService.findAll();

        return new ResponseEntity<Collection<UserBlockDTO>>(reportedUsersDTOs, HttpStatus.OK);
    }

    @GetMapping(value = "/host/{user_id}")
    public ResponseEntity<?> getHostId(@IdentityConstraint @PathVariable Long user_id) throws Exception{
        Host host = hostService.findByUser(user_id);

        if(host == null)  return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<Integer>(Math.toIntExact(host.getId()), HttpStatus.OK);
    }
    @PostMapping(value ="/report",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserReportDTO> createUserReport
            (@Valid @RequestBody UserReportDTO userReportDTO) throws Exception {
        UserReportDTO userReportSaved = userReportService.create(userReportDTO);
        if(userReportSaved == null){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<UserReportDTO>(userReportSaved, HttpStatus.CREATED);
    }
 }
