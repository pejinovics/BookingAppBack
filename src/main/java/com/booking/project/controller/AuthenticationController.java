package com.booking.project.controller;

import com.booking.project.dto.UserCredentialsDTO;
import com.booking.project.model.User;
import com.booking.project.config.security.jwt.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//@RestController
@CrossOrigin(origins = "http://localhost:4200")
@Validated
//@RequestMapping("/api/auth")
public class AuthenticationController {

//    @Autowired
    private AuthenticationManager authenticationManager;

//    @Autowired
    private JwtTokenUtil jwtTokenUtil;

//    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping(value = "/login")
    public User login(@Valid @RequestBody UserCredentialsDTO userCredentialsDTO) {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(userCredentialsDTO.getEmail(),
                userCredentialsDTO.getPassword());
        Authentication auth = authenticationManager.authenticate(authReq);

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        String token = jwtTokenUtil.generateToken(userCredentialsDTO.getEmail(), userDetailsService.loadUserByUsername(userCredentialsDTO.getEmail()));
        User user = new User(userCredentialsDTO);

        user.setJwt(token);
        return user;
    }


    @GetMapping(value = "/logout", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity logoutUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof AnonymousAuthenticationToken)){
            SecurityContextHolder.clearContext();

            return new ResponseEntity<>("You successfully logged out!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User is not authenticated!", HttpStatus.UNAUTHORIZED);
        }

    }

}
