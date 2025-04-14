package kr.co.yournews.apis.auth.service;

import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final UserService userService;
    private final PasswordEncodeService passwordEncodeService;
    private final JwtHelper jwtHelper;

    /**
     * dto를 통해 비밀번호 인코딩 후, 회원가입 진행 메서드.
     *
     * @param signUpDto : 사용자 회원가입 요청 dto
     */
    @Transactional
    public void signUp(SignUpDto.Auth signUpDto) {
        String encodedPassword = passwordEncodeService.encode(signUpDto.password());
        User user = signUpDto.toEntity(encodedPassword);
        userService.save(user);
    }

    @Transactional(readOnly = true)
    public TokenDto signIn(SignInDto signInDto) {
        User user = userService.readByUsername(signInDto.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!passwordEncodeService.matches(signInDto.password(), user.getPassword())) {
            throw new CustomException(UserErrorType.NOT_MATCHED_PASSWORD);
        }

        return jwtHelper.createToken(user);
    }
}
