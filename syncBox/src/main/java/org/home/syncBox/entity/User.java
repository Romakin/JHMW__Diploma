package org.home.syncBox.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "t_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, message = "Min 5 characters")
    @Column(unique=true)
    private String username;

    @Size(min = 3, message = "Min 5 characters")
    private String password;

    @Transient
    private String passwordConfirm;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isExpired;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isLocked;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isEnabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (Role role: roles)
            authorities.add(new SimpleGrantedAuthority(role.getName()));

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isExpired == false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isLocked == false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
