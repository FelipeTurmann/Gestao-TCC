package com.example.gestaotcc.config;

import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {
    @Getter
    private Long id;
    private String nome;
    private String email;
    @JsonIgnore
    private String senha;
    @Getter
    private TipoUsuario tipoUsuario;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UsuarioEntity usuario) {
        // Criar uma autoridade baseada no tipo de usuário
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name())
        );

        return UserPrincipal.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .senha(usuario.getSenha())
                .tipoUsuario(usuario.getTipoUsuario())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
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