package com.example.gestaotcc.model;

import com.example.gestaotcc.model.enuns.TipoEntrega;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Entregas")
@Builder
@Data
public class EntregaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEntrega tipo;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Column(length = 500)
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "tcc_id", nullable = false)
    private TccEntity tcc;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL)

    private List<AvaliacaoEntity> avaliacoes;

    @Column(nullable = false)
    private String caminhoArquivo;
}