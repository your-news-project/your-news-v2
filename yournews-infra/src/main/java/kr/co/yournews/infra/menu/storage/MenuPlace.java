package kr.co.yournews.infra.menu.storage;

import kr.co.yournews.domain.menu.type.MenuType;
import lombok.RequiredArgsConstructor;

/**
 * 식당 위치 Enum
 */
@RequiredArgsConstructor
public enum MenuPlace {
    INMUN_STAFF("inmun_staff.png"),
    STUDENT_HALL("student_hall.png"),
    NATURAL_SCIENCE("natural_science.png"),
    DORMITORY("dormitory.png"),
    ETC("etc.png");

    private final String fileName;

    public String toFileName() {
        return fileName;
    }

    public String toUrl(MenuType menuType) {
        return "/menus/" + menuType.name().toLowerCase() + "/" + fileName;
    }
}
