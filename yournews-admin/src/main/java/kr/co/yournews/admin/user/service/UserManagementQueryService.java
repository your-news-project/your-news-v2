package kr.co.yournews.admin.user.service;

import kr.co.yournews.admin.user.dto.UserRes;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementQueryService {
    private final UserService userService;
    private final SubNewsService subNewsService;

    /**
     * 전체 사용자 조회 메서드
     *
     * @return : 페이징 처리된 사용자 리스트
     */
    @Transactional(readOnly = true)
    public Page<UserRes.Summary> findAllUsers(Pageable pageable) {
        return userService.readAll(pageable)
                .map(UserRes.Summary::from);
    }

    /**
     * 특정 사용자 조회 메서드
     *
     * @param userId : 조회 대상 사용자 ID
     * @return : 조회된 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserRes.Details findUserById(Long userId) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        List<SubNewsQueryDto> subNewsDtos =
                subNewsService.readSubNewsWithKeywordsByUserId(userId);

        return UserRes.Details.from(user, subNewsDtos);
    }
}
