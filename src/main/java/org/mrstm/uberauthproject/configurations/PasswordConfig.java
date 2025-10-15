package org.mrstm.uberauthproject.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean //telling spring boot that it is a bean
    // as there could 6 type of more constructors could be possible we need to define which one to use.
    // u can check it in the BCryptPasswordEncoder package.
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
