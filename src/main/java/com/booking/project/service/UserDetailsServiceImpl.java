package com.booking.project.service;
import com.booking.project.model.User;
import com.booking.project.repository.inteface.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

//@Service
public class UserDetailsServiceImpl implements UserDetailsService{

//    @Autowired
    private IUserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> ret = userRepository.findByEmail(email);
        if (!ret.isEmpty() ) {
            return org.springframework.security.core.userdetails.User.withUsername(email).password(ret.get().getPassword()).roles(ret.get().getUserType().toString()).disabled(ret.get().isDisabled()).build();
        }
        throw new UsernameNotFoundException("User not found with this username: " + email);
    }
}
