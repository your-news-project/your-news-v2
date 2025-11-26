package kr.co.yournews.apis.menu.service;

import kr.co.yournews.apis.menu.dto.MenuImageRes;
import kr.co.yournews.domain.menu.type.MenuType;
import kr.co.yournews.infra.menu.storage.MenuPlace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuQueryService {

    /**
     * 이번주 학식 이미지 URL 조회
     * - 크롤링/저장은 따로 돌아가고 있다고 가정
     * - 여기서는 단순히 "어떤 URL로 접근하면 되는지"만 내려줌
     */
    public MenuImageRes getThisWeekMenus() {
        return MenuImageRes.builder()
                .inmunStaff(MenuPlace.INMUN_STAFF.toUrl(MenuType.MAIN_CAFETERIA))
                .studentHall(MenuPlace.STUDENT_HALL.toUrl(MenuType.MAIN_CAFETERIA))
                .naturalScience(MenuPlace.NATURAL_SCIENCE.toUrl(MenuType.MAIN_CAFETERIA))
                .dormitory(MenuPlace.DORMITORY.toUrl(MenuType.DORMITORY_MENU))
                .build();
    }
}
