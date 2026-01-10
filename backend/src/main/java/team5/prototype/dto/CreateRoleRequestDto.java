package team5.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequestDto {
    private String name;
    private String description;
    private Long tenantId;
}