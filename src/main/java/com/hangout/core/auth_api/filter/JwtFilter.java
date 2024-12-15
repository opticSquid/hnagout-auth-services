package com.hangout.core.auth_api.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().contains("/v1/user/") || request.getRequestURI().contains("/v1/admin/")) {
            log.info("Filtering request through JWT Filter");
            String authorizationHeader = request.getHeader("Authorization");
            log.debug("Authorization Token received: {}", authorizationHeader);
            String jwt = null;
            // try to extract jwt token from headers
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                log.debug("extracted jwt: {}", jwt);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            // validate the jwt
            if (!this.accessTokenUtil.validateToken(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

}
