package org.mrstm.uberauthproject.services;

import jakarta.servlet.http.HttpServletRequest;
import org.mrstm.uberentityservice.dto.auth.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public PassengerResponseDTO sigunUpPassenger(PassengerSignUpRequestDTO passengerSignUpRequestDTO);
    public DriverSignUpResponseDto signupDriver(DriverSignUpRequest driverSignUpRequest);
    public ValidUserDto validateAndGetUser(HttpServletRequest request);
}
