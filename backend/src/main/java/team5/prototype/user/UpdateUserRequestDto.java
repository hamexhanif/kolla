package team5.prototype.user;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter @Setter
public class UpdateUserRequestDto {
    private String username;
    private String email;
    private List<Long> roleIds;
}