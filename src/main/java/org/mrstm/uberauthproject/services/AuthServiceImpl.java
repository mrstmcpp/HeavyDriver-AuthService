package org.mrstm.uberauthproject.services;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberentityservice.dto.auth.*;
import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;

    public AuthServiceImpl(PassengerRepository passengerRepository, DriverRepository driverRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtService jwtService, RedisService redisService) {
        this.driverRepository = driverRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passengerRepository = passengerRepository;
        this.jwtService = jwtService;
        this.redisService = redisService;
    }

    @Override
    public PassengerResponseDTO sigunUpPassenger(PassengerSignUpRequestDTO passengerSignUpRequestDTO) {
        try {
            if (passengerRepository.findByEmail(passengerSignUpRequestDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Passenger already exists with email: " + passengerSignUpRequestDTO.getEmail());
            }

            if (passengerRepository.findByPhoneNumber(passengerSignUpRequestDTO.getPhoneNumber()).isPresent()) {
                throw new RuntimeException("Passenger already exists with phone number: " + passengerSignUpRequestDTO.getPhoneNumber());
            }

            Passenger passenger = Passenger.builder()
                    .email(passengerSignUpRequestDTO.getEmail())
                    .password(bCryptPasswordEncoder.encode(passengerSignUpRequestDTO.getPassword())) // encrypt
                    .passanger_name(passengerSignUpRequestDTO.getName())
                    .phoneNumber(passengerSignUpRequestDTO.getPhoneNumber())
                    .build();

            Passenger p = passengerRepository.save(passenger);
            return PassengerResponseDTO.fromPassenger(p);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while signing up passenger", e);
        }
    }

    @Override
    public DriverSignUpResponseDto signupDriver(DriverSignUpRequest driverSignUpRequest) {
        try {
            if (driverRepository.findByEmail(driverSignUpRequest.getEmail()).isPresent()) {
                throw new RuntimeException("Passenger already exists with email: " + driverSignUpRequest.getEmail());
            }

            if (driverRepository.findByPhoneNumber(driverSignUpRequest.getPhoneNumber()).isPresent()) {
                throw new RuntimeException("Passenger already exists with phone number: " + driverSignUpRequest.getPhoneNumber());
            }

            Driver driver = Driver.builder()
                    .email(driverSignUpRequest.getEmail())
                    .password(bCryptPasswordEncoder.encode(driverSignUpRequest.getPassword())) // encrypt
                    .fullName(driverSignUpRequest.getName())
                    .phoneNumber(driverSignUpRequest.getPhoneNumber())
                    .aadharCardNumber(driverSignUpRequest.getAadharCardNumber())
                    .build();

            driverRepository.save(driver);
            return DriverSignUpResponseDto.fromDriver(driver);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while signing up passenger", e);
        }
    }

    @Override
    public ValidUserDto validateAndGetUser(HttpServletRequest request) {
        try {
            String jwtToken = extractJwtFromRequest(request);
            if (jwtToken == null || jwtToken.isBlank()) {
                return ValidUserDto.builder().loggedIn(false).build();
            }

            if (redisService.exists(jwtToken)) {
                Map<String, String> data = redisService.getValue(jwtToken);
                if (data == null) return ValidUserDto.builder().loggedIn(false).build();

                return ValidUserDto.builder()
                        .loggedIn(true)
                        .user(data.get("username"))
                        .name(data.get("name"))
                        .userId(Long.parseLong(data.get("userId")))
                        .role(data.get("role"))
                        .build();
            }

            String username = jwtService.extractEmailFromToken(jwtToken);
            String role = jwtService.extractRoleFromToken(jwtToken);
            Long userId = jwtService.extractUserIdFromToken(jwtToken);

            if (username == null || role == null || userId == null) {
                return ValidUserDto.builder().loggedIn(false).build();
            }

            if (!jwtService.isTokenValid(jwtToken, username)) {
                return ValidUserDto.builder().loggedIn(false).build();
            }

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
                    return ValidUserDto.builder().loggedIn(false).build();
                }
            }

            redisService.setValue(jwtToken, username, String.valueOf(userId), role, name);

            return ValidUserDto.builder()
                    .loggedIn(true)
                    .user(username)
                    .name(name)
                    .userId(userId)
                    .role(role)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ValidUserDto.builder().loggedIn(false).build();
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
