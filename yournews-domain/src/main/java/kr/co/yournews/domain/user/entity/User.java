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
import kr.co.yournews.domain.user.type.UserStatus;
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

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private OAuthPlatform platform;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "ban_reason")
    private String banReason;

    @JsonIgnore
    @ColumnDefault("NULL")
    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

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

    @ColumnDefault("false")
    @Column(name = "daily_sub_status")
    private boolean dailySubStatus;

    @ColumnDefault("false")
    @Column(name = "calendar_sub_status")
    private boolean calendarSubStatus;

    @Builder
    public User(
            String username, String password, String nickname,
            String email, Role role, OAuthPlatform platform,
            UserStatus status, boolean signedUp, boolean subStatus,
            boolean dailySubStatus, boolean calendarSubStatus
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.platform = platform;
        this.status = status;
        this.signedUp = signedUp;
        this.subStatus = subStatus;
        this.dailySubStatus = dailySubStatus;
        this.calendarSubStatus = calendarSubStatus;
    }

    public void updateInfo(
            String nickname, boolean subStatus,
            boolean dailySubStatus, boolean calendarSubStatus
    ) {
        this.nickname = nickname;
        this.signedUp = true;
        this.subStatus = subStatus;
        this.dailySubStatus = dailySubStatus;
        this.calendarSubStatus = calendarSubStatus;
        this.role = Role.USER;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateInfo(String nickname) {
        this.nickname = nickname;
    }

    public void updateSubStatus(boolean subStatus, boolean dailySubStatus) {
        this.subStatus = subStatus;
        this.dailySubStatus = dailySubStatus;
    }

    public void updateSubStatus(boolean subStatus) {
        this.subStatus = subStatus;
    }

    public void updateDailySubStatus(boolean dailySubStatus) {
        this.dailySubStatus = dailySubStatus;
    }

    public void updateCalendarSubStatus(boolean calendarSubStatus) {
        this.calendarSubStatus = calendarSubStatus;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isOauthUser() {
        return this.platform != null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    public void ban(String reason) {
        this.status = UserStatus.BANNED;
        this.banReason = reason;
        this.bannedAt = LocalDateTime.now();
    }

    public void unban() {
        this.status = UserStatus.ACTIVE;
        this.banReason = null;
        this.bannedAt = null;
    }
}
