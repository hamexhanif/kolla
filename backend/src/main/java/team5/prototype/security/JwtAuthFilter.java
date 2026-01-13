package team5.prototype.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.debug("\n>>> FILTER START: {}", request.getRequestURI());
        log.debug(">>> SecurityContext BEFORE: {}", SecurityContextHolder.getContext().getAuthentication());

        final String authHeader = request.getHeader("Authorization");
        log.debug(">>> Authorization Header: '{}'", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug(">>> No Bearer token, continuing...");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            log.debug(">>> Extracted JWT: '{}'", jwt);  // NEU!
            log.debug(">>> JWT Length: {}", jwt.length());
            final String email = jwtService.extractEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("=== JWT AUTH DEBUG ===");
                    log.debug("Request URI: {}", request.getRequestURI());
                    log.debug("User: {}", email);
                    log.debug("Authorities: {}", userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug(">>> SecurityContext SET TO: {}", SecurityContextHolder.getContext().getAuthentication());
                    log.debug(">>> Is Authenticated: {}", authToken.isAuthenticated());
                    log.debug("=====================");

                    logger.info("Benutzer '{}' erfolgreich authentifiziert.", email);
                }
            }
        } catch (Exception e) {
            logger.error("Fehler bei der JWT-Token-Validierung: {}", e.getMessage());
            e.printStackTrace();
        }

        log.debug(">>> SecurityContext BEFORE filterChain: {}", SecurityContextHolder.getContext().getAuthentication());

        filterChain.doFilter(request, response);

        log.debug(">>> SecurityContext AFTER filterChain: {}", SecurityContextHolder.getContext().getAuthentication());
        log.debug(">>> FILTER END\n");
    }
}