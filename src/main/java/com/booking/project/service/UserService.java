package com.booking.project.service;

import com.booking.project.dto.*;
import com.booking.project.model.*;
import com.booking.project.service.interfaces.*;
import com.booking.project.utils.email.EmailBuilder;
import com.booking.project.utils.email.IEmailSender;
import com.booking.project.model.enums.UserStatus;
import com.booking.project.model.enums.UserType;
import com.booking.project.repository.inteface.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements IUserService {
    @Autowired
    private IUserRepository repository;
    @Autowired
    private IEmailSender emailSender;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private IConfirmationTokenService confirmationTokenService;
    @Autowired
    private IGuestService guestService;
    @Autowired
    private IHostService hostService;
    @Autowired
    private IReservationService reservationService;
    @Autowired
    private INotificationTypeStatusService notificationTypeStatusService;
    private EmailBuilder emailBuilder = new EmailBuilder();
    @Override
    public Collection<User> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }
    @Override
    public Optional<User> findByEmail(String email){
        return repository.findByEmail(email);
    }
    public User registerUser(UserInfoDTO userInfoDTO){
//        Optional<User> userExist = repository.findByEmail(userInfoDTO.getEmail());


//        if(!userExist.isEmpty()){
//            ConfirmationToken token = confirmationTokenService.findTokenByUser(userExist.get().getId());
//
//            if(token.getExpiresAt().isAfter(LocalDateTime.now())) return null;
//
//            confirmationTokenService.deleteById(token.getId());
//
//            if(userExist.get().getUserType().equals(UserType.GUEST)){
//                Optional<Guest> guest = guestService.findByUser(userExist.get().getId());
//                guestService.deleteById(guest.get().getId());
//            }else if(userExist.get().getUserType().equals(UserType.HOST)){
//                Host host = hostService.findByUser(userExist.get().getId());
//                hostService.deleteById(host.getId());
//            }
//            repository.deleteById(userExist.get().getId());
//        }

        String passwordEncoded = bCryptPasswordEncoder.encode(userInfoDTO.getPassword());
        User user = new User(userInfoDTO);
        user.setPassword(passwordEncoded);
        user.setStatus(UserStatus.ACTIVE);
        repository.save(user);
//
//        String token = UUID.randomUUID().toString();
//        ConfirmationToken confirmationToken = new ConfirmationToken(token,LocalDateTime.now(),LocalDateTime.now().plusHours(24),user);
//        confirmationTokenService.saveConfirmationToken(confirmationToken);

//        String link = "http://localhost:8080/api/register/confirm?token=" + token;

//        emailSender.send(user.getEmail(),emailBuilder.buildEmail(userInfoDTO.getName(),link));

        return user;
    }
    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);

        User user =  confirmationToken.getUser();
        repository.enableAppUser(user.getEmail());

        if(user.getUserType().equals(UserType.GUEST)){
            notificationTypeStatusService.initializeGuestNotificationStatus(user);
        }else if(user.getUserType().equals(UserType.HOST)){
            notificationTypeStatusService.initializeHostNotificationStatus(user);
        }
        return "confirmed";
    }
    @Override
    public User save(User user) throws Exception {
        return repository.save(user);
    }

    @Override
    public User updateAdmin(UserAdminUpdateDTO userAdminDTO, Long id) throws Exception{
        Optional<User> userForUpdate = findById(id);

        if(userForUpdate.isEmpty()) return null;
        if(!BCrypt.checkpw(userAdminDTO.getOldPassword(), userForUpdate.get().getPassword())) return null;

        String encodedPassword = bCryptPasswordEncoder.encode(userAdminDTO.getNewPassword());
        userForUpdate.get().setPassword(encodedPassword);

        save(userForUpdate.get());
        return userForUpdate.get();
    }

    @Override
    public User update(UserUpdateDTO userUpdateDTO, Long id) throws Exception {
        Optional<User> user = findById(id);
        if(!BCrypt.checkpw(userUpdateDTO.getOldPassword(), user.get().getPassword())) return null;
        if(user.isEmpty()) return null;
        if(!userUpdateDTO.getNewPassword().equals("")){
            String encodedPassword = bCryptPasswordEncoder.encode(userUpdateDTO.getNewPassword());
            user.get().setPassword(encodedPassword);
            save(user.get());
        }

        if(user.get().getUserType().equals(UserType.GUEST)){
            Optional<Guest> guest = guestService.findByUser(id);
            guest.get().setName(userUpdateDTO.getName());
            guest.get().setLastName(userUpdateDTO.getLastname());
            guest.get().setAddress(userUpdateDTO.getAddress());
            guest.get().setPhoneNumber(userUpdateDTO.getPhoneNumber());
            guestService.save(guest.get());

        }else if(user.get().getUserType().equals(UserType.HOST)){
            Host host = hostService.findByUser(id);
            host.setName(userUpdateDTO.getName());
            host.setLastName(userUpdateDTO.getLastname());
            host.setAddress(userUpdateDTO.getAddress());
            host.setPhoneNumber(userUpdateDTO.getPhoneNumber());
            hostService.save(host);
        }
        return user.get();

    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    @Override
    public boolean deleteUserById(UserDeleteDTO userDeleteDTO,Long id) throws Exception {
        Optional<User> user = findById(id);

        if(user.isEmpty()) return false;
        try {
            if (!BCrypt.checkpw(userDeleteDTO.getPassword(), user.get().getPassword())) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }

        if(user.get().getUserType().equals(UserType.GUEST)){
            Collection<Reservation> guestReservations = reservationService.findByGuestId(guestService.findByUser(id).get().getId());
            if(guestReservations.size() != 0) return false;

        }else if(user.get().getUserType().equals(UserType.HOST)){
            Collection<Reservation> hostReservations = reservationService.findByHostId(hostService.findByUser(id).getId());
            if(hostReservations.size() != 0) return false;
        }

        user.get().setStatus(UserStatus.DELETED);
        save(user.get());

        return true;
    }
    @Override
    public UserDTO changeStatus(Long id, UserStatus status, UserType userType) throws Exception {
        Optional<User> user = findById(id);

        if(user.isEmpty()) return null;
        if(!user.get().getUserType().equals(userType)) return null;

        user.get().setStatus(status);
        save(user.get());
        UserDTO userDTO = new UserDTO(user.get());
        return userDTO;
    }

    @Override
    public UserDTO report(Long id) throws Exception {
        Optional<User> user = findById(id);

        if(user.isEmpty()) return null;

        user.get().setReported(true);
        save(user.get());
        UserDTO userDTO = new UserDTO(user.get());
        return userDTO;
    }
    @Override
    public Collection<UserDTO> findReportedUsers(){
        Collection<User> users = repository.findByIsReportedTrue();

        Collection<UserDTO> userDTOS = new ArrayList<>();
        for(User user : users){
            userDTOS.add(new UserDTO(user));
        }
        return userDTOS;
    }

}
