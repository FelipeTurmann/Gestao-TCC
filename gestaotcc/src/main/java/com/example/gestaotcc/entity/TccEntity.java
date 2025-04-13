package com.example.gestaotcc.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "tcc")
@Data
@Builder
public class TccEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String titulo;

    private String resumo;

    @Enumerated(EnumType.STRING)
    private enums.StatusTcc status; // PROPOSTA, EM_ANDAMENTO, CONCLUIDO, REPROVADO

    @ManyToOne
    private UsuarioEntity aluno;

    @ManyToOne
    private UsuarioEntity orientador;
}
