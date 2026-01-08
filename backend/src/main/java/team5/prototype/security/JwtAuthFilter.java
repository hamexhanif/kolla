package team5.prototype.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        final String authHeader = request.getHeader("Authorization");

        // Wenn kein Header da ist oder er nicht mit "Bearer " beginnt, überspringen
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String email = jwtService.extractEmail(jwt);

            // Wenn wir einen Benutzernamen haben und der Benutzer noch nicht authentifiziert ist
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("=== JWT AUTH DEBUG ===");
                    System.out.println("Request URI: " + request.getRequestURI());
                    System.out.println("User: " + email);
                    System.out.println("Authorities: " + userDetails.getAuthorities());
                    System.out.println("=====================");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Benutzer '{}' erfolgreich authentifiziert.", email);
                } else {
                    logger.warn("Ungültiger JWT-Token für Benutzer '{}' empfangen.", email);
                }
            }
        } catch (UsernameNotFoundException e) {
            logger.error("Benutzer aus JWT nicht in der Datenbank gefunden: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Fehler bei der JWT-Token-Validierung: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
        System.out.println(">>> AFTER filterChain.doFilter");

    }
}