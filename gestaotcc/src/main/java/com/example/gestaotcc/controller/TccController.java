package com.example.gestaotcc.controller;

import com.example.gestaotcc.config.UserPrincipal;
import com.example.gestaotcc.dto.TccDTO;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.service.TccService;
import com.example.gestaotcc.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tccs")
@RequiredArgsConstructor
public class TccController {

    private final TccService tccService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<TccDTO> cadastrarTcc(@RequestBody TccDTO tccDTO,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal.getTipoUsuario() != TipoUsuario.ALUNO) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UsuarioEntity aluno = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        UsuarioEntity orientador = usuarioService.buscarPorId(tccDTO.getOrientadorId())
                .orElseThrow(() -> new RuntimeException("Orientador não encontrado"));

        TccEntity tcc = tccDTO.toEntity(aluno, orientador);
        tcc.setDataCriacao(LocalDate.now());

        TccEntity novoTcc = tccService.cadastrarPropostaTcc(tcc);
        return new ResponseEntity<>(TccDTO.fromEntity(novoTcc), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<TccDTO> atualizarStatusTcc(@PathVariable Long id,
                                                     @PathVariable StatusTcc status,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        TccEntity tccAtualizado = tccService.atualizarStatusTcc(id, status, usuario);
        return ResponseEntity.ok(TccDTO.fromEntity(tccAtualizado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TccDTO> atualizarTcc(@PathVariable Long id,
                                               @RequestBody TccDTO tccDTO,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!id.equals(tccDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        UsuarioEntity aluno = usuarioService.buscarPorId(tccDTO.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        UsuarioEntity orientador = usuarioService.buscarPorId(tccDTO.getOrientadorId())
                .orElseThrow(() -> new RuntimeException("Orientador não encontrado"));

        TccEntity tcc = tccDTO.toEntity(aluno, orientador);
        TccEntity tccAtualizado = tccService.atualizarTcc(tcc, usuario);

        return ResponseEntity.ok(TccDTO.fromEntity(tccAtualizado));
    }

    @GetMapping
    public ResponseEntity<List<TccDTO>> listarTccs(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TccEntity> tccs;

        if (userPrincipal.getTipoUsuario() == TipoUsuario.ALUNO) {
            tccs = tccService.listarTccsPorAluno(userPrincipal.getId());
        } else if (userPrincipal.getTipoUsuario() == TipoUsuario.ORIENTADOR) {
            tccs = tccService.listarTccsPorOrientador(userPrincipal.getId());
        } else { // COORDENADOR
            tccs = tccService.listarTodosTccs();
        }

        List<TccDTO> tccsDTO = tccs.stream()
                .map(TccDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tccsDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TccDTO> obterTcc(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TccEntity tcc = tccService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Verificar permissão
        boolean temPermissao = userPrincipal.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(userPrincipal.getId()) ||
                tcc.getOrientador().getId().equals(userPrincipal.getId());

        if (!temPermissao) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(TccDTO.fromEntity(tcc));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TccDTO>> listarTccsPorStatus(@PathVariable StatusTcc status,
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TccEntity> tccs;

        if (userPrincipal.getTipoUsuario() == TipoUsuario.ALUNO) {
            UsuarioEntity aluno = usuarioService.buscarPorId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
            tccs = tccService.listarTccsPorAlunoEStatus(aluno, status);
        } else if (userPrincipal.getTipoUsuario() == TipoUsuario.ORIENTADOR) {
            UsuarioEntity orientador = usuarioService.buscarPorId(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Orientador não encontrado"));
            tccs = tccService.listarTccsPorOrientadorEStatus(orientador, status);
        } else { // COORDENADOR
            tccs = tccService.listarTccsPorStatus(status);
        }

        List<TccDTO> tccsDTO = tccs.stream()
                .map(TccDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tccsDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTcc(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        tccService.deletarTcc(id, usuario);
        return ResponseEntity.noContent().build();
    }
}