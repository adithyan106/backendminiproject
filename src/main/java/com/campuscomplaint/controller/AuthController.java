package com.campuscomplaint.controller;

import com.campuscomplaint.dto.UserDto;
import com.campuscomplaint.model.User;
import com.campuscomplaint.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public ResponseEntity<?> registerHelp() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Use POST /api/auth/register with JSON body: {name, email, password, role}");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email is already registered!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User newUser = new User(
                userDto.getName(),
                userDto.getEmail(),
                userDto.getPassword(), // In production, hash the password!
                userDto.getRole()
        );

        userRepository.save(newUser);

        // Don't leak the password in the response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("role", newUser.getRole());
        response.put("name", newUser.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public ResponseEntity<?> loginHelp() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Use POST /api/auth/login with JSON body: {email, password, role}");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
        Optional<User> userOptional = userRepository.findByEmail(userDto.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Validate password and role
            if (user.getPassword().equals(userDto.getPassword()) && user.getRole() == userDto.getRole()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("role", user.getRole());
                response.put("name", user.getName());
                response.put("id", user.getId());
                return ResponseEntity.ok(response);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid email, password, or role.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
