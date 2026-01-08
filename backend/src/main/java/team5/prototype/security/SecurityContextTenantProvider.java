package team5.prototype.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextTenantProvider implements TenantProvider {

    @Override
    public Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Kein authentifizierter Benutzer vorhanden.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            Long tenantId = userPrincipal.getTenantId();
            if (tenantId == null) {
                throw new AccessDeniedException("Tenant-Information fehlt.");
            }
            return tenantId;
        }

        throw new AccessDeniedException("Unbekannter Principal.");
    }
}
