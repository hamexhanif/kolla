package team5.prototype.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {

    private String email;
    private String password;
    private String token;
    private Long userId;

    public AuthDto(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }
}
