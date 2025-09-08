package kr.co.yournews.apis.notification.controller;

import kr.co.yournews.apis.notification.dto.NotificationDto;
import kr.co.yournews.apis.notification.dto.NotificationRankingDto;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import kr.co.yournews.apis.notification.service.NotificationQueryService;
import kr.co.yournews.apis.notification.service.NotificationRankingService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.type.NotificationType;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationCommandService notificationCommandService;

    @MockBean
    private NotificationQueryService notificationQueryService;

    @MockBean
    private NotificationRankingService notificationRankingService;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;
    private final Long notificationId = 1L;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();


        user = User.builder()
                .username("test")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
    }

    @Nested
    @DisplayName("특정 알림 조회 테스트")
    class GetNotificationTest {

        private final String publicId = "public-id";
        private final NotificationDto.Details details = new NotificationDto.Details(
                notificationId, "공지", List.of("제목"), List.of("url"), false, NotificationType.IMMEDIATE, LocalDateTime.now()
        );

        @Test
        @DisplayName("ID를 통한 조회 성공")
        void getNotificationByIdSuccess() throws Exception {
            // given
            given(notificationQueryService.getNotificationById(notificationId)).willReturn(details);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies/id/{notificationId}", notificationId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newsName").value("공지"))
                    .andExpect(jsonPath("$.data.postTitle").value("제목"));
        }

        @Test
        @DisplayName("ID를 통한 조회 실패 - 알림 없음")
        void getNotificationByIdFailNotFound() throws Exception {
            // given
            given(notificationQueryService.getNotificationById(notificationId))
                    .willThrow(new CustomException(NotificationErrorType.NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies/id/{notificationId}", notificationId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(NotificationErrorType.NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message").value(NotificationErrorType.NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("공동 ID를 통한 조회 성공")
        void getNotificationByPublicIdSuccess() throws Exception {
            // given
            given(notificationQueryService.getNotificationByPublicId(userId, publicId)).willReturn(details);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies/public-id/{publicId}", publicId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newsName").value("공지"))
                    .andExpect(jsonPath("$.data.postTitle").value("제목"));
        }

        @Test
        @DisplayName("공동 ID를 통한 조회 실패 - 알림 없음")
        void getNotificationByPublicIdFailNotificationNotFound() throws Exception {
            // given
            given(notificationQueryService.getNotificationByPublicId(userId, publicId))
                    .willThrow(new CustomException(NotificationErrorType.NOT_FOUND));

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies/public-id/{publicId}", publicId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(NotificationErrorType.NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message").value(NotificationErrorType.NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("전체 알림 조회 테스트")
    class GetAllNotificationTest {

        @Test
        @DisplayName("알림 목록 조회 - 읽음 여부 필터 (isRead=true)")
        void getNotificationsByIsReadSuccess() throws Exception {
            // given
            Page<NotificationDto.Summary> page = new PageImpl<>(List.of(
                    new NotificationDto.Summary(2L, "공지", true, NotificationType.IMMEDIATE, LocalDateTime.now())
            ));

            given(notificationQueryService.getNotificationsByUserIdAndIsRead(eq(userId), eq(true), any(Pageable.class)))
                    .willReturn(page);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies")
                            .param("isRead", "true")
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].isRead").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("알림 목록 조회 - 특정 소식 필터링")
        void getNotificationByNewsNameSuccess() throws Exception {
            // given
            String newsName = "특정 소식";
            Page<NotificationDto.Summary> page = new PageImpl<>(List.of(
                    new NotificationDto.Summary(2L, newsName, true, NotificationType.IMMEDIATE, LocalDateTime.now())
            ));

            given(notificationQueryService.getNotificationsByUserIdAndNewsNameAndIsRead(eq(userId), eq(newsName), eq(true), any(Pageable.class)))
                    .willReturn(page);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies")
                            .param("isRead", "true")
                            .param("newsName", newsName)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].isRead").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("알림 목록 조회 - 현재 구독 중이지 않는 소식 알림 조회")
        void getNotificationByNewsNameNotInSuccess() throws Exception {
            // given
            String newsName = "구독 중이지 않은 소식";
            Page<NotificationDto.Summary> page = new PageImpl<>(List.of(
                    new NotificationDto.Summary(2L, newsName, true, NotificationType.IMMEDIATE, LocalDateTime.now())
            ));

            given(notificationQueryService.getNotificationsByUserIdAndNewsNameNotInAndIsRead(eq(userId), eq(true), any(Pageable.class)))
                    .willReturn(page);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/notifies")
                            .param("isRead", "true")
                            .param("others", "true")
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].isRead").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }
    }

    @Nested
    @DisplayName("알림 삭제 테스트")
    class DeleteNotificationTest {

        @Test
        @DisplayName("성공")
        void deleteNotificationSuccess() throws Exception {
            // given
            doNothing().when(notificationCommandService).deleteNotification(userId, notificationId);

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/notifies/{notificationId}", notificationId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("알림 삭제 실패 - 권한 없음")
        void deleteNotification_fail_forbidden() throws Exception {
            // given
            willThrow(new CustomException(NotificationErrorType.FORBIDDEN))
                    .given(notificationCommandService).deleteNotification(userId, notificationId);

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/notifies/{notificationId}", notificationId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(jsonPath("$.code").value(NotificationErrorType.FORBIDDEN.getCode()))
                    .andExpect(jsonPath("$.message").value(NotificationErrorType.FORBIDDEN.getMessage()));
        }
    }

    @Test
    @DisplayName("일간 소식 랭킹 조회")
    void getTopNewsRankingTest() throws Exception {
        // given
        List<NotificationRankingDto> rankingList = List.of(
                new NotificationRankingDto("소식1", 10),
                new NotificationRankingDto("소식2", 5),
                new NotificationRankingDto("소식3", 3)
        );

        given(notificationRankingService.getNewsRanking()).willReturn(rankingList);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/notifies/rank")
                        .with(user(userDetails))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(jsonPath("$.data.size()").value(rankingList.size()));
    }
}
