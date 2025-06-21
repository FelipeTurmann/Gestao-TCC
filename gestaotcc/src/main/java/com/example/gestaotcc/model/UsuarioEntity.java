package com.example.gestaotcc.model;

import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    @OneToMany(mappedBy = "aluno")
    @ToString.Exclude
    @JsonIgnore
    private List<TccEntity> tccsComoAluno;

    @OneToMany(mappedBy = "orientador")
    @ToString.Exclude
    @JsonIgnore
    private List<TccEntity> tccsComoOrientador;

    @OneToMany(mappedBy = "orientador")
    @ToString.Exclude
    @JsonIgnore
    private List<AvaliacaoEntity> avaliacoes;
}


