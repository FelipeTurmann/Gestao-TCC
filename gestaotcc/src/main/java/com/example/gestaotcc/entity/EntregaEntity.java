package com.example.gestaotcc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Entrega")
public class EntregaEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private enums.TipoEntrega tipo; // PARCIAL, FINAL

    private LocalDateTime dataEnvio;

    private String arquivo; // caminho ou link

    @Lob
    private String observacao;

    @ManyToOne
    private TccEntity tcc;
}
