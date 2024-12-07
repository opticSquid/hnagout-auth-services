package com.hangout.core.auth_api.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hangout.core.auth_api.exceptions.UserNotFoundException;
import com.hangout.core.auth_api.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        log.debug("Authorization Token received: {}", authorizationHeader);
        String username = null;
        String jwt = null;
        // try to extract jwt token from headers
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.debug("extracted jwt: {}", jwt);
        }
        // if jwt token was found
        if (jwt != null) {
            // validate the jwt
            if (this.accessTokenUtil.validateToken(jwt)) {
                // extract username from jwt
                username = this.accessTokenUtil.getUsername(jwt);
                log.debug("username extracted from jwt: {}", username);
                UserDetails userDetails = null;
                // try to get the userDetails from database
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(username);
                } catch (Exception ex) {
                    throw new UserNotFoundException("current user was not found in database");
                }
                // user has been found here
                // get the authentication token given the user details
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                        null,
                        userDetails.getAuthorities());
                // setting the details in auth object
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // setting security context for the current user
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
