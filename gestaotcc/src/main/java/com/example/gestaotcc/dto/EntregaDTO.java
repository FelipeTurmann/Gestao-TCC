package com.example.gestaotcc.dto;


import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaDTO {

    private Long id;

    private TipoEntrega tipo;

    private LocalDateTime dataEnvio;

    private String observacao;

    private Long tccId;

    private String tituloTcc;

    private String caminhoArquivo;

    public static EntregaDTO fromEntity(EntregaEntity entrega) {
        return EntregaDTO.builder()
                .id(entrega.getId())
                .tipo(entrega.getTipo())
                .dataEnvio(entrega.getDataEnvio())
                .observacao(entrega.getObservacao())
                .tccId(entrega.getTcc().getId())
                .tituloTcc(entrega.getTcc().getTitulo())
                .caminhoArquivo(entrega.getCaminhoArquivo())
                .build();
    }

    public EntregaEntity toEntity(TccEntity tcc) {
        return EntregaEntity.builder()
                .id(this.id)
                .tipo(this.tipo)
                .dataEnvio(this.dataEnvio)
                .observacao(this.observacao)
                .tcc(tcc)
                .caminhoArquivo(this.caminhoArquivo)
                .build();
    }
}