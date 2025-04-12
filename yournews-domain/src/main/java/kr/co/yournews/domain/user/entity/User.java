package kr.co.yournews.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
@Entity(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
}
