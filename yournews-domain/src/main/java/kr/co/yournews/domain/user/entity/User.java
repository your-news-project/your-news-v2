package kr.co.yournews.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.user.type.OAuthPlatform;
import kr.co.yournews.domain.user.type.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
@Entity(name = "user")
public class User extends BaseTimeEntity {

    public static final String UNKNOWN_NICKNAME = "알 수 없음";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private OAuthPlatform platform;

    @JsonIgnore
    @ColumnDefault("NULL")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ColumnDefault("true")
    @Column(name = "signed_up", nullable = false)
    private boolean signedUp;

    @ColumnDefault("false")
    @Column(name = "sub_status")
    private boolean subStatus;

    @Builder
    public User(String username, String password, String nickname, String email,
                Role role, OAuthPlatform platform, boolean signedUp, boolean subStatus) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.platform = platform;
        this.signedUp = signedUp;
        this.subStatus = subStatus;
    }

    public void updateInfo(String nickname, boolean subStatus) {
        this.nickname = nickname;
        this.signedUp = true;
        this.subStatus = subStatus;
        this.role = Role.USER;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
