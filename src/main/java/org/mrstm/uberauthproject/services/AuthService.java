package org.mrstm.uberauthproject.services;

import org.mrstm.uberentityservice.dto.auth.DriverSignUpRequest;
import org.mrstm.uberentityservice.dto.auth.DriverSignUpResponseDto;
import org.mrstm.uberentityservice.dto.auth.PassengerResponseDTO;
import org.mrstm.uberentityservice.dto.auth.PassengerSignUpRequestDTO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public PassengerResponseDTO sigunUpPassenger(PassengerSignUpRequestDTO passengerSignUpRequestDTO);
    public DriverSignUpResponseDto signupDriver(DriverSignUpRequest driverSignUpRequest);
}
