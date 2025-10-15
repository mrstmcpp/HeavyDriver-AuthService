package org.mrstm.uberauthproject.services;

import org.mrstm.uberauthproject.helpers.AuthPassengerDetails;
import org.mrstm.uberentityservice.models.Passenger;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PassengerDetailsServiceImpl implements UserDetailsService {
    //class is responsible for loading the user in the form of UserDetails object for auth.

    private final PassengerRepository passengerRepository; //since it is not a concrete class. hence autowired can be used.

    public PassengerDetailsServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Passenger> passenger = passengerRepository.findByEmail(email);
        if(passenger.isPresent()){
            return new AuthPassengerDetails(passenger.get());
        }else{
            throw new UsernameNotFoundException("No passenger found with email: " + email);
        }
    }

}
