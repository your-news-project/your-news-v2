package kr.co.yournews.auth.service;

import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService {
    private final UserService userService;
    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * dto를 통해 비밀번호 인코딩 후, 회원가입 진행 메서드.
     *
     * @param signUpDto : 사용자 회원가입 요청 dto
     */
    public void createUser(SignUpDto.Auth signUpDto) {
        User user = signUpDto.toEntity(signUpDto.password(bCryptPasswordEncoder));
        userService.save(user);
    }
}
