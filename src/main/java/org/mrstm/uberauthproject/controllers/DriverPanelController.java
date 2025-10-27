package org.mrstm.uberauthproject.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.mrstm.uberauthproject.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
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

}
