package com.example.gestaotcc.controller;

import com.example.gestaotcc.config.UserPrincipal;
import com.example.gestaotcc.dto.UsuarioDTO;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioDTO> cadastrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        UsuarioEntity usuario = usuarioDTO.toEntity();
        UsuarioEntity novoUsuario = usuarioService.cadastrarUsuario(usuario);
        return new ResponseEntity<>(UsuarioDTO.fromEntity(novoUsuario), HttpStatus.CREATED);
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> obterPerfilUsuario(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UsuarioEntity usuario = usuarioService.buscarPorId(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return ResponseEntity.ok(UsuarioDTO.fromEntity(usuario));
    }

    @PutMapping("/atualizar")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@RequestBody UsuarioDTO usuarioDTO,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Apenas permite atualizar o próprio perfil
        if (!userPrincipal.getId().equals(usuarioDTO.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UsuarioEntity usuario = usuarioDTO.toEntity();
        UsuarioEntity usuarioAtualizado = usuarioService.atualizarUsuario(usuario);
        return ResponseEntity.ok(UsuarioDTO.fromEntity(usuarioAtualizado));
    }

    @GetMapping("/orientadores")
    public ResponseEntity<List<UsuarioDTO>> listarOrientadores() {
        List<UsuarioEntity> orientadores = usuarioService.listarOrientadores();
        List<UsuarioDTO> orientadoresDTO = orientadores.stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orientadoresDTO);
    }

    @GetMapping("listarTodosUsuarios")
    @PreAuthorize("hasRole('COORDENADOR')")
    public ResponseEntity<List<UsuarioDTO>> listarTodosUsuarios() {
        List<UsuarioEntity> usuarios = usuarioService.listarTodos();
        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADOR')")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}