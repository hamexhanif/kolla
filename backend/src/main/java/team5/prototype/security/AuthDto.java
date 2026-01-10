package team5.prototype.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {

    private String username;
    private String email;
    private String password;
    private Long tenantId;

    private String token;

    public AuthDto(String token) {
        this.token = token;
    }

    public AuthDto() {
    }
}
