package team5.prototype.security;

public class UserPrincipal {
    private final String username;
    private final Long tenantId;

    public UserPrincipal(String username, Long tenantId) {
        this.username = username;
        this.tenantId = tenantId;
    }

    public String getUsername() {
        return username;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
