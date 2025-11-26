package kr.co.yournews.apis.menu.dto;

import lombok.Builder;

@Builder
public record MenuImageRes(
        String inmunStaff,      // 인문계 교직원식당
        String studentHall,     // 학생회관 식당
        String naturalScience,  // 자연계 식당
        String dormitory        // 기숙사 식당
) {
}
