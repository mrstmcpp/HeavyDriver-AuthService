package org.mrstm.uberauthproject.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotFoundException;
import org.mrstm.uberauthproject.contexts.RoleContext;
import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberauthproject.services.AuthService;
import org.mrstm.uberauthproject.services.JwtService;
import org.mrstm.uberauthproject.services.RedisService;
import org.mrstm.uberentityservice.dto.auth.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;
    private final RedisService redisService;

    @Value("${cookie.expiry}")
    private int cookieExpiry;

    public AuthController(
            DriverRepository driverRepository,
            AuthService authService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PassengerRepository passengerRepository,
            RedisService redisService
    ) {
        this.driverRepository = driverRepository;
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passengerRepository = passengerRepository;
        this.redisService = redisService;
    }


    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerResponseDTO> signupDriver(@RequestBody PassengerSignUpRequestDTO dto) {
        PassengerResponseDTO response = authService.sigunUpPassenger(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/signup/driver")
    public ResponseEntity<DriverSignUpResponseDto> signupDriver(@RequestBody DriverSignUpRequest dto) {
        DriverSignUpResponseDto response = authService.signupDriver(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        try {
            RoleContext.setRole(authRequestDto.getRole().toString().toUpperCase());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword())
            );

            if (!authentication.isAuthenticated()) {
                return new ResponseEntity<>("Authorization unsuccessful.", HttpStatus.EXPECTATION_FAILED);
            }

            String role = authRequestDto.getRole().toString().toUpperCase();
            String name;
            Long userId;

            switch (role) {
                case "DRIVER" -> {
                    var driver = driverRepository.findByEmail(authRequestDto.getEmail())
                            .orElseThrow(() -> new NotFoundException("Driver not found."));
                    userId = driver.getId();
                    name = driver.getFullName();
                }
                case "PASSENGER" -> {
                    var passenger = passengerRepository.findByEmail(authRequestDto.getEmail())
                            .orElseThrow(() -> new NotFoundException("Passenger not found."));
                    userId = passenger.getId();
                    name = passenger.getPassanger_name();
                }
                default -> {
                    return ResponseEntity.badRequest().body("Invalid role provided.");
                }
            }


            // gen JWT with role + userId
            String jwtToken = jwtService.generateToken(authRequestDto.getEmail(), role, userId);

            // Create cookie
            ResponseCookie responseCookie = ResponseCookie.from("JwtToken", jwtToken)
                    .httpOnly(true)
                    .secure(false) // https
                    .path("/")
                    .maxAge(cookieExpiry)
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

            redisService.setValue(jwtToken, authRequestDto.getEmail(), userId.toString(), role , name);

            return new ResponseEntity<>(AuthResponseDto.builder()
                    .success(true)
                    .build(), HttpStatus.OK);
        } finally {
            RoleContext.clear();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidUserDto> validateUser(HttpServletRequest request) {
        try {
            String jwtToken = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("JwtToken".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (jwtToken == null || jwtToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ValidUserDto.builder().loggedIn(false).build());
            }

            if (redisService.exists(jwtToken)) {
                Map<String, String> data = redisService.getValue(jwtToken);
                if (data == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ValidUserDto.builder().loggedIn(false).build());
                }

                return ResponseEntity.ok(
                        ValidUserDto.builder()
                                .loggedIn(true)
                                .user(data.get("username"))
                                .name(data.get("name"))
                                .userId(Long.parseLong(data.get("userId")))
                                .role(data.get("role"))
                                .build()
                );
            }

            String username = jwtService.extractEmailFromToken(jwtToken);
            String role = jwtService.extractRoleFromToken(jwtToken);
            Long userId = jwtService.extractUserIdFromToken(jwtToken);

            if (username == null || role == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ValidUserDto.builder().loggedIn(false).build());
            }

            if (jwtService.isTokenValid(jwtToken, username)) {
                String name;
                switch (role.toUpperCase()) {
                    case "DRIVER" -> {
                        var driver = driverRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("Driver not found."));
                        name = driver.getFullName();
                    }
                    case "PASSENGER" -> {
                        var passenger = passengerRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("Passenger not found."));
                        name = passenger.getPassanger_name();
                    }
                    default -> {
                        return ResponseEntity.badRequest().body(
                                ValidUserDto.builder()
                                        .loggedIn(false)
                                        .build()
                        );
                    }
                }

                redisService.setValue(jwtToken, username, String.valueOf(userId), role, name);

                return ResponseEntity.ok(
                        ValidUserDto.builder()
                                .loggedIn(true)
                                .user(username)
                                .name(name)
                                .userId(userId)
                                .role(role)
                                .build()
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ValidUserDto.builder().loggedIn(false).build());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ValidUserDto.builder().loggedIn(false).build());
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String jwtToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken != null && !jwtToken.isBlank()) {
            redisService.delete(jwtToken);
        }

        ResponseCookie cookie = ResponseCookie.from("JwtToken", "")
                .httpOnly(true)
                .secure(false) //https
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new ResponseEntity<>("Logout Successful.", HttpStatus.OK);
    }

}