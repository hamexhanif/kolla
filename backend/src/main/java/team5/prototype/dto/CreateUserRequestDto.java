package team5.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequestDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Long tenantId;
    private Long roleId;
}