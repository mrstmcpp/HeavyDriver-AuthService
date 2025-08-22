package org.mrstm.uberauthproject.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mrstm.uberauthproject.dto.*;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberauthproject.services.AuthService;
import org.mrstm.uberauthproject.services.JwtService;
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

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService, PassengerRepository passengerRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passengerRepository = passengerRepository;
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
    public ResponseEntity<ValidUserDto> validatePassenger(HttpServletRequest request)  {
        String jwtToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("current cookie bc" + cookie.getName() + " " + cookie.getValue());
                if ("JwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        String username = jwtService.extractEmailFromToken(jwtToken);
        Long userId = passengerRepository.findByEmail(username).get().getId();
        if (jwtService.isTokenValid(jwtToken, username)) {
            return new ResponseEntity<>(ValidUserDto.builder()
                    .loggedIn(true)
                    .user((username))
                    .userId(userId)
                    .build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ValidUserDto.builder()
                    .loggedIn(false).build(), HttpStatus.UNAUTHORIZED);
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
