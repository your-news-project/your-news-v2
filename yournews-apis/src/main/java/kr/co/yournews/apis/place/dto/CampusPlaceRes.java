package kr.co.yournews.apis.place.dto;

import kr.co.yournews.domain.place.entity.CampusPlace;

public record CampusPlaceRes(
        String name,
        String area,
        String zone,
        Double latitude,
        Double longitude
) {
    public static CampusPlaceRes from(CampusPlace campusPlace) {
        return new CampusPlaceRes(
                campusPlace.getName(),
                campusPlace.getArea(),
                campusPlace.getZone(),
                campusPlace.getLatitude(),
                campusPlace.getLongitude()
        );
    }
}
