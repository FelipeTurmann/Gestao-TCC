package com.example.gestaotcc.entity;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

import javax.naming.Name;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
public class AvaliacaoEntity {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal nota;

    private String comentario;

    private LocalDateTime dataAvaliacao;

    @ManyToOne
    private EntregaEntity entrega;

    @ManyToOne
    private UsuarioEntity orientador;
}