package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.UserMapper;
import com.Announcements.Announcements.model.Roles;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserCreateDTO register(UserCreateDTO userCreateDTO) {
        log.info("Начало регистрации пользователя: {}", userCreateDTO.username());

        if (userCreateDTO.password() == null || userCreateDTO.password().isEmpty()) {
            log.warn("Пароль не может быть пустым: {}", userCreateDTO.username());
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        Users user = userMapper.fromUserCreateDto(userCreateDTO);

        user.setPassword(bCryptPasswordEncoder.encode(userCreateDTO.password()));

        if (Objects.isNull(user.getRole())) {
            user.setRole(Roles.USER);
        }

        user.setStatus(Status.UNBLOCKED);
        user.setLimitNews(5);

        Users savedUser = userRepository.save(user);

        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getUsername());
        return userCreateDTO;
    }

    public String verify(LoginDTO loginDTO) {
        log.info("Попытка авторизации пользователя: {}", loginDTO.username());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password()));
        if (authentication.isAuthenticated()){
            log.info("Пользователь {} успешно авторизован.", loginDTO.username());
            return jwtService.generateToken(loginDTO.username());
        }

        log.warn("Не удалось авторизовать пользователя: {}", loginDTO.username());
        return "Fail";
    }

    private Users getTargetUser(Integer id) throws UserSelfException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Получение текущего пользователя: {}", username);

        Users currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Users targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        if (currentUser.equals(targetUser)) {
            log.warn("Попытка блокировки или разблокировки самого себя пользователем: {}", username);
            throw new UserSelfException("Ошибка блокировки", "Вы не можете заблокировать или разблокировать сами себя");
        }

        log.info("Пользователь найден: {}", targetUser.getUsername());
        return targetUser;
    }

    public UserDTO blockUser(Integer id) throws UserSelfException {
        log.info("Попытка блокировки пользователя с id={}", id);

        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.BLOCKED);
        userRepository.save(targetUser);

        log.info("Пользователь с id={} успешно заблокирован.", id);
        return userMapper.toUserDTO(targetUser);
    }

    public UserDTO unblockUser(Integer id) throws UserSelfException {
        log.info("Попытка разблокировки пользователя с id={}", id);

        Users targetUser = getTargetUser(id);

        targetUser.setStatus(Status.UNBLOCKED);
        userRepository.save(targetUser);

        log.info("Пользователь с id={} успешно разблокирован.", id);
        return userMapper.toUserDTO(targetUser);
    }

    public void checkBlocking(Integer id) throws Exception{
        log.info("Проверка блокировки пользователя с id={}", id);

        Optional<Users> usersOptional = userRepository.findById(id);
        Users user = usersOptional.get();

        if(user.getStatus() == Status.BLOCKED){
            log.warn("Пользователь с id={} заблокирован.", id);
            throw new BlockedException("Пользователь заблокирован");
        }

        log.info("Пользователь с id={} не заблокирован.", id);
    }

    public UserDTO findByUsername(String username) {
        log.info("Поиск пользователя по имени: {}", username);

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));

        log.info("Пользователь найден: {}", username);
        return userMapper.toUserDTO(user);
    }

    public UserDTO findById(Integer id){
        log.info("Поиск пользователя по id={}", id);

        Users users = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));

        log.info("Пользователь найден с id={}", id);
        return userMapper.toUserDTO(users);
    }

    public UserDTO updateUser(Users user){
        log.info("Обновление данных пользователя: {}", user.getUsername());

        Users updatedUser = userRepository.save(user);

        log.info("Пользователь {} успешно обновлен.", updatedUser.getUsername());
        return userMapper.toUserDTO(user);
    }


    public UserDTO updateLimitNews(Integer id, Integer newLimit){
        log.info("Обновление лимита объявлений для пользователя ID: {}. Новый лимит: {}", id, newLimit);

        Users user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Такой пользователь не найден!"));

        user.setLimitNews(newLimit);
        userRepository.save(user);

        log.info("Лимит для пользователя ID: {} успешно обновлен на {}.", id, newLimit);
        return userMapper.toUserDTO(user);
    }

    public UserDTO updateRole(Integer id, Roles updateRole){
        log.info("Обновление роли пользователя с ID: {}. Новая роль: {}", id, updateRole);

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден."));

        user.setRole(updateRole);
        Users userUpd = userRepository.save(user);

        log.info("Роль пользователя ID: {} успешно обновлена на {}.", id, updateRole);
        return userMapper.toUserDTO(userUpd);
    }

    public List<UserDTO> getAllUser(){
        log.info("Получение списка всех пользователей.");

        List<Users> users = userRepository.findAll();

        log.info("Получено {} пользователей.", users.size());
        return userMapper.toUserDTOList(users);
    }
}
