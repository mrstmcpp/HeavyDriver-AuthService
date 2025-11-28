package org.mrstm.uberauthproject.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import org.mrstm.uberauthproject.services.UserService;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.AddDocumentResponseDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DocumentDetailsDto;
import org.mrstm.uberentityservice.dto.driver.DriverPanel.DriverDocumentResponseDto;
import org.mrstm.uberentityservice.models.DriverDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class DriverPanelController {
    private final UserService userService;

    public DriverPanelController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getUserDetails(HttpServletRequest request) {
        Object details = userService.getUserDetails(request);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/driver/documents/verification")
    public ResponseEntity<DriverDocumentResponseDto> getDocumentDetails(@RequestHeader("X-User-Id") String driverId,
                                                                        @RequestHeader("X-User-Role") String role){
//        System.out.println("driver id and role: " + driverId + role);
        if(role.equals("PASSENGER")){
            throw new BadRequestException("This request is only valid for drivers.");
        }
        return new ResponseEntity<>(userService.getDocumentDetails(Long.parseLong(driverId)) , HttpStatus.OK);
    }

    @PostMapping("/documents/add")
    public ResponseEntity<AddDocumentResponseDto> addDriverDocument(@RequestHeader("X-User-Id") String driverId,
                                                                    @RequestHeader("X-User-Role") String role , @RequestBody AddDocumentDto addDocumentDto){
        if(role.equals("PASSENGER")){
            throw new BadRequestException("This request is only valid for drivers.");
        }
        return new ResponseEntity<>(userService.addDriverDocument(Long.parseLong(driverId) , addDocumentDto), HttpStatus.CREATED);
    }

}
