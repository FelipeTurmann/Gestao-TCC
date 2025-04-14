package com.example.gestaotcc.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
@Builder
@Data
public class AvaliacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double nota;

    @Column(nullable = false, length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime dataAvaliacao;

    @ManyToOne
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaEntity entrega;

    @ManyToOne
    @JoinColumn(name = "orientador_id", nullable = false)
    private UsuarioEntity orientador;
}