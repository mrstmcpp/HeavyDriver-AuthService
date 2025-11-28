package org.mrstm.uberauthproject.services;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.mrstm.uberauthproject.repositories.DriverDocumentRepository;
import org.mrstm.uberauthproject.repositories.DriverRepository;
import org.mrstm.uberauthproject.repositories.DriverVerificationRepository;
import org.mrstm.uberauthproject.repositories.PassengerRepository;
import org.mrstm.uberentityservice.dto.auth.UserPanel.DriverPanelDetails;
import org.mrstm.uberentityservice.dto.auth.UserPanel.PassengerPanelDetails;
import org.mrstm.uberentityservice.dto.auth.ValidUserDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentResponseDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DocumentDetailsDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DriverDocumentResponseDto;
import org.mrstm.uberentityservice.models.Driver;
import org.mrstm.uberentityservice.models.DriverDocument;
import org.mrstm.uberentityservice.models.DriverVerification;
import org.mrstm.uberentityservice.models.VerificationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final DriverRepository driverRepository;
    private final AuthService authService;
    private final PassengerRepository passengerRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final DriverVerificationRepository driverVerificationRepository;

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

    @Override
    public DriverDocumentResponseDto getDocumentDetails(Long driverId) {
        DriverVerification verification = driverVerificationRepository.findByDriverId(driverId);
        return DriverDocumentResponseDto.builder()
                .documentList(verification.getDriverDocumentList())
                .driverId(verification.getDriver().getId())
                .remarks(verification.getRemarks())
                .verifiedAt(verification.getVerifiedAt())
                .build();

    }

    @Override
    @Transactional
    public AddDocumentResponseDto addDriverDocument(Long driverId, AddDocumentDto addDocumentDto) {
        Long verificationId = driverVerificationRepository.findVerificationIdByDriverId(driverId)
                .orElseGet(() -> {
                    DriverVerification newVerification = new DriverVerification();
                    newVerification.setDriver(Driver.builder().id(driverId).build());
                    DriverVerification saved = driverVerificationRepository.save(newVerification);
                    return saved.getId();
                });

        DriverVerification verificationRef = DriverVerification.builder().id(verificationId).build();

        Optional<DriverDocument> existingDoc =
                driverDocumentRepository.findByDriverVerificationIdAndDocumentType(verificationId, addDocumentDto.getDocumentType());

        DriverDocument document = existingDoc.orElseGet(DriverDocument::new);
        document.setDocumentType(addDocumentDto.getDocumentType());
        document.setDocumentUrl(addDocumentDto.getFileUrl());
        document.setFileName(addDocumentDto.getFileName());
        document.setVerificationStatus(VerificationStatus.PENDING);
        document.setDriverVerification(verificationRef);


        driverDocumentRepository.save(document);
        return AddDocumentResponseDto.builder()
                .message("Document Uploaded Successfully.")
                .documentType(document.getDocumentType())
                .fileUrl(document.getDocumentUrl())
                .status(document.getVerificationStatus())
                .build();
    }



}
