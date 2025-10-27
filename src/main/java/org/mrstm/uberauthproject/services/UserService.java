package org.mrstm.uberauthproject.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    Object getUserDetails(HttpServletRequest request);
}
