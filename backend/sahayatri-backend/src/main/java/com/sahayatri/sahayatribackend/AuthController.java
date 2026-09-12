package com.sahayatri.sahayatribackend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // =========================
    // SIGNUP
    // =========================

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Email already registered"
                    ));
        }

        // Save user
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Account created successfully!",
                        "name",
                        savedUser.getName(),
                        "email",
                        savedUser.getEmail()
                )
        );
    }


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginData) {

        String email =
                loginData.get("email");

        String password =
                loginData.get("password");


        // Find user by email
        User user =
                userRepository.findByEmail(email)
                        .orElse(null);


        // User not found
        if (user == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "error",
                            "Invalid email or password"
                    ));
        }


        // Check password
        if (!user.getPassword().equals(password)) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "error",
                            "Invalid email or password"
                    ));
        }


        // Login successful
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Login successful!",
                        "name",
                        user.getName(),
                        "email",
                        user.getEmail()
                )
        );
    }
}