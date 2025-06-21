package com.example.gestaotcc.service;

import com.example.gestaotcc.model.AvaliacaoEntity;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.EntregaRepository;
import com.example.gestaotcc.repository.TccRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntregaServiceTest {

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private TccRepository tccRepository;

    @Mock
    private ArquivoStorageService arquivoStorageService;

    @Mock
    private MultipartFile arquivo;

    @InjectMocks
    private EntregaService entregaService;

    private UsuarioEntity alunoFelipe;
    private UsuarioEntity orientador;
    private UsuarioEntity coordenador;
    private TccEntity tcc;
    private EntregaEntity entrega;

    @BeforeEach
    void setUp() {
        // Setup do aluno Felipe
        alunoFelipe = new UsuarioEntity();
        alunoFelipe.setId(1L);
        alunoFelipe.setNome("Felipe Silva");
        alunoFelipe.setTipoUsuario(TipoUsuario.ALUNO);

        // Setup do orientador
        orientador = new UsuarioEntity();
        orientador.setId(2L);
        orientador.setNome("Prof. João");
        orientador.setTipoUsuario(TipoUsuario.ORIENTADOR);

        // Setup do coordenador
        coordenador = new UsuarioEntity();
        coordenador.setId(3L);
        coordenador.setNome("Coord. Maria");
        coordenador.setTipoUsuario(TipoUsuario.COORDENADOR);

        // Setup do TCC
        tcc = new TccEntity();
        tcc.setId(1L);
        tcc.setTitulo("Sistema de Gestão TCC");
        tcc.setAluno(alunoFelipe);
        tcc.setOrientador(orientador);
        tcc.setStatus(StatusTcc.EM_ANDAMENTO);

        // Setup da entrega
        entrega = new EntregaEntity();
        entrega.setId(1L);
        entrega.setTcc(tcc);
        entrega.setTipo(TipoEntrega.PARCIAL);
        entrega.setObservacao("Primeira entrega parcial");
        entrega.setAvaliacoes(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve cadastrar entrega com sucesso quando aluno é autor do TCC")
    void deveCadastrarEntregaComSucessoQuandoAlunoEAutor() {
        // Arrange
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));
        when(arquivoStorageService.salvarArquivo(arquivo)).thenReturn("/uploads/arquivo.pdf");
        when(entregaRepository.save(any(EntregaEntity.class))).thenReturn(entrega);

        // Act
        EntregaEntity resultado = entregaService.cadastrarEntrega(entrega, arquivo, alunoFelipe);

        // Assert
        assertNotNull(resultado);
        verify(tccRepository).findById(1L);
        verify(arquivoStorageService).salvarArquivo(arquivo);
        verify(entregaRepository).save(any(EntregaEntity.class));
        assertEquals("/uploads/arquivo.pdf", entrega.getCaminhoArquivo());
        assertNotNull(entrega.getDataEnvio());
    }

    @Test
    @DisplayName("Deve lançar exceção quando aluno tenta cadastrar entrega de TCC que não é seu")
    void deveLancarExcecaoQuandoAlunoTentaCadastrarEntregaDeTccQueNaoESeu() {
        // Arrange
        UsuarioEntity outroAluno = new UsuarioEntity();
        outroAluno.setId(99L);
        outroAluno.setTipoUsuario(TipoUsuario.ALUNO);

        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            entregaService.cadastrarEntrega(entrega, arquivo, outroAluno);
        });

        assertEquals("Apenas o autor do TCC pode enviar entregas", exception.getMessage());
        verify(arquivoStorageService, never()).salvarArquivo(any());
        verify(entregaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TCC não é encontrado")
    void deveLancarExcecaoQuandoTccNaoEEncontrado() {
        // Arrange
        when(tccRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            entregaService.cadastrarEntrega(entrega, arquivo, alunoFelipe);
        });

        assertEquals("TCC não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando TCC já está finalizado")
    void deveLancarExcecaoQuandoTccJaEstaFinalizado() {
        // Arrange
        tcc.setStatus(StatusTcc.CONCLUIDO);
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            entregaService.cadastrarEntrega(entrega, arquivo, alunoFelipe);
        });

        assertEquals("Não é possível enviar entregas para um TCC já finalizado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar entregas quando aluno é autor do TCC")
    void deveListarEntregasQuandoAlunoEAutorDoTcc() {
        // Arrange
        List<EntregaEntity> entregas = Arrays.asList(entrega);
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));
        when(entregaRepository.findByTcc(tcc)).thenReturn(entregas);

        // Act
        List<EntregaEntity> resultado = entregaService.listarEntregasPorTcc(1L, alunoFelipe);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(entregaRepository).findByTcc(tcc);
    }

    @Test
    @DisplayName("Deve listar entregas quando usuário é orientador do TCC")
    void deveListarEntregasQuandoUsuarioEOrientadorDoTcc() {
        // Arrange
        List<EntregaEntity> entregas = Arrays.asList(entrega);
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));
        when(entregaRepository.findByTcc(tcc)).thenReturn(entregas);

        // Act
        List<EntregaEntity> resultado = entregaService.listarEntregasPorTcc(1L, orientador);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(entregaRepository).findByTcc(tcc);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não tem permissão para listar entregas")
    void deveLancarExcecaoQuandoUsuarioNaoTemPermissaoParaListarEntregas() {
        // Arrange
        UsuarioEntity usuarioSemPermissao = new UsuarioEntity();
        usuarioSemPermissao.setId(99L);
        usuarioSemPermissao.setTipoUsuario(TipoUsuario.ORIENTADOR);

        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            entregaService.listarEntregasPorTcc(1L, usuarioSemPermissao);
        });

        assertEquals("Usuário não tem permissão para acessar estas entregas", exception.getMessage());
    }

    @Test
    @DisplayName("Deve deletar entrega quando aluno é autor e não há avaliações")
    void deveDeletarEntregaQuandoAlunoEAutorESemAvaliacoes() {
        // Arrange
        entrega.setTcc(tcc);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        // Act
        entregaService.deletarEntrega(1L, alunoFelipe);

        // Assert
        verify(arquivoStorageService).excluirArquivo(entrega.getCaminhoArquivo());
        verify(entregaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar entrega com avaliações")
    void deveLancarExcecaoAoTentarDeletarEntregaComAvaliacoes() {
        // Arrange
        AvaliacaoEntity avaliacao = new AvaliacaoEntity();
        entrega.getAvaliacoes().add(avaliacao);

        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            entregaService.deletarEntrega(1L, alunoFelipe);
        });

        assertEquals("Não é possível excluir uma entrega que já possui avaliação", exception.getMessage());
        verify(entregaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve obter última entrega por data de envio")
    void deveObterUltimaEntregaPorDataDeEnvio() {
        // Arrange
        entrega.setDataEnvio(LocalDateTime.now());
        when(tccRepository.findById(1L)).thenReturn(Optional.of(tcc));
        when(entregaRepository.findFirstByTccOrderByDataEnvioDesc(tcc))
                .thenReturn(Optional.of(entrega));

        // Act
        Optional<EntregaEntity> resultado = entregaService.obterUltimaEntrega(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(entrega, resultado.get());
        verify(entregaRepository).findFirstByTccOrderByDataEnvioDesc(tcc);
    }
}