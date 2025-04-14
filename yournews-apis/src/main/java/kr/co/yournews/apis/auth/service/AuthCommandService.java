package kr.co.yournews.apis.auth.service;

import jakarta.servlet.http.HttpServletResponse;
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
     * @return : jwt token
     */
    @Transactional
    public TokenDto signUp(SignUpDto.Auth signUpDto) {
        String encodedPassword = passwordEncodeService.encode(signUpDto.password());
        User user = signUpDto.toEntity(encodedPassword);
        userService.save(user);

        return jwtHelper.createToken(user);
    }

    /**
     * 서비스 이용을 위한 로그인 메서드
     *
     * @param signInDto : 사용자가 입력한 정보
     * @return : jwt token
     */
    @Transactional(readOnly = true)
    public TokenDto signIn(SignInDto signInDto) {
        User user = userService.readByUsername(signInDto.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!passwordEncodeService.matches(signInDto.password(), user.getPassword())) {
            throw new CustomException(UserErrorType.NOT_MATCHED_PASSWORD);
        }

        return jwtHelper.createToken(user);
    }

    /**
     * access token 재발급 메서드
     *
     * @param refreshToken : access token 재발급을 위한 refresh token
     * @return : jwt token
     */
    public TokenDto reissueAccessToken(String refreshToken) {
        return jwtHelper.reissueToken(refreshToken);
    }

    /**
     * 서비스 로그아웃 메서드
     *
     * @param refreshToken : refresh token
     * @param response     : 쿠키 제거용 HttpServletResponse
     */
    public void signOut(String refreshToken, HttpServletResponse response) {
        jwtHelper.removeToken(refreshToken, response);
    }
}
