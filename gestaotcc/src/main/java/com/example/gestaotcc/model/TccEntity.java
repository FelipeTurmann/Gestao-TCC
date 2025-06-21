package com.example.gestaotcc.model;

import com.example.gestaotcc.model.enuns.StatusTcc;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@Table(name = "tcc")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TccEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String resumo;

    @Column(nullable = false)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTcc status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private UsuarioEntity aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientador_id", nullable = false)
    private UsuarioEntity orientador;

    @OneToMany(mappedBy = "tcc", cascade = CascadeType.ALL)
    private List<EntregaEntity> entregas;

    @Column(nullable = false)
    private LocalDate dataCriacao;

    @Column(nullable = true)
    private LocalDate dataAtualizacao;
}
