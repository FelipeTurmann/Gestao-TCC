package com.example.gestaotcc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaEntity entrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientador_id", nullable = false)
    private UsuarioEntity orientador;
}