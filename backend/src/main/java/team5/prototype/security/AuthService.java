package team5.prototype.security;

public interface AuthService {
    String login(String email, String password);
    String login(String email, String password, Long tenantId);
}
