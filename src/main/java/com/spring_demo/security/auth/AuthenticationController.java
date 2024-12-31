package com.spring_demo.security.auth;


import com.spring_demo.security.User.Role;
import com.spring_demo.security.User.User;
import com.spring_demo.security.User.UserRepository;
import com.spring_demo.security.config.JWTService;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Getter
@RestController
@RequestMapping("/api/v1/auth")

public class AuthenticationController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public  AuthenticationController (UserRepository repository,
                                      PasswordEncoder passwordEncoder,
                                      JWTService jwtService,
                                      AuthenticationManager authenticationManager
    )
    {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register
            (
                    @RequestBody
                    RegisterRequest request

    ){
return ResponseEntity.ok(registerService(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate
            (
                    @RequestBody
                    AuthenticationRequest request

            ){
        return ResponseEntity.ok(authenticateService(request));
    }

    public AuthenticationResponse registerService(@NotNull RegisterRequest request) {
        var user = new User(
                request.getFirstname(),
                request.getLastname(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );

        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(
                jwtToken
               );
    }

    public AuthenticationResponse authenticateService(@NotNull AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(
                jwtToken
        );
    }
}
