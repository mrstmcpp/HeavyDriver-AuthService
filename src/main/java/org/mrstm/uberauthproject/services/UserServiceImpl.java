package org.mrstm.uberauthproject.services;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberentityservice.dto.auth.UserPanel.DriverPanelDetails;
import org.mrstm.uberentityservice.dto.auth.UserPanel.PassengerPanelDetails;
import org.mrstm.uberentityservice.dto.auth.ValidUserDto;
import org.springframework.stereotype.Service;



@Service
public class UserServiceImpl implements UserService {
    private final DriverRepository driverRepository;
    private final AuthService authService;
    private final PassengerRepository passengerRepository;

    public UserServiceImpl(DriverRepository driverRepository, AuthService authService, PassengerRepository passengerRepository) {
        this.driverRepository = driverRepository;
        this.passengerRepository = passengerRepository;
        this.authService = authService;
    }

    public Object getUserDetails(HttpServletRequest request) {
        ValidUserDto validUser = authService.validateAndGetUser(request);

        if (!validUser.isLoggedIn()) {
            throw new NotAuthorizedException("Unauthorized user.");
        }

        return switch (validUser.getRole().toUpperCase()) {
            case "DRIVER" -> buildDriverDetails(validUser);
            case "PASSENGER" -> buildPassengerDetails(validUser);
            default -> throw new NotAuthorizedException("Unknown role.");
        };
    }

    private DriverPanelDetails buildDriverDetails(ValidUserDto user) {
        var driver = driverRepository.findById(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Driver not found."));

        return DriverPanelDetails.builder()
                .loggedIn(true)
                .role("DRIVER")
                .userId(driver.getId())
                .name(driver.getFullName())
                .email(driver.getEmail())
                .phoneNumber(driver.getPhoneNumber())
                .licenseNumber(driver.getLicenseNumber())
                .driverApprovalStatus(driver.getDriverApprovalStatus() == null ? "AUTOMATIC" : driver.getDriverApprovalStatus().toString())
                .aadharCardNumber(driver.getAadharCardNumber())
                .rating(driver.getRating())
                .activeBooking(driver.getActiveBooking() == null
                        ? "No active booking"
                        : driver.getActiveBooking().getId().toString())
                .activeCity(driver.getActiveCity())
                .build();
    }

    private PassengerPanelDetails buildPassengerDetails(ValidUserDto user) {
        var passenger = passengerRepository.findById(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Passenger not found."));

        return PassengerPanelDetails.builder()
                .loggedIn(true)
                .role("PASSENGER")
                .userId(passenger.getId())
                .name(passenger.getPassanger_name())
                .email(passenger.getEmail())
                .phoneNumber(passenger.getPhoneNumber())
                .build();
    }

}
