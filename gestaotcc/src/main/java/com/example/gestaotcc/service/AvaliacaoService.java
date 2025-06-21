package com.example.gestaotcc.service;

import com.example.gestaotcc.model.AvaliacaoEntity;
import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.AvaliacaoRepository;
import com.example.gestaotcc.repository.EntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;

    private final EntregaRepository entregaRepository;

    private final TccService tccService;

    public AvaliacaoEntity cadastrarAvaliacao(AvaliacaoEntity avaliacao, UsuarioEntity usuarioSolicitante) {
        // Validações
        EntregaEntity entrega = entregaRepository.findById(avaliacao.getEntrega().getId())
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        TccEntity tcc = entrega.getTcc();

        // Verificar se o usuário é o orientador do TCC ou coordenador
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ORIENTADOR &&
                !tcc.getOrientador().getId().equals(usuarioSolicitante.getId())) {
            throw new RuntimeException("Apenas o orientador do TCC pode avaliar esta entrega");
        }

        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            throw new RuntimeException("Alunos não podem criar avaliações");
        }

        // Verificar se já existe uma avaliação deste orientador para esta entrega
        Optional<AvaliacaoEntity> avaliacaoExistente =
                avaliacaoRepository.findByEntregaAndOrientador(entrega, usuarioSolicitante);

        if (avaliacaoExistente.isPresent()) {
            throw new RuntimeException("Você já avaliou esta entrega. Edite a avaliação existente.");
        }

        // Verificar se a nota está no intervalo permitido (0 a 10)
        if (avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new RuntimeException("A nota deve estar entre 0 e 10");
        }

        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setEntrega(entrega);
        avaliacao.setOrientador(usuarioSolicitante);

        // Se for uma entrega final com nota >=7, podemos sugerir atualizar status do TCC para concluído
        if (entrega.getTipo() == TipoEntrega.FINAL && avaliacao.getNota() >= 7.0) {
           tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.CONCLUIDO, usuarioSolicitante);
        } else {
            tccService.atualizarStatusTcc(tcc.getId(), StatusTcc.REPROVADO, usuarioSolicitante);
        }

        return avaliacaoRepository.save(avaliacao);
    }

    public AvaliacaoEntity atualizarAvaliacao(AvaliacaoEntity avaliacao, UsuarioEntity usuarioSolicitante) {
        AvaliacaoEntity avaliacaoExistente = avaliacaoRepository.findById(avaliacao.getId())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        // Apenas o orientador que criou a avaliação ou o coordenador podem editar
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ORIENTADOR &&
                !avaliacaoExistente.getOrientador().getId().equals(usuarioSolicitante.getId())) {
            throw new RuntimeException("Apenas o orientador que criou a avaliação pode editá-la");
        }

        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            throw new RuntimeException("Alunos não podem editar avaliações");
        }

        // Verificar se a nota está no intervalo permitido (0 a 10)
        if (avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new RuntimeException("A nota deve estar entre 0 e 10");
        }

        avaliacaoExistente.setNota(avaliacao.getNota());
        avaliacaoExistente.setComentario(avaliacao.getComentario());
        avaliacaoExistente.setDataAvaliacao(LocalDateTime.now());

        return avaliacaoRepository.save(avaliacaoExistente);
    }

    public List<AvaliacaoEntity> listarAvaliacoesPorEntrega(Long entregaId, UsuarioEntity usuarioSolicitante) {
        EntregaEntity entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        TccEntity tcc = entrega.getTcc();

        // Verificar se o usuário tem permissão para ver estas avaliações
        boolean temPermissao = usuarioSolicitante.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuarioSolicitante.getId()) ||
                tcc.getOrientador().getId().equals(usuarioSolicitante.getId());

        if (!temPermissao) {
            throw new RuntimeException("Usuário não tem permissão para visualizar estas avaliações");
        }

        return avaliacaoRepository.findByEntrega(entrega);
    }

    public List<AvaliacaoEntity> listarAvaliacoesPorOrientador(Long orientadorId) {
        UsuarioEntity orientador = new UsuarioEntity();
        orientador.setId(orientadorId);
        return avaliacaoRepository.findByOrientador(orientador);
    }

    public Optional<AvaliacaoEntity> buscarPorId(Long id) {
        return avaliacaoRepository.findById(id);
    }

    public void deletarAvaliacao(Long id, UsuarioEntity usuarioSolicitante) {
        AvaliacaoEntity avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        // Apenas o orientador que criou a avaliação ou um coordenador podem excluir
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ORIENTADOR &&
                !avaliacao.getOrientador().getId().equals(usuarioSolicitante.getId())) {
            throw new RuntimeException("Apenas o orientador que criou a avaliação pode excluí-la");
        }

        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            throw new RuntimeException("Alunos não podem excluir avaliações");
        }

        avaliacaoRepository.deleteById(id);
    }
}