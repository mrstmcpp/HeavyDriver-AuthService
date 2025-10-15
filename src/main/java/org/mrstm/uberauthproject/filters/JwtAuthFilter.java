package org.mrstm.uberauthproject.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mrstm.uberauthproject.services.JwtService;
import org.mrstm.uberauthproject.services.DriverDetailsServiceImpl;
import org.mrstm.uberauthproject.services.PassengerDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private PassengerDetailsServiceImpl passengerDetailsService;
    @Autowired
    private DriverDetailsServiceImpl driverDetailsService;

    private final JwtService jwtService;

    private final RequestMatcher requestMatcher =
            new AntPathRequestMatcher("/api/v1/auth/validate", HttpMethod.GET.name());

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JwtToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtService.extractEmailFromToken(token);
        String role = jwtService.extractRoleFromToken(token);

        if (email != null && role != null) {
            UserDetails userDetails;
            if ("DRIVER".equalsIgnoreCase(role)) {
                userDetails = driverDetailsService.loadUserByUsername(email);
            } else {
                userDetails = passengerDetailsService.loadUserByUsername(email);
            }

            if (jwtService.isTokenValid(token, email)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        RequestMatcher matcher = new NegatedRequestMatcher(requestMatcher);
        return matcher.matches(request);
    }
}
