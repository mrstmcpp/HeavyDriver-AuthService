package org.mrstm.uberauthproject.services;


import org.mrstm.uberauthproject.helpers.AuthDriverDetails;
import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberentityservice.models.Driver;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverDetailsServiceImpl implements UserDetailsService {
    private final DriverRepository driverRepository;

    public DriverDetailsServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Driver> driver = driverRepository.findByEmail(email);
        if(driver.isPresent()){
            return new AuthDriverDetails(driver.get());
        }else{
            throw new UsernameNotFoundException("No driver found with email: " + email);
        }
    }
}
