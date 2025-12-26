package kr.co.yournews.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.domain.place.type.PlaceType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "campus_place")
public class CampusPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String area;

    @Column
    private String zone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type")
    private PlaceType placeType;

    @Builder
    public CampusPlace(
            String name, String area, String zone,
            Double latitude, Double longitude, PlaceType placeType
    ) {
        this.name = name;
        this.area = area;
        this.zone = zone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeType = placeType;
    }
}
