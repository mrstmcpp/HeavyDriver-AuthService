package org.mrstm.uberauthproject.services;


import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberentityservice.dto.auth.DriverSignUpRequest;
import org.mrstm.uberentityservice.dto.auth.DriverSignUpResponseDto;
import org.mrstm.uberentityservice.dto.auth.PassengerResponseDTO;
import org.mrstm.uberentityservice.dto.auth.PassengerSignUpRequestDTO;
import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthServiceImpl(PassengerRepository passengerRepository, DriverRepository driverRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.driverRepository = driverRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public PassengerResponseDTO sigunUpPassenger(PassengerSignUpRequestDTO passengerSignUpRequestDTO) {
        try {
            if (passengerRepository.findByEmail(passengerSignUpRequestDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Passenger already exists with email: " + passengerSignUpRequestDTO.getEmail());
            }

            if(passengerRepository.findByPhoneNumber(passengerSignUpRequestDTO.getPhoneNumber()).isPresent()){
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

            if(driverRepository.findByPhoneNumber(driverSignUpRequest.getPhoneNumber()).isPresent()){
                throw new RuntimeException("Passenger already exists with phone number: " + driverSignUpRequest.getPhoneNumber());
            }

            Driver driver = Driver.builder()
                    .email(driverSignUpRequest.getEmail())
                    .password(bCryptPasswordEncoder.encode(driverSignUpRequest.getPassword())) // encrypt
                    .fullName(driverSignUpRequest.getName())
                    .phoneNumber(driverSignUpRequest.getPhoneNumber())
                    .build();

            driverRepository.save(driver);
            return DriverSignUpResponseDto.fromDriver(driver);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while signing up passenger", e);
        }
    }
}
