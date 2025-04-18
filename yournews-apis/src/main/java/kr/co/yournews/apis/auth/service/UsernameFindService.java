package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.UsernameDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsernameFindService {
    private final UserService userService;

    /**
     * 이메일을 통해 사용자의 아이디(username)를 조회하는 메서드
     *
     * @param usernameDto : 이메일 정보를 담은 요청 DTO
     * @return : username을 감싼 응답 DTO
     */
    @Transactional(readOnly = true)
    public UsernameDto.Response getUsernameByEmail(UsernameDto.Request usernameDto) {
        String username = userService.readByEmail(usernameDto.email())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND))
                .getUsername();

        return UsernameDto.Response.from(username);
    }
}
