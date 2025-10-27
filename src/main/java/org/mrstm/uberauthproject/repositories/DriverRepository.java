package org.mrstm.uberauthproject.repositories;

import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    Optional<Driver> findByPhoneNumber(String phoneNumber);

    Optional<Driver> getDriverById(long l);
}
