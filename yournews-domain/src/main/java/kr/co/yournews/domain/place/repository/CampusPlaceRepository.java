package kr.co.yournews.domain.place.repository;

import kr.co.yournews.domain.place.entity.CampusPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusPlaceRepository extends JpaRepository<CampusPlace, Long> {
}
