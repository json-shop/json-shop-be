package deepdive.jsonstore.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;


    //LoginTest를 위해 생성
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
