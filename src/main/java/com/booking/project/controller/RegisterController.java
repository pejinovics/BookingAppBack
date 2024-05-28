package com.booking.project.controller;

import com.booking.project.dto.UserInfoDTO;
import com.booking.project.exception.ResourceConflictException;
import com.booking.project.model.Guest;
import com.booking.project.model.Host;
import com.booking.project.model.User;
import com.booking.project.model.enums.UserType;
import com.booking.project.service.GuestService;
import com.booking.project.service.HostService;
import com.booking.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/api/register")
@Validated
public class RegisterController {

    @Autowired
    private UserService userService;
    @Autowired
    private GuestService guestService;
    @Autowired
    private HostService hostService;

    @PostMapping()
    public ResponseEntity<User> addUser(@Valid  @RequestBody UserInfoDTO userInfoDTO) throws Exception {
        User savedUser = userService.registerUser(userInfoDTO);

        if(savedUser == null){
            throw new ResourceConflictException(userInfoDTO.getId(),"Username already exists");
        }
        if(savedUser.getUserType().equals(UserType.GUEST)){
            Guest guest = new Guest(userInfoDTO);
            guest.setUser(savedUser);
            guest.setId(savedUser.getId());
            guestService.save(guest);

        }else if(savedUser.getUserType().equals(UserType.HOST)){
            Host host = new Host(userInfoDTO);
            host.setUser(savedUser);
            host.setId(savedUser.getId());
            hostService.save(host);
        }



        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
    @GetMapping(path = "/userExists/{email}")
    public Boolean confirm(@PathVariable("email") String email){

        Optional<User> user = userService.findByEmail(email);
        if (!user.isEmpty()) {
            return true;
        }else {
            return false;
        }
    }
//    @GetMapping(path = "confirm")
//    public String confirm(@RequestParam("token") String token){
//        return userService.confirmToken(token);
//    }
}
