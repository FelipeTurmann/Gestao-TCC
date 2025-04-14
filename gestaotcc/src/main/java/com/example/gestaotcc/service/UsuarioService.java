package com.example.gestaotcc.service;

import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioEntity cadastrarUsuario(UsuarioEntity usuario) {
        // Validação de dados
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criptografar senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return usuarioRepository.save(usuario);
    }

    public UsuarioEntity atualizarUsuario(UsuarioEntity usuario) {
        UsuarioEntity usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuarioExistente.setNome(usuario.getNome());

        // Se estiver atualizando a senha
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public List<UsuarioEntity> listarOrientadores() {
        return usuarioRepository.findByTipoUsuario(TipoUsuario.ORIENTADOR);
    }

    public Optional<UsuarioEntity> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioEntity> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<UsuarioEntity> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void deletarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}