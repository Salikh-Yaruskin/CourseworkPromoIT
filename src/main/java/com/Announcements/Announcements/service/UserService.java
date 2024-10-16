package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;

    public UserDTO register(UserDTO userDTO) {
        if (userDTO.password() == null || userDTO.password().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        Users user = new Users(userDTO);

        user.setPassword(bCryptPasswordEncoder.encode(userDTO.password()));

        if (Objects.equals(user.getRole(), "string") || user.getRole() == null) {
            user.setRole("USER");
        }

        user.setStatus(Status.UNBLOCKED);

        Users savedUser = userRepository.save(user);

        return new UserDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getGmail(),
                null,
                savedUser.getRole(),
                savedUser.getStatus()
        );
    }

    public String verify(LoginDTO loginDTO) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password()));
        if (authentication.isAuthenticated()){
            return jwtService.generateToken(loginDTO.username());
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

    public Users updateLimitNews(Integer id, Integer newLimit){
        Users user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Такой пользователь не найден!"));

        user.setLimitNews(newLimit);
        return userRepository.save(user);
    }

    public UserDTO updateRole(Integer id, String updateRole){
        Optional<Users> usersOptional = userRepository.findById(id);
        Users user = usersOptional.get();

        user.setRole(updateRole);

        Users userUpd = userRepository.save(user);

        return new UserDTO(
                userUpd.getId(),
                userUpd.getUsername(),
                userUpd.getGmail(),
                null,
                userUpd.getRole(),
                userUpd.getStatus()
        );
    }

    public List<UserDTO> getAllUser(){
        List<Users> users = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();
        List<Users> usersList = new ArrayList<>();
        usersList.addAll(users);

        return usersList
                .stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getGmail(),
                        null,
                        user.getRole(),
                        user.getStatus()

                ))
                .collect(Collectors.toList());
    }
}
