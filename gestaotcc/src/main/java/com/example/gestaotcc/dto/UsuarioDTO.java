package com.example.gestaotcc.dto;

import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    private String nome;

    private String email;

    private String senha;

    private TipoUsuario tipoUsuario;

    public static UsuarioDTO fromEntity(UsuarioEntity usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipoUsuario(usuario.getTipoUsuario())
                .build();
    }

    public UsuarioEntity toEntity() {
        return UsuarioEntity.builder()
                .id(this.id)
                .nome(this.nome)
                .email(this.email)
                .senha(this.senha)
                .tipoUsuario(this.tipoUsuario)
                .build();
    }
}