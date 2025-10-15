package org.mrstm.uberauthproject.providers;

import org.mrstm.uberauthproject.contexts.RoleContext;
import org.mrstm.uberauthproject.services.DriverDetailsServiceImpl;
import org.mrstm.uberauthproject.services.PassengerDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class MultiRoleAuthenticationProvider implements AuthenticationProvider {
    private final DriverDetailsServiceImpl driverDetailsService;
    private final PassengerDetailsServiceImpl passengerDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public MultiRoleAuthenticationProvider(DriverDetailsServiceImpl driverDetailsService, PassengerDetailsServiceImpl passengerDetailsService,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.driverDetailsService = driverDetailsService;
        this.passengerDetailsService = passengerDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();
//        System.out.println(rawPassword);
        String role = RoleContext.getRole();

        if(role == null){
            throw new BadCredentialsException("Role not provided in context");
        }
        UserDetails userDetails;
        if("DRIVER".equalsIgnoreCase(role)){
//            System.out.println(role);
            userDetails = driverDetailsService.loadUserByUsername(email);
//            System.out.println(userDetails.getUsername());

        }else if("PASSENGER".equalsIgnoreCase(role)){
            userDetails = passengerDetailsService.loadUserByUsername(email);
        }else{
            throw new BadCredentialsException("Invalid role: " + role);
        }

        if(!bCryptPasswordEncoder.matches(rawPassword , userDetails.getPassword())){
            throw new BadCredentialsException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(userDetails , null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
