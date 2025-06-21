package com.example.gestaotcc.service;

import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.TccRepository;
import com.example.gestaotcc.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do TccService")
class TccServiceTest {

    @Mock
    private TccRepository tccRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TccService tccService;

    private TccEntity tcc;
    private UsuarioEntity aluno;
    private UsuarioEntity orientador;
    private UsuarioEntity coordenador;
    private UsuarioEntity usuarioComum;

    @BeforeEach
    void setUp() {
        // Aluno
        aluno = new UsuarioEntity();
        aluno.setId(1L);
        aluno.setNome("Felipe Silva");
        aluno.setEmail("felipe@email.com");
        aluno.setTipoUsuario(TipoUsuario.ALUNO);

        // Orientador
        orientador = new UsuarioEntity();
        orientador.setId(2L);
        orientador.setNome("Prof. Felipe Santos");
        orientador.setEmail("prof.felipe@email.com");
        orientador.setTipoUsuario(TipoUsuario.ORIENTADOR);

        // Coordenador
        coordenador = new UsuarioEntity();
        coordenador.setId(3L);
        coordenador.setNome("Dr. Felipe Oliveira");
        coordenador.setEmail("coord.felipe@email.com");
        coordenador.setTipoUsuario(TipoUsuario.COORDENADOR);

        // Usuário comum (não orientador)
        usuarioComum = new UsuarioEntity();
        usuarioComum.setId(4L);
        usuarioComum.setNome("Felipe Comum");
        usuarioComum.setEmail("felipe.comum@email.com");
        usuarioComum.setTipoUsuario(TipoUsuario.ALUNO);

        // TCC
        tcc = new TccEntity();
        tcc.setId(1L);
        tcc.setTitulo("Sistema de Gestão de TCC");
        tcc.setResumo("Resumo do TCC");
        tcc.setArea("Ciência da Computação");
        tcc.setAluno(aluno);
        tcc.setOrientador(orientador);
        tcc.setStatus(StatusTcc.EM_ANDAMENTO);
        tcc.setDataCriacao(LocalDate.now());
    }

    // =============== TESTES CADASTRAR PROPOSTA TCC ===============

    @Test
    @DisplayName("Deve cadastrar proposta de TCC com sucesso")
    void deveCadastrarPropostaTccComSucesso() {
        // Given
        when(usuarioRepository.findById(orientador.getId())).thenReturn(Optional.of(orientador));
        when(tccRepository.save(any(TccEntity.class))).thenReturn(tcc);

        // When
        TccEntity resultado = tccService.cadastrarPropostaTcc(tcc);

        // Then
        assertNotNull(resultado);
        assertEquals(StatusTcc.EM_ANDAMENTO, resultado.getStatus());
        assertEquals(LocalDate.now(), resultado.getDataCriacao());

        verify(usuarioRepository, times(1)).findById(orientador.getId());
        verify(tccRepository, times(1)).save(tcc);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar TCC sem título")
    void deveLancarExcecaoAoCadastrarTccSemTitulo() {
        // Given
        tcc.setTitulo(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.cadastrarPropostaTcc(tcc));

        assertEquals("Título do TCC é obrigatório", exception.getMessage());

        verify(usuarioRepository, never()).findById(any());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar TCC com título vazio")
    void deveLancarExcecaoAoCadastrarTccComTituloVazio() {
        // Given
        tcc.setTitulo("");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.cadastrarPropostaTcc(tcc));

        assertEquals("Título do TCC é obrigatório", exception.getMessage());

        verify(usuarioRepository, never()).findById(any());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando orientador não for encontrado")
    void deveLancarExcecaoQuandoOrientadorNaoForEncontrado() {
        // Given
        when(usuarioRepository.findById(orientador.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.cadastrarPropostaTcc(tcc));

        assertEquals("Orientador não encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(orientador.getId());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for orientador")
    void deveLancarExcecaoQuandoUsuarioNaoForOrientador() {
        // Given
        tcc.setOrientador(usuarioComum);
        when(usuarioRepository.findById(usuarioComum.getId())).thenReturn(Optional.of(usuarioComum));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.cadastrarPropostaTcc(tcc));

        assertEquals("O usuário selecionado não é um orientador", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(usuarioComum.getId());
        verify(tccRepository, never()).save(any());
    }

    // =============== TESTES ATUALIZAR STATUS TCC ===============

    @Test
    @DisplayName("Deve atualizar status TCC como orientador do TCC")
    void deveAtualizarStatusTccComoOrientadorDoTcc() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        when(tccRepository.save(any(TccEntity.class))).thenReturn(tcc);

        // When
        TccEntity resultado = tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.CONCLUIDO, orientador);

        // Then
        assertNotNull(resultado);
        assertEquals(StatusTcc.CONCLUIDO, resultado.getStatus());
        assertEquals(LocalDate.now(), resultado.getDataAtualizacao());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).save(tcc);
    }

    @Test
    @DisplayName("Deve atualizar status TCC como coordenador")
    void deveAtualizarStatusTccComoCoordenador() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        when(tccRepository.save(any(TccEntity.class))).thenReturn(tcc);

        // When
        TccEntity resultado = tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.CONCLUIDO, coordenador);

        // Then
        assertNotNull(resultado);
        assertEquals(StatusTcc.CONCLUIDO, resultado.getStatus());
        assertEquals(LocalDate.now(), resultado.getDataAtualizacao());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).save(tcc);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar status de TCC inexistente")
    void deveLancarExcecaoAoAtualizarStatusDeTccInexistente() {
        // Given
        when(tccRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarStatusTcc(1L, StatusTcc.CONCLUIDO, orientador));

        assertEquals("TCC não encontrado", exception.getMessage());

        verify(tccRepository, times(1)).findById(1L);
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando orientador não for o orientador do TCC")
    void deveLancarExcecaoQuandoOrientadorNaoForOrientadorDoTcc() {
        // Given
        UsuarioEntity outroOrientador = new UsuarioEntity();
        outroOrientador.setId(5L);
        outroOrientador.setTipoUsuario(TipoUsuario.ORIENTADOR);

        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.CONCLUIDO, outroOrientador));

        assertEquals("Apenas o orientador do TCC ou um coordenador pode alterar seu status", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não tem permissão para atualizar status")
    void deveLancarExcecaoQuandoUsuarioNaoTemPermissaoParaAtualizarStatus() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.CONCLUIDO, aluno));

        assertEquals("Usuário não tem permissão para atualizar status", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).save(any());
    }

    // =============== TESTES ATUALIZAR TCC ===============

    @Test
    @DisplayName("Deve atualizar TCC como aluno autor")
    void deveAtualizarTccComoAlunoAutor() {
        // Given
        TccEntity tccAtualizado = new TccEntity();
        tccAtualizado.setId(tcc.getId());
        tccAtualizado.setTitulo("Novo Título");
        tccAtualizado.setResumo("Novo Resumo");
        tccAtualizado.setArea("Nova Área");

        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        when(tccRepository.save(any(TccEntity.class))).thenReturn(tcc);

        // When
        TccEntity resultado = tccService.atualizarTcc(tccAtualizado, aluno);

        // Then
        assertNotNull(resultado);
        assertEquals("Novo Título", tcc.getTitulo());
        assertEquals("Novo Resumo", tcc.getResumo());
        assertEquals("Nova Área", tcc.getArea());
        assertEquals(LocalDate.now(), tcc.getDataAtualizacao());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).save(tcc);
    }

    @Test
    @DisplayName("Deve lançar exceção quando aluno não for o autor do TCC")
    void deveLancarExcecaoQuandoAlunoNaoForAutorDoTcc() {
        // Given
        UsuarioEntity outroAluno = new UsuarioEntity();
        outroAluno.setId(6L);
        outroAluno.setTipoUsuario(TipoUsuario.ALUNO);

        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarTcc(tcc, outroAluno));

        assertEquals("Apenas o autor pode atualizar o TCC", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TCC estiver concluído")
    void deveLancarExcecaoQuandoTccEstiverConcluido() {
        // Given
        tcc.setStatus(StatusTcc.CONCLUIDO);
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarTcc(tcc, aluno));

        assertEquals("Não é possível atualizar um TCC já concluído ou reprovado", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TCC estiver reprovado")
    void deveLancarExcecaoQuandoTccEstiverReprovado() {
        // Given
        tcc.setStatus(StatusTcc.REPROVADO);
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.atualizarTcc(tcc, aluno));

        assertEquals("Não é possível atualizar um TCC já concluído ou reprovado", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar TCC como orientador incluindo status")
    void deveAtualizarTccComoOrientadorIncluindoStatus() {
        // Given
        TccEntity tccAtualizado = new TccEntity();
        tccAtualizado.setId(tcc.getId());
        tccAtualizado.setTitulo("Novo Título");
        tccAtualizado.setStatus(StatusTcc.CONCLUIDO);

        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        when(tccRepository.save(any(TccEntity.class))).thenReturn(tcc);

        // When
        TccEntity resultado = tccService.atualizarTcc(tccAtualizado, orientador);

        // Then
        assertNotNull(resultado);
        assertEquals("Novo Título", tcc.getTitulo());
        assertEquals(StatusTcc.CONCLUIDO, tcc.getStatus());
        assertEquals(LocalDate.now(), tcc.getDataAtualizacao());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).save(tcc);
    }

    // =============== TESTES LISTAR TCCS ===============

    @Test
    @DisplayName("Deve listar TCCs por aluno")
    void deveListarTccsPorAluno() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(usuarioRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(tccRepository.findByAluno(aluno)).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTccsPorAluno(aluno.getId());

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(tcc.getId(), resultado.get(0).getId());

        verify(usuarioRepository, times(1)).findById(aluno.getId());
        verify(tccRepository, times(1)).findByAluno(aluno);
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar TCCs por aluno inexistente")
    void deveLancarExcecaoAoListarTccsPorAlunoInexistente() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.listarTccsPorAluno(1L));

        assertEquals("Aluno não encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(tccRepository, never()).findByAluno(any());
    }

    @Test
    @DisplayName("Deve listar TCCs por orientador")
    void deveListarTccsPorOrientador() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(usuarioRepository.findById(orientador.getId())).thenReturn(Optional.of(orientador));
        when(tccRepository.findByOrientador(orientador)).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTccsPorOrientador(orientador.getId());

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(tcc.getId(), resultado.get(0).getId());

        verify(usuarioRepository, times(1)).findById(orientador.getId());
        verify(tccRepository, times(1)).findByOrientador(orientador);
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar TCCs por orientador inexistente")
    void deveLancarExcecaoAoListarTccsPorOrientadorInexistente() {
        // Given
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.listarTccsPorOrientador(2L));

        assertEquals("Orientador não encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(2L);
        verify(tccRepository, never()).findByOrientador(any());
    }

    @Test
    @DisplayName("Deve listar TCCs por aluno e status")
    void deveListarTccsPorAlunoEStatus() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(tccRepository.findByAlunoAndStatus(aluno, StatusTcc.EM_ANDAMENTO)).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTccsPorAlunoEStatus(aluno, StatusTcc.EM_ANDAMENTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(tccRepository, times(1)).findByAlunoAndStatus(aluno, StatusTcc.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve listar TCCs por orientador e status")
    void deveListarTccsPorOrientadorEStatus() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(tccRepository.findByOrientadorAndStatus(orientador, StatusTcc.EM_ANDAMENTO)).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTccsPorOrientadorEStatus(orientador, StatusTcc.EM_ANDAMENTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(tccRepository, times(1)).findByOrientadorAndStatus(orientador, StatusTcc.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve listar TCCs por status")
    void deveListarTccsPorStatus() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(tccRepository.findByStatus(StatusTcc.EM_ANDAMENTO)).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTccsPorStatus(StatusTcc.EM_ANDAMENTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(tccRepository, times(1)).findByStatus(StatusTcc.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve listar todos os TCCs")
    void deveListarTodosTccs() {
        // Given
        List<TccEntity> tccs = Arrays.asList(tcc);
        when(tccRepository.findAll()).thenReturn(tccs);

        // When
        List<TccEntity> resultado = tccService.listarTodosTccs();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(tccRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar TCC por ID")
    void deveBuscarTccPorId() {
        // Given
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));

        // When
        Optional<TccEntity> resultado = tccService.buscarPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(tcc.getId(), resultado.get().getId());

        verify(tccRepository, times(1)).findById(1L);
    }

    // =============== TESTES DELETAR TCC ===============

    @Test
    @DisplayName("Deve deletar TCC como aluno autor (TCC em andamento)")
    void deveDeletarTccComoAlunoAutorTccEmAndamento() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        doNothing().when(tccRepository).deleteById(tcc.getId());

        // When
        tccService.deletarTcc(tcc.getId(), aluno);

        // Then
        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).deleteById(tcc.getId());
    }

    @Test
    @DisplayName("Deve deletar TCC como coordenador")
    void deveDeletarTccComoCoordenador() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));
        doNothing().when(tccRepository).deleteById(tcc.getId());

        // When
        tccService.deletarTcc(tcc.getId(), coordenador);

        // Then
        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, times(1)).deleteById(tcc.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar TCC inexistente")
    void deveLancarExcecaoAoDeletarTccInexistente() {
        // Given
        when(tccRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.deletarTcc(1L, aluno));

        assertEquals("TCC não encontrado", exception.getMessage());

        verify(tccRepository, times(1)).findById(1L);
        verify(tccRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando aluno não for autor do TCC")
    void deveLancarExcecaoQuandoAlunoNaoForAutorParaDeletar() {
        // Given
        UsuarioEntity outroAluno = new UsuarioEntity();
        outroAluno.setId(6L);
        outroAluno.setTipoUsuario(TipoUsuario.ALUNO);

        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.deletarTcc(tcc.getId(), outroAluno));

        assertEquals("Apenas o autor pode excluir o TCC", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TCC não estiver em andamento")
    void deveLancarExcecaoQuandoTccNaoEstiverEmAndamento() {
        // Given
        tcc.setStatus(StatusTcc.CONCLUIDO);
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.deletarTcc(tcc.getId(), aluno));

        assertEquals("Só é possível excluir TCCs que estejam em andamento", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não tem permissão para deletar")
    void deveLancarExcecaoQuandoUsuarioNaoTemPermissaoParaDeletar() {
        // Given
        when(tccRepository.findById(tcc.getId())).thenReturn(Optional.of(tcc));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tccService.deletarTcc(tcc.getId(), orientador));

        assertEquals("Usuário não tem permissão para excluir TCC", exception.getMessage());

        verify(tccRepository, times(1)).findById(tcc.getId());
        verify(tccRepository, never()).deleteById(any());
    }
}