package kr.co.yournews.apis.studentservice.event;

import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;

import java.util.List;

public record StudentServiceRegisteredEvent(
        Long studentServiceId,
        String name,
        String description,
        List<String> serviceUrls,
        StudentServiceContentType contentType,
        String registrantNickname,
        String registrantEmail,
        int imageCount
) {
}
