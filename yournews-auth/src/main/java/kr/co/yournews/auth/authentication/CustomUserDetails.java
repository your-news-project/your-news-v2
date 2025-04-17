package kr.co.yournews.auth.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUserDetails implements UserDetails {
    private Long userId;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(Long userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.authorities = List.of(new CustomGrantedAuthority(role.getValue()));
    }

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
