package kr.co.yournews.domain.place.service;

import kr.co.yournews.domain.place.entity.CampusPlace;
import kr.co.yournews.domain.place.repository.CampusPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusPlaceService {
    private final CampusPlaceRepository campusPlaceRepository;

    public List<CampusPlace> readAll() {
        return campusPlaceRepository.findAll();
    }
}
