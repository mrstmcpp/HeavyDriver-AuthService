package org.mrstm.uberauthproject.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mrstm.uberauthproject.dto.*;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberauthproject.services.AuthService;
import org.mrstm.uberauthproject.services.JwtService;
import org.mrstm.uberauthproject.services.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final PassengerRepository passengerRepository;
    @Value("${cookie.expiry}")
    private int cookieExpiry;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private AuthService authService;
    private final RedisService redisService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService, PassengerRepository passengerRepository, RedisService redisService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passengerRepository = passengerRepository;
        this.redisService = redisService;
    }

    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerResponseDTO> signupPassenger(@RequestBody PassengerSignUpRequestDTO passengerSignUpRequestDTO) {
        PassengerResponseDTO response = authService.sigunUpPassenger(passengerSignUpRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signup/driver")
    public ResponseEntity<?> signupDriver(@RequestParam String driver) {
        return ResponseEntity.ok().build();
    }


    @PostMapping("/signin/passenger")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto , HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword()));
        //above line check whether user got successful login or not
        if(authentication.isAuthenticated()) {
            String jwtToken = jwtService.generateToken(authRequestDto.getEmail());
            ResponseCookie responseCookie = ResponseCookie.from("JwtToken" , jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7*24*3600)
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
            return new ResponseEntity<>(AuthResponseDto.builder().success(true).build(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Authorization unsuccessful.", HttpStatus.EXPECTATION_FAILED);
    }


    @GetMapping("/validate")
    public ResponseEntity<ValidUserDto> validatePassenger(HttpServletRequest request) {
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
                        .body(ValidUserDto.builder()
                                .loggedIn(false)
                                .build());
            }

            if(redisService.exists(jwtToken)){
                return ResponseEntity.ok(
                        ValidUserDto.builder()
                                .loggedIn(true)
                                .user(redisService.getValue(jwtToken).get("username"))
                                .userId(Long.parseLong(redisService.getValue(jwtToken).get("userId")))
                                .build()
                );
            }


            String username = jwtService.extractEmailFromToken(jwtToken);


//            String userId = jwtService.extractUserIdFromToken(jwtToken);
            if (username == null || username.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ValidUserDto.builder()
                                .loggedIn(false)
                                .build());
            }

            Long userId = passengerRepository.findByEmail(username)
                    .map(p -> p.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isTokenValid(jwtToken, username)) {
                redisService.setValue(jwtToken , username , userId.toString());
                return ResponseEntity.ok(
                        ValidUserDto.builder()
                                .loggedIn(true)
                                .user(username)
                                .userId(userId)
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ValidUserDto.builder()
                                .loggedIn(false)
                                .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ValidUserDto.builder()
                            .loggedIn(false)
                            .build());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("JwtToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // instantly expire
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new ResponseEntity<>("Logout Successful.", HttpStatus.OK);
    }

}
