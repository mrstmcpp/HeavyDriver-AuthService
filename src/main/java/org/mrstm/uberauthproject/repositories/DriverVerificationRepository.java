package org.mrstm.uberauthproject.repositories;

import org.mrstm.uberentityservice.models.DriverVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverVerificationRepository extends JpaRepository<DriverVerification , Long> {
    @Query("SELECT v.id FROM DriverVerification v WHERE v.driver.id = :driverId")
    Optional<Long> findVerificationIdByDriverId(@Param("driverId") Long driverId);
    DriverVerification findByDriverId(Long driverId);
}
