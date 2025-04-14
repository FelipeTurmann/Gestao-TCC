package com.example.gestaotcc.dto;

import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TccDTO {

    private Long id;

    private String titulo;

    private String resumo;

    private String area;

    private StatusTcc status;

    private Long alunoId;

    private String alunoNome;

    private Long orientadorId;

    private String orientadorNome;

    private LocalDate dataCriacao;

    private LocalDate dataAtualizacao;

    public static TccDTO fromEntity(TccEntity tcc) {
        return TccDTO.builder()
                .id(tcc.getId())
                .titulo(tcc.getTitulo())
                .resumo(tcc.getResumo())
                .area(tcc.getArea())
                .status(tcc.getStatus())
                .alunoId(tcc.getAluno().getId())
                .alunoNome(tcc.getAluno().getNome())
                .orientadorId(tcc.getOrientador().getId())
                .orientadorNome(tcc.getOrientador().getNome())
                .dataCriacao(tcc.getDataCriacao())
                .dataAtualizacao(tcc.getDataAtualizacao())
                .build();
    }

    public TccEntity toEntity(UsuarioEntity aluno, UsuarioEntity orientador) {
        return TccEntity.builder()
                .id(this.id)
                .titulo(this.titulo)
                .resumo(this.resumo)
                .area(this.area)
                .status(this.status)
                .aluno(aluno)
                .orientador(orientador)
                .dataCriacao(this.dataCriacao)
                .dataAtualizacao(this.dataAtualizacao)
                .build();
    }
}