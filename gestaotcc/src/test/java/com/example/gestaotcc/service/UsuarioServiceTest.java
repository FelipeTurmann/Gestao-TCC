package com.example.gestaotcc.service;

import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioEntity usuario;
    private UsuarioEntity usuarioExistente;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNome("Felipe Turmann");
        usuario.setEmail("felipe@email.com");
        usuario.setSenha("123456");
        usuario.setTipoUsuario(TipoUsuario.ALUNO);

        usuarioExistente = new UsuarioEntity();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Felipe Turmann Antigo");
        usuarioExistente.setEmail("felipe@email.com");
        usuarioExistente.setSenha("senhaAntiga");
        usuarioExistente.setTipoUsuario(TipoUsuario.ALUNO);
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void deveCadastrarUsuarioComSucesso() {
        // Given
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(usuario.getSenha())).thenReturn("senhaEncriptada");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuario);

        // When
        UsuarioEntity resultado = usuarioService.cadastrarUsuario(usuario);

        // Then
        assertNotNull(resultado);
        assertEquals(usuario.getNome(), resultado.getNome());
        assertEquals(usuario.getEmail(), resultado.getEmail());

        verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
        verify(passwordEncoder, times(1)).encode("123456");
        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals("senhaEncriptada", usuario.getSenha());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar usuário com email já existente")
    void deveLancarExcecaoAoTentarCadastrarUsuarioComEmailExistente() {
        // Given
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuarioExistente));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.cadastrarUsuario(usuario));

        assertEquals("Email já cadastrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso sem alterar senha")
    void deveAtualizarUsuarioComSucessoSemAlterarSenha() {
        // Given
        UsuarioEntity usuarioAtualizado = new UsuarioEntity();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Felipe Turmann Novo");
        usuarioAtualizado.setSenha(null); // Não alterando senha

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioExistente);

        // When
        UsuarioEntity resultado = usuarioService.atualizarUsuario(usuarioAtualizado);

        // Then
        assertNotNull(resultado);
        assertEquals("Felipe Turmann Novo", usuarioExistente.getNome());
        assertEquals("senhaAntiga", usuarioExistente.getSenha()); // Senha não foi alterada

        verify(usuarioRepository, times(1)).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso alterando senha")
    void deveAtualizarUsuarioComSucessoAlterandoSenha() {
        // Given
        UsuarioEntity usuarioAtualizado = new UsuarioEntity();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Felipe Turmann Novo");
        usuarioAtualizado.setSenha("novaSenha");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaEncriptada");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioExistente);

        // When
        UsuarioEntity resultado = usuarioService.atualizarUsuario(usuarioAtualizado);

        // Then
        assertNotNull(resultado);
        assertEquals("Felipe Turmann Novo", usuarioExistente.getNome());
        assertEquals("novaSenhaEncriptada", usuarioExistente.getSenha());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("novaSenha");
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso quando senha está vazia")
    void deveAtualizarUsuarioComSucessoQuandoSenhaEstaVazia() {
        // Given
        UsuarioEntity usuarioAtualizado = new UsuarioEntity();
        usuarioAtualizado.setId(1L);
        usuarioAtualizado.setNome("Felipe Turmann Novo");
        usuarioAtualizado.setSenha(""); // Senha vazia

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioExistente);

        // When
        UsuarioEntity resultado = usuarioService.atualizarUsuario(usuarioAtualizado);

        // Then
        assertNotNull(resultado);
        assertEquals("Felipe Turmann Novo", usuarioExistente.getNome());
        assertEquals("senhaAntiga", usuarioExistente.getSenha()); // Senha não foi alterada

        verify(usuarioRepository, times(1)).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void deveLancarExcecaoAoTentarAtualizarUsuarioInexistente() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.atualizarUsuario(usuario));

        assertEquals("Usuário não encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar orientadores com sucesso")
    void deveListarOrientadoresComSucesso() {
        // Given
        UsuarioEntity orientador1 = new UsuarioEntity();
        orientador1.setTipoUsuario(TipoUsuario.ORIENTADOR);

        UsuarioEntity orientador2 = new UsuarioEntity();
        orientador2.setTipoUsuario(TipoUsuario.ORIENTADOR);

        List<UsuarioEntity> orientadores = Arrays.asList(orientador1, orientador2);

        when(usuarioRepository.findByTipoUsuario(TipoUsuario.ORIENTADOR)).thenReturn(orientadores);

        // When
        List<UsuarioEntity> resultado = usuarioService.listarOrientadores();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(u -> u.getTipoUsuario() == TipoUsuario.ORIENTADOR));

        verify(usuarioRepository, times(1)).findByTipoUsuario(TipoUsuario.ORIENTADOR);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarUsuarioPorIdComSucesso() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // When
        Optional<UsuarioEntity> resultado = usuarioService.buscarPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(usuario.getId(), resultado.get().getId());
        assertEquals(usuario.getNome(), resultado.get().getNome());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar usuário por ID inexistente")
    void deveRetornarOptionalVazioAoBuscarUsuarioPorIdInexistente() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<UsuarioEntity> resultado = usuarioService.buscarPorId(1L);

        // Then
        assertFalse(resultado.isPresent());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void deveBuscarUsuarioPorEmailComSucesso() {
        // Given
        when(usuarioRepository.findByEmail("felipe@email.com")).thenReturn(Optional.of(usuario));

        // When
        Optional<UsuarioEntity> resultado = usuarioService.buscarPorEmail("felipe@email.com");

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(usuario.getEmail(), resultado.get().getEmail());
        assertEquals(usuario.getNome(), resultado.get().getNome());

        verify(usuarioRepository, times(1)).findByEmail("felipe@email.com");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar usuário por email inexistente")
    void deveRetornarOptionalVazioAoBuscarUsuarioPorEmailInexistente() {
        // Given
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        // When
        Optional<UsuarioEntity> resultado = usuarioService.buscarPorEmail("inexistente@email.com");

        // Then
        assertFalse(resultado.isPresent());

        verify(usuarioRepository, times(1)).findByEmail("inexistente@email.com");
    }

    @Test
    @DisplayName("Deve listar todos os usuários com sucesso")
    void deveListarTodosOsUsuariosComSucesso() {
        // Given
        UsuarioEntity usuario2 = new UsuarioEntity();
        usuario2.setId(2L);
        usuario2.setNome("Maria Silva");

        List<UsuarioEntity> usuarios = Arrays.asList(usuario, usuario2);

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // When
        List<UsuarioEntity> resultado = usuarioService.listarTodos();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deveDeletarUsuarioComSucesso() {
        // Given
        doNothing().when(usuarioRepository).deleteById(1L);

        // When
        usuarioService.deletarUsuario(1L);

        // Then
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve listar orientadores vazio quando não há orientadores")
    void deveListarOrientadoresVazioQuandoNaoHaOrientadores() {
        // Given
        when(usuarioRepository.findByTipoUsuario(TipoUsuario.ORIENTADOR)).thenReturn(Arrays.asList());

        // When
        List<UsuarioEntity> resultado = usuarioService.listarOrientadores();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository, times(1)).findByTipoUsuario(TipoUsuario.ORIENTADOR);
    }

    @Test
    @DisplayName("Deve listar todos os usuários vazio quando não há usuários")
    void deveListarTodosOsUsuariosVazioQuandoNaoHaUsuarios() {
        // Given
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<UsuarioEntity> resultado = usuarioService.listarTodos();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository, times(1)).findAll();
    }
}
