package com.mfgops.auth;

import com.mfgops.common.StandardResponse;
import com.mfgops.tenant.Tenant;
import com.mfgops.tenant.TenantContext;
import com.mfgops.tenant.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register-tenant")
    @Transactional
    public ResponseEntity<StandardResponse<AuthResponse>> registerTenant(@Valid @RequestBody RegisterRequest request) {
        // 1. Create and persist the Tenant (Factory)
        Tenant tenant = new Tenant();
        tenant.setName(request.getCompanyName());
        tenant.setSubdomain(request.getCompanyName().toLowerCase().replaceAll("\\s+", "-"));
        tenant.setCreatedBy("SYSTEM");
        tenant = tenantRepository.save(tenant);

        // 2. Bind current ThreadLocal context to permit owner user creation inside the prePersist interceptor
        TenantContext.setTenantId(tenant.getId());

        try {
            // 3. Create the OWNER Account
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setRole("OWNER");
            user.setCreatedBy("SYSTEM");
            userRepository.save(user);

            // 4. Generate Token
            String token = jwtUtil.generateToken(user);
            AuthResponse data = new AuthResponse(token, user.getUsername(), user.getRole(), tenant.getId());
            return ResponseEntity.ok(StandardResponse.success(data, "Tenant and Owner account registered successfully."));
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate standard username-password validation via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Load complete UserDetails and cast to our Tenant isolated user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUsername()));

        // Enforce active status checking
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(403).body(StandardResponse.failure(List.of("User account is inactive"), "Login failed."));
        }

        String token = jwtUtil.generateToken(user);
        AuthResponse data = new AuthResponse(token, user.getUsername(), user.getRole(), user.getTenantId());
        return ResponseEntity.ok(StandardResponse.success(data, "Login successful."));
    }
}
