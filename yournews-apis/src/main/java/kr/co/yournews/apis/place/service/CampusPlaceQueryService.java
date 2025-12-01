package kr.co.yournews.apis.place.service;

import kr.co.yournews.apis.place.dto.CampusPlaceRes;
import kr.co.yournews.domain.place.service.CampusPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusPlaceQueryService {
    private final CampusPlaceService campusPlaceService;

    /**
     * 캠퍼스 위치 정보 조회 메서드
     *
     * @return : 캠퍼스 위치 정보
     */
    @Transactional(readOnly = true)
    public List<CampusPlaceRes> getAllCampusPlace() {
        return campusPlaceService.readAll()
                .stream().map(CampusPlaceRes::from).toList();
    }
}
