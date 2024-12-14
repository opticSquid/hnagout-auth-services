package com.hangout.core.auth_api.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().contains("/v1/user/") || request.getRequestURI().contains("/v1/internal/")
                || request.getRequestURI().contains("/v1/admin/")) {
            String authorizationHeader = request.getHeader("Authorization");
            String jwt = authorizationHeader.substring(7);
            // extract username from jwt
            String username = this.accessTokenUtil.getUsername(jwt);
            log.debug("username extracted from jwt: {}", username);
            // try to get the userDetails from database
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // get the authentication token given the user details
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                        null,
                        userDetails.getAuthorities());
                // setting the details in auth object
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // setting security context for the current user
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (UsernameNotFoundException ex) {
                throw new UserNotFoundException("current user was not found in database");
            }
        }
        // proceed to next filter
        filterChain.doFilter(request, response);
    }

}
