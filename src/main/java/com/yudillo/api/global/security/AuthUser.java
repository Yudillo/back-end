package com.yudillo.api.global.security;

import com.yudillo.api.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SecurityContext 에 담기는 인증 주체. 컨트롤러에서 @CurrentUser 로 주입받아 쓴다.
 */
@Getter
public class AuthUser implements UserDetails {

    private final UUID id;
    private final String email;
    private final String name;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private AuthUser(UUID id, String email, String name, String password, String authority) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority(authority));
    }

    public static AuthUser from(User user) {
        return new AuthUser(user.getId(), user.getEmail(), user.getName(),
                user.getPassword(), user.getRole().authority());
    }

    @Override
    public String getUsername() {
        return email;
    }
}
