package org.mrstm.uberauthproject.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ValidUserDto {
    private boolean loggedIn;
    private String user;
}
