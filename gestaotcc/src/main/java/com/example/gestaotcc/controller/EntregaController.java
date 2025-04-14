package com.example.gestaotcc.controller;

import com.example.gestaotcc.config.UserPrincipal;
import com.example.gestaotcc.dto.EntregaDTO;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.service.EntregaService;
import com.example.gestaotcc.service.TccService;
import com.example.gestaotcc.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final TccService tccService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<EntregaDTO> cadastrarEntrega(@RequestParam("arquivo") MultipartFile arquivo,
                                                       @RequestParam("tccId") Long tccId,
                                                       @RequestParam("tipo") String tipo,
                                                       @RequestParam(value = "observacao", required = false) String observacao,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        TccEntity tcc = tccService.buscarPorId(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        EntregaEntity entrega = EntregaEntity.builder()
                .tcc(tcc)
                .tipo(TipoEntrega.valueOf(tipo))
                .observacao(observacao)
                .dataEnvio(LocalDateTime.now())
                .build();

        EntregaEntity novaEntrega = entregaService.cadastrarEntrega(entrega, arquivo, usuario);
        return new ResponseEntity<>(EntregaDTO.fromEntity(novaEntrega), HttpStatus.CREATED);
    }

    @GetMapping("/tcc/{tccId}")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorTcc(@PathVariable Long tccId,
                                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<EntregaEntity> entregas = entregaService.listarEntregasPorTcc(tccId, usuario);
        List<EntregaDTO> entregasDTO = entregas.stream()
                .map(EntregaDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(entregasDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaDTO> buscarEntrega(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<EntregaEntity> entrega = entregaService.buscarPorId(id);

        if (!entrega.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar se o usuário tem acesso a este TCC
        TccEntity tcc = entrega.get().getTcc();
        boolean temAcesso = usuario.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuario.getId()) ||
                tcc.getOrientador().getId().equals(usuario.getId());

        if (!temAcesso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(EntregaDTO.fromEntity(entrega.get()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEntrega(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        entregaService.deletarEntrega(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ultimaEntrega/{tccId}")
    public ResponseEntity<EntregaDTO> obterUltimaEntrega(@PathVariable Long tccId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se o usuário tem acesso a este TCC
        TccEntity tcc = tccService.buscarPorId(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        boolean temAcesso = usuario.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuario.getId()) ||
                tcc.getOrientador().getId().equals(usuario.getId());

        if (!temAcesso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<EntregaEntity> ultimaEntrega = entregaService.obterUltimaEntrega(tccId);

        if (ultimaEntrega.isPresent()) {
            return ResponseEntity.ok(EntregaDTO.fromEntity(ultimaEntrega.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tcc/{tccId}/tipo/{tipo}")
    public ResponseEntity<List<EntregaDTO>> listarEntregasPorTipoETcc(
            @PathVariable Long tccId,
            @PathVariable String tipo,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se o usuário tem acesso a este TCC
        TccEntity tcc = tccService.buscarPorId(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        boolean temAcesso = usuario.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuario.getId()) ||
                tcc.getOrientador().getId().equals(usuario.getId());

        if (!temAcesso) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EntregaEntity> entregas = entregaService.listarEntregasPorTipoETcc(tccId, tipo);
        List<EntregaDTO> entregasDTO = entregas.stream()
                .map(EntregaDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(entregasDTO);
    }
}