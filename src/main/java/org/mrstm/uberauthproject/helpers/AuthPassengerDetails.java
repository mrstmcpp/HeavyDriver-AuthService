package org.mrstm.uberauthproject.helpers;

import org.mrstm.uberentityservice.models.Passenger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthPassengerDetails extends Passenger implements UserDetails {

    // Why ?
    //Bcoz spring security works on UserDetails polymorphic type for auth
    private Long id;
    private String email;
    private String password;
    public AuthPassengerDetails(Passenger passenger) {
        this.id = passenger.getId();
        this.email = passenger.getEmail();
        this.password = passenger.getPassword();
    }

    /**
     * UserDetails interface helps us to get user-specific data .
     * It is an in-built interface in spring security.
     * We can use our own implementation bt it is recommended to use it.
     * @return
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public Long getId(){
        return this.id;
    }

    //currently some of the function is not needed. hence, set them to always true;
    @Override
    public boolean isAccountNonExpired() {//return UserDetails.super.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {// return UserDetails.super.isAccountNonLocked();
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {//        return UserDetails.super.isCredentialsNonExpired();
        return true;
    }

    @Override
    public boolean isEnabled() {//        return UserDetails.super.isEnabled();
        return true;
    }
}
