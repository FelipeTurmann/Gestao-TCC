//package com.example.gestaotcc.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new InMemoryUserDetailsManager(
//                User.withDefaultPasswordEncoder()
//                        .username("user")
//                        .password("admin")
//                        .roles("USER")
//                        .build()
//        );
//    }
//
//
//}