package org.mrstm.uberauthproject.services;

import jakarta.servlet.http.HttpServletRequest;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentResponseDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DocumentDetailsDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DriverDocumentResponseDto;
import org.mrstm.uberentityservice.models.DriverDocument;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    Object getUserDetails(HttpServletRequest request);

    DriverDocumentResponseDto getDocumentDetails(Long driverId);
    AddDocumentResponseDto addDriverDocument(Long driverId , AddDocumentDto addDocumentDto);
}
