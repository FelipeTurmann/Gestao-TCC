package com.example.gestaotcc.controller;

import com.example.gestaotcc.config.UserPrincipal;
import com.example.gestaotcc.dto.AvaliacaoDTO;
import com.example.gestaotcc.model.AvaliacaoEntity;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.service.AvaliacaoService;
import com.example.gestaotcc.service.EntregaService;
import com.example.gestaotcc.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final EntregaService entregaService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<AvaliacaoDTO> cadastrarAvaliacao(@RequestBody AvaliacaoDTO avaliacaoDTO,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity orientador = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EntregaEntity entrega = entregaService.buscarPorId(avaliacaoDTO.getEntregaId())
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        AvaliacaoEntity avaliacao = AvaliacaoEntity.builder()
                .nota(avaliacaoDTO.getNota())
                .comentario(avaliacaoDTO.getComentario())
                .dataAvaliacao(LocalDateTime.now())
                .entrega(entrega)
                .orientador(orientador)
                .build();

        AvaliacaoEntity novaAvaliacao = avaliacaoService.cadastrarAvaliacao(avaliacao, orientador);
        return new ResponseEntity<>(AvaliacaoDTO.fromEntity(novaAvaliacao), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoDTO> atualizarAvaliacao(@PathVariable Long id,
                                                           @RequestBody AvaliacaoDTO avaliacaoDTO,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (!id.equals(avaliacaoDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        UsuarioEntity orientador = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EntregaEntity entrega = entregaService.buscarPorId(avaliacaoDTO.getEntregaId())
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        AvaliacaoEntity avaliacao = avaliacaoDTO.toEntity(entrega, orientador);
        AvaliacaoEntity avaliacaoAtualizada = avaliacaoService.atualizarAvaliacao(avaliacao, orientador);

        return ResponseEntity.ok(AvaliacaoDTO.fromEntity(avaliacaoAtualizada));
    }

    @GetMapping("/entrega/{entregaId}")
    public ResponseEntity<List<AvaliacaoDTO>> listarAvaliacoesPorEntrega(@PathVariable Long entregaId,
                                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<AvaliacaoEntity> avaliacoes = avaliacaoService.listarAvaliacoesPorEntrega(entregaId, usuario);
        List<AvaliacaoDTO> avaliacoesDTO = avaliacoes.stream()
                .map(AvaliacaoDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(avaliacoesDTO);
    }

    @GetMapping("/orientador/{orientadorId}")
    public ResponseEntity<List<AvaliacaoDTO>> listarAvaliacoesPorOrientador(@PathVariable Long orientadorId,
                                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Verificar se o usuário solicitante é o próprio orientador ou um coordenador
        if (!userPrincipal.getId().equals(orientadorId) &&
                userPrincipal.getTipoUsuario() != TipoUsuario.COORDENADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AvaliacaoEntity> avaliacoes = avaliacaoService.listarAvaliacoesPorOrientador(orientadorId);
        List<AvaliacaoDTO> avaliacoesDTO = avaliacoes.stream()
                .map(AvaliacaoDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(avaliacoesDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoDTO> buscarAvaliacao(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        AvaliacaoEntity avaliacao = avaliacaoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        // Verificar se o usuário tem acesso a esta avaliação
        EntregaEntity entrega = avaliacao.getEntrega();
        TccEntity tcc = entrega.getTcc();

        boolean temAcesso = usuario.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuario.getId()) ||
                tcc.getOrientador().getId().equals(usuario.getId());

        if (!temAcesso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(AvaliacaoDTO.fromEntity(avaliacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAvaliacao(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        avaliacaoService.deletarAvaliacao(id, usuario);
        return ResponseEntity.noContent().build();
    }
}