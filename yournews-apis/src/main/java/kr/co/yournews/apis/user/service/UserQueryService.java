package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.UserRes;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserService userService;
    private final SubNewsService subNewsService;

    /**
     * 사용자의 정보를 조회하는 메서드
     *
     * @param userId : 사용자 PK 값
     * @return 사용자의 정보가 담긴 dto
     */
    @Transactional(readOnly = true)
    public UserRes.Info getUserInfoById(Long userId) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        List<SubNewsQueryDto> subNewsDtos =
                subNewsService.readSubNewsWithKeywordsByUserId(userId);

        return UserRes.Info.from(user, subNewsDtos);
    }

    @Transactional(readOnly = true)
    public UserRes.NotificationStatus getUserNotificationStatus(Long userId) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        return UserRes.NotificationStatus.from(user);
    }
}
