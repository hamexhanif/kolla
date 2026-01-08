package team5.prototype.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/api/auth/**", "/h2-console/**", "/ws/**").permitAll()
                        // 2. Manager-spezifische Endpunkte
                        .requestMatchers("/api/manager/**").hasRole("WORKFLOW_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("WORKFLOW_MANAGER") // Nur Manager dürfen ALLE User sehen
                        // 3. Regeln für normale Benutzer
                        // Jeder authentifizierte Benutzer darf seine EIGENEN Tasks abrufen.
                        // Spring EL (@) erlaubt uns hier, die userId aus dem Pfad mit dem Namen des eingeloggten Benutzers zu vergleichen.
                        // (Dies ist fortgeschritten, aber die sauberste Lösung).
                        // Für den Prototyp ist eine einfachere Regel ausreichend:
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/tasks").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/dashboard").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}