package insty.global.security;

import insty.model.user.User;
import insty.model.user.UserType;
import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> user.getUserType().name());

        return authorities;
    }

    public Long getUserId() {
        return user.getId();
    }

    public UserType getUserType() {
        return user.getUserType();
    }

    // 계정이 만료되지 않는지 (true 여야 통과)
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    // 계정이 잠기지 않았는지 (true 여야 통과)
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    // 비밀번호가 만료되지 않았는지 (true 여야 통과)
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    // 계정이 활성화 되었는지 (true 여야 통과)
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}