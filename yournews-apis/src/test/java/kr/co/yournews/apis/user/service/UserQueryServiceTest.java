package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.UserRes;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserQueryService userQueryService;

    private final Long userId = 1L;

    @Test
    @DisplayName("사용자 조회 테스트")
    void getUserInfoByIdSuccess() {
        // given
        User mockUser = User.builder()
                .username("testuser")
                .nickname("테스트")
                .email("test@email.com")
                .build();

        ReflectionTestUtils.setField(mockUser, "id", userId);
        given(userService.readById(userId)).willReturn(Optional.of(mockUser));

        // when
        UserRes result = userQueryService.getUserInfoById(userId);

        // then
        assertEquals(mockUser.getId(), result.id());
        assertEquals(mockUser.getUsername(), result.username());
        assertEquals(mockUser.getNickname(), result.nickname());
        assertEquals(mockUser.getEmail(), result.email());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void getUserInfoByIdFailUserNotFound() {
        // given
        given(userService.readById(userId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> userQueryService.getUserInfoById(userId));

        // then
        assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
    }
}
