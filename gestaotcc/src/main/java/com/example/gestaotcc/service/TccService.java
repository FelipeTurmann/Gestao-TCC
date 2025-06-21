package com.example.gestaotcc.service;

import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.TccRepository;
import com.example.gestaotcc.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TccService {
    private final TccRepository tccRepository;
    private final UsuarioRepository usuarioRepository;

    public TccEntity cadastrarPropostaTcc(TccEntity tcc) {
        // Validações
        if (tcc.getTitulo() == null || tcc.getTitulo().isEmpty()) {
            throw new RuntimeException("Título do TCC é obrigatório");
        }

        // Verificar se o orientador existe e é realmente um orientador
        UsuarioEntity orientador = usuarioRepository.findById(tcc.getOrientador().getId())
                .orElseThrow(() -> new RuntimeException("Orientador não encontrado"));

        if (orientador.getTipoUsuario() != TipoUsuario.ORIENTADOR) {
            throw new RuntimeException("O usuário selecionado não é um orientador");
        }

        // Configurações iniciais
        tcc.setStatus(StatusTcc.EM_ANDAMENTO);
        tcc.setDataCriacao(LocalDate.now());

        return tccRepository.save(tcc);
    }

    public TccEntity atualizarStatusTcc(Long tccId, StatusTcc novoStatus, UsuarioEntity usuarioSolicitante) {
        TccEntity tcc = tccRepository.findById(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Verificar permissões
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ORIENTADOR ||
                usuarioSolicitante.getTipoUsuario() == TipoUsuario.COORDENADOR) {

            // Se é o orientador do TCC ou um coordenador
            if (tcc.getOrientador().getId().equals(usuarioSolicitante.getId()) ||
                    usuarioSolicitante.getTipoUsuario() == TipoUsuario.COORDENADOR) {
                tcc.setStatus(novoStatus);
                tcc.setDataAtualizacao(LocalDate.now());
                return tccRepository.save(tcc);
            } else {
                throw new RuntimeException("Apenas o orientador do TCC ou um coordenador pode alterar seu status");
            }
        } else {
            throw new RuntimeException("Usuário não tem permissão para atualizar status");
        }
    }

    public TccEntity atualizarTcc(TccEntity tcc, UsuarioEntity usuarioSolicitante) {
        TccEntity tccExistente = tccRepository.findById(tcc.getId())
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Aluno só pode atualizar se for o autor e se estiver em PROPOSTA ou EM_ANDAMENTO
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            if (!tccExistente.getAluno().getId().equals(usuarioSolicitante.getId())) {
                throw new RuntimeException("Apenas o autor pode atualizar o TCC");
            }

            if (tccExistente.getStatus() == StatusTcc.CONCLUIDO ||
                    tccExistente.getStatus() == StatusTcc.REPROVADO) {
                throw new RuntimeException("Não é possível atualizar um TCC já concluído ou reprovado");
            }

            // Aluno não pode alterar o orientador ou o status
            tcc.setOrientador(tccExistente.getOrientador());
            tcc.setStatus(tccExistente.getStatus());
        }

        // Atualizar dados
        tccExistente.setTitulo(tcc.getTitulo());
        tccExistente.setResumo(tcc.getResumo());
        tccExistente.setArea(tcc.getArea());
        tccExistente.setDataAtualizacao(LocalDate.now());

        // Orientador ou coordenador podem alterar status
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ORIENTADOR ||
                usuarioSolicitante.getTipoUsuario() == TipoUsuario.COORDENADOR) {
            tccExistente.setStatus(tcc.getStatus());
        }

        return tccRepository.save(tccExistente);
    }

    public List<TccEntity> listarTccsPorAluno(Long alunoId) {
        UsuarioEntity aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        return tccRepository.findByAluno(aluno);
    }

    public List<TccEntity> listarTccsPorOrientador(Long orientadorId) {
        UsuarioEntity orientador = usuarioRepository.findById(orientadorId)
                .orElseThrow(() -> new RuntimeException("Orientador não encontrado"));
        return tccRepository.findByOrientador(orientador);
    }

    public List<TccEntity> listarTccsPorAlunoEStatus(UsuarioEntity aluno, StatusTcc status) {
        return tccRepository.findByAlunoAndStatus(aluno, status);
    }

    public List<TccEntity> listarTccsPorOrientadorEStatus(UsuarioEntity orientador, StatusTcc status) {
        return tccRepository.findByOrientadorAndStatus(orientador, status);
    }

    public List<TccEntity> listarTccsPorStatus(StatusTcc status) {
        return tccRepository.findByStatus(status);
    }

    public List<TccEntity> listarTodosTccs() {
        return tccRepository.findAll();
    }

    public Optional<TccEntity> buscarPorId(Long id) {
        return tccRepository.findById(id);
    }

    public void deletarTcc(Long id, UsuarioEntity usuarioSolicitante) {
        TccEntity tcc = tccRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Apenas o aluno autor (se estiver em PROPOSTA) ou um coordenador pode excluir
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            if (!tcc.getAluno().getId().equals(usuarioSolicitante.getId())) {
                throw new RuntimeException("Apenas o autor pode excluir o TCC");
            }

            if (tcc.getStatus() != StatusTcc.EM_ANDAMENTO) {
                throw new RuntimeException("Só é possível excluir TCCs que estejam em andamento");
            }
        } else if (usuarioSolicitante.getTipoUsuario() != TipoUsuario.COORDENADOR) {
            throw new RuntimeException("Usuário não tem permissão para excluir TCC");
        }

        tccRepository.deleteById(id);
    }
}