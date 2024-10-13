package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;

    public Users register(Users user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if (Objects.equals(user.getRole(), "string")){
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public String verify(Users user) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()){
            return jwtService.generateToken(user.getUsername());
        }
        return "Fail";
    }

    private Users getTargetUser(Integer id) throws UserSelfException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Users targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        if (currentUser.equals(targetUser)) {
            throw new UserSelfException("Вы не можете заблокировать или разблокировать сами себя");
        }

        return targetUser;
    }

    public Users blockUser(Integer id) throws UserSelfException {
        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.BLOCKED);
        return userRepository.save(targetUser);
    }

    public Users unblockUser(Integer id) throws UserSelfException {
        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.UNBLOCKED);
        return userRepository.save(targetUser);
    }

    public void checkBlocking(Integer id) throws Exception{
        Optional<Users> usersOptional = userRepository.findById(id);
        Users user = usersOptional.get();
        if(user.getStatus() == Status.BLOCKED){
            throw new BlockedException("Пользователь заблокирован");
        }
    }

    public Users findByUsername(String username) {
        Optional<Users> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Users findById(Integer id){
        Optional<Users> user = userRepository.findById(id);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public Users updateUser(Users user){
        return userRepository.save(user);
    }
}
