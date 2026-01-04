package team5.prototype.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.lang.NonNull; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.security.core.userdetails.*; import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; import org.springframework.stereotype.Component; import org.springframework.util.StringUtils; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) { this.jwtService = jwtService; this.userDetailsService = userDetailsService; }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) { filterChain.doFilter(request, response); return; }
        final String username = jwtService.extractUsername(token);
        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
    private String getTokenFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) { return bearerToken.substring(7); }
        return null;
    }
}