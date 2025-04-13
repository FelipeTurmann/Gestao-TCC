package com.example.gestaotcc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String nome;

    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    private enums.TipoUsuario tipoUsuario; // ALUNO, ORIENTADOR, COORDENADOR
}
