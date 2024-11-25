package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.UserMapper;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserCreateDTO register(UserCreateDTO userCreateDTO) {
        if (userCreateDTO.password() == null || userCreateDTO.password().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        Users user = userMapper.fromUserCreateDto(userCreateDTO);

        user.setPassword(bCryptPasswordEncoder.encode(userCreateDTO.password()));

        if (Objects.equals(user.getRole(), "string") || user.getRole() == null) {
            user.setRole("USER");
        }

        user.setStatus(Status.UNBLOCKED);

        Users savedUser = userRepository.save(user);

        return userCreateDTO;
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
            throw new UserSelfException("Ошибка блокировки", "Вы не можете заблокировать или разблокировать сами себя");
        }

        return targetUser;
    }

    public UserDTO blockUser(Integer id) throws UserSelfException {
        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.BLOCKED);
        userRepository.save(targetUser);
        return userMapper.toUserDTO(targetUser);
    }

    public UserDTO unblockUser(Integer id) throws UserSelfException {
        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.UNBLOCKED);
        userRepository.save(targetUser);
        return userMapper.toUserDTO(targetUser);
    }

    public void checkBlocking(Integer id) throws Exception{
        Optional<Users> usersOptional = userRepository.findById(id);
        Users user = usersOptional.get();
        if(user.getStatus() == Status.BLOCKED){
            throw new BlockedException("Пользователь заблокирован");
        }
    }

    public UserDTO findByUsername(String username) {
        Optional<Users> user = userRepository.findByUsername(username);
        Users users = user.get();
        return userMapper.toUserDTO(users);
    }

    public UserDTO findById(Integer id){
        Optional<Users> user = userRepository.findById(id);
        Users users = user.get();
        return userMapper.toUserDTO(users);
    }

    public UserDTO updateUser(Users user){
        userRepository.save(user);
        return userMapper.toUserDTO(user);
    }


    public UserDTO updateLimitNews(Integer id, Integer newLimit){
        Users user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Такой пользователь не найден!"));

        user.setLimitNews(newLimit);
        userRepository.save(user);
        return userMapper.toUserDTO(user);
    }

    public UserDTO updateRole(Integer id, String updateRole){
        Optional<Users> usersOptional = userRepository.findById(id);
        Users user = usersOptional.get();

        user.setRole(updateRole);

        Users userUpd = userRepository.save(user);

        return userMapper.toUserDTO(userUpd);
    }

    public List<UserDTO> getAllUser(){
        List<Users> users = StreamSupport.stream(userRepository.findAll().spliterator(), false).toList();
        List<Users> usersList = new ArrayList<>();
        usersList.addAll(users);

        return userMapper.toUserDTOList(usersList);
    }
}
