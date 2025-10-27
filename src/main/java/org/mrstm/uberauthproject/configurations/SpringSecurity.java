package org.mrstm.uberauthproject.configurations;


import org.mrstm.uberauthproject.filters.JwtAuthFilter;
import org.mrstm.uberauthproject.providers.MultiRoleAuthenticationProvider;
import org.mrstm.uberauthproject.services.PassengerDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SpringSecurity implements WebMvcConfigurer {
    //for setting up spring security.
    //this also helps to avoid authentication for first time registration.
    private final JwtAuthFilter jwtAuthFilter;

    private final MultiRoleAuthenticationProvider multiRoleAuthenticationProvider;

    public SpringSecurity(JwtAuthFilter jwtAuthFilter, MultiRoleAuthenticationProvider multiRoleAuthenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.multiRoleAuthenticationProvider = multiRoleAuthenticationProvider;
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new PassengerDetailsServiceImpl();
//    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // for avoiding 403 on sending requests.
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/signup/**").permitAll()
                        .requestMatchers("/signin/**").permitAll()
                        .requestMatchers("/validate/**").permitAll()
                        .requestMatchers("/logout/**").permitAll()
                        .anyRequest().permitAll()
                )
                .authenticationProvider(multiRoleAuthenticationProvider)
                .addFilterBefore(jwtAuthFilter , UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    //these below two methods are written to call spring security password matching mechanism.
//    @Bean
//    public AuthenticationProvider authenticationProvider() { //an AuthenticationProvider processes an Authentication request, and a fully authenticated object with full credentials is returned.
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService());
//        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
//        return authProvider;
//    }
    //this function is written for calling authenticationprovider.
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedOrigins("http://localhost:6969/" , "http://localhost:6970/")
                .allowedOrigins("http://localhost:3006/")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

}
