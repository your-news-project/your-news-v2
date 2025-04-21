package kr.co.yournews.apis.auth.service;

import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserValidationService {
    private final UserService userService;

    /**
     * 주어진 아이디가 이미 존재하는지 확인
     *
     * @param username : 중복 여부를 확인할 사용자명
     * @return : true - 이미 존재함, false - 사용 가능함
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userService.existsByUsername(username);
    }

    /**
     * 주어진 닉네임이 이미 존재하는지 확인
     *
     * @param nickname : 중복 여부를 확인할 닉네임
     * @return : true - 이미 존재함, false - 사용 가능함
     */
    @Transactional(readOnly = true)
    public boolean isNicknameExists(String nickname) {
        return userService.existsByNickname(nickname);
    }
}
