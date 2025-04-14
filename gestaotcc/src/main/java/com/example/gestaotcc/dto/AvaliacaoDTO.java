package com.example.gestaotcc.dto;

import com.example.gestaotcc.model.AvaliacaoEntity;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoDTO {

    private Long id;

    private Double nota;

    private String comentario;

    private LocalDateTime dataAvaliacao;

    private Long entregaId;

    private Long orientadorId;

    private String orientadorNome;

    
    public static AvaliacaoDTO fromEntity(AvaliacaoEntity avaliacao) {
        return AvaliacaoDTO.builder()
                .id(avaliacao.getId())
                .nota(avaliacao.getNota())
                .comentario(avaliacao.getComentario())
                .dataAvaliacao(avaliacao.getDataAvaliacao())
                .entregaId(avaliacao.getEntrega().getId())
                .orientadorId(avaliacao.getOrientador().getId())
                .orientadorNome(avaliacao.getOrientador().getNome())
                .build();
    }

    public AvaliacaoEntity toEntity(EntregaEntity entrega, UsuarioEntity orientador) {
        return AvaliacaoEntity.builder()
                .id(this.id)
                .nota(this.nota)
                .comentario(this.comentario)
                .dataAvaliacao(this.dataAvaliacao)
                .entrega(entrega)
                .orientador(orientador)
                .build();
    }
}