package com.mfgops.auth;

import com.mfgops.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        UUID tenantId = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
                tenantId = jwtUtil.extractTenantId(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) this.userDetailsService.loadUserByUsername(username);

                // Ensure username and tenant isolation align perfectly
                if (jwtUtil.validateToken(jwt, userDetails) && userDetails.getTenantId().equals(tenantId)) {
                    // Populate ThreadLocal TenantContext
                    TenantContext.setTenantId(tenantId);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Clean up the ThreadLocal to prevent memory leaks and pollution in Tomcat thread pools
            TenantContext.clear();
        }
    }
}
