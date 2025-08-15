package org.mrstm.uberauthproject.services;

import org.mrstm.uberauthproject.dto.PassengerResponseDTO;
import org.mrstm.uberauthproject.dto.PassengerSignUpRequestDTO;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final PassengerRepository passengerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthServiceImpl(PassengerRepository passengerRepository , BCryptPasswordEncoder bCryptPasswordEncoder) {
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
}
