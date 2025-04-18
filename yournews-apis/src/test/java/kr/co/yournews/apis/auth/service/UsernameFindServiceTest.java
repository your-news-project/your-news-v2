package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.UsernameDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UsernameFindServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UsernameFindService usernameFindService;

    @Test
    @DisplayName("아이디 찾기 테스트")
    void getUsernameByEmailTest() {
        // given
        String email = "test@email.com";
        String username = "testuser";
        UsernameDto.Request request = new UsernameDto.Request(email);

        User user = mock(User.class);
        given(userService.readByEmail(email)).willReturn(Optional.of(user));
        given(user.getUsername()).willReturn(username);

        // when
        UsernameDto.Response response = usernameFindService.getUsernameByEmail(request);

        // then
        assertEquals(username, response.username());
    }

    @Test
    @DisplayName("아이디 찾기 - 존재하지 않는 이메일")
    void getUsernameByEmailFailTest() {
        // given
        String email = "test@email.com";
        UsernameDto.Request request = new UsernameDto.Request(email);

        given(userService.readByEmail(email)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> usernameFindService.getUsernameByEmail(request));

        // then
        assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
    }
}
