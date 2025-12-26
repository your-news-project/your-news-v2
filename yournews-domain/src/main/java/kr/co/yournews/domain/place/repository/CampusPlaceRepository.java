package kr.co.yournews.domain.place.repository;

import kr.co.yournews.domain.place.entity.CampusPlace;
import kr.co.yournews.domain.place.type.PlaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusPlaceRepository extends JpaRepository<CampusPlace, Long> {
    List<CampusPlace> findByPlaceType(PlaceType placeType);
}
