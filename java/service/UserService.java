package com.example.group56.service;

import com.example.group56.dto.AccountDTO;
import com.example.group56.model.User;
import com.example.group56.model.Enrolment;
import com.example.group56.repo.UserQuestRepository;
import com.example.group56.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserQuestRepository userQuestRepository;

    public User registerUser(AccountDTO accountDTO) {
        User user = userRepository.save(new User(
                accountDTO.getEmail(),
                passwordEncoder.encode(accountDTO.getPassword()))
        );
        avatarService.saveDefaultAvatar(user);
        return user;
    }

    public Boolean isUserTakingQuest(User user, Enrolment enrolment) {
        return userQuestRepository.existsByUserAndEnrolment(user, enrolment);
    }

    // Returns a UserDetails for Spring security
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /* Access to repo directly to save injecting both into a controller. */

    public Optional<User> getUserByName(String username) {
        return userRepository.findByName(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) { return userRepository.findById(id); }
    public Optional<User> getUserByEmail(String email) { return userRepository.findByEmail(email); }
}
