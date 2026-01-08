package team5.prototype.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
@Service
public class JwtService {
    private final SecretKey jwtSecretKey;
    public JwtService(@Value("${jwt.secret}") String jwtSecret) { this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)); }
    public String extractEmail(String token) { return extractClaim(token, Claims::getSubject); }
    public boolean isTokenValid(String token, UserDetails userDetails) { final String emailFromToken = extractEmail(token); return (emailFromToken.equals(userDetails.getUsername())) && !isTokenExpired(token); }
    public String generateToken(UserDetails userDetails, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)).signWith(jwtSecretKey, SignatureAlgorithm.HS512).compact();
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { final Claims claims = extractAllClaims(token); return claimsResolver.apply(claims); }
    private boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }
    private Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }
    private Claims extractAllClaims(String token) { return Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token).getBody(); }
}