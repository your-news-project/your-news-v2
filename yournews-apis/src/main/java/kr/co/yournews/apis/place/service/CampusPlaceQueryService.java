package kr.co.yournews.apis.place.service;

import kr.co.yournews.apis.place.dto.CampusPlaceRes;
import kr.co.yournews.domain.place.service.CampusPlaceService;
import kr.co.yournews.domain.place.type.PlaceType;
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
     * @param placeType : 위치 정보 타입
     *                  BUILDING - 건물 위치
     *                  BUS_ROUTE - 버스 노선
     * @return : 캠퍼스 위치 정보
     */
    @Transactional(readOnly = true)
    public List<CampusPlaceRes> getCampusPlaceByPlaceType(PlaceType placeType) {
        return campusPlaceService.readByPlaceType(placeType)
                .stream().map(CampusPlaceRes::from).toList();
    }
}
