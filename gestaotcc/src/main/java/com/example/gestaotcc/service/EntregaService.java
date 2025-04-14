package com.example.gestaotcc.service;

import com.example.gestaotcc.model.EntregaEntity;
import com.example.gestaotcc.model.TccEntity;
import com.example.gestaotcc.model.UsuarioEntity;
import com.example.gestaotcc.model.enuns.StatusTcc;
import com.example.gestaotcc.model.enuns.TipoEntrega;
import com.example.gestaotcc.model.enuns.TipoUsuario;
import com.example.gestaotcc.repository.EntregaRepository;
import com.example.gestaotcc.repository.TccRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntregaService {
    private final EntregaRepository entregaRepository;
    private final TccRepository tccRepository;
    private final ArquivoStorageService arquivoStorageService;

    public EntregaEntity cadastrarEntrega(EntregaEntity entrega, MultipartFile arquivo, UsuarioEntity usuarioSolicitante) {
        // Validações
        TccEntity tcc = tccRepository.findById(entrega.getTcc().getId())
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Verificar se o usuário é o aluno autor do TCC
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO &&
                !tcc.getAluno().getId().equals(usuarioSolicitante.getId())) {
            throw new RuntimeException("Apenas o autor do TCC pode enviar entregas");
        }

        if (tcc.getStatus() == StatusTcc.EM_ANDAMENTO) {
            throw new RuntimeException("TCC ainda não foi aprovado pelo orientador");
        }

        if (tcc.getStatus() == StatusTcc.CONCLUIDO || tcc.getStatus() == StatusTcc.REPROVADO) {
            throw new RuntimeException("Não é possível enviar entregas para um TCC já finalizado");
        }

        // Salvar arquivo
        String caminhoArquivo = arquivoStorageService.salvarArquivo(arquivo);
        entrega.setCaminhoArquivo(caminhoArquivo);
        entrega.setDataEnvio(LocalDateTime.now());
        entrega.setTcc(tcc);

        // Se o TCC estiver em andamento e a entrega for do tipo FINAL,
        // seria possível atualizar o status do TCC para concluído automaticamente,
        // mas deixamos essa decisão para o orientador

        return entregaRepository.save(entrega);
    }

    public List<EntregaEntity> listarEntregasPorTcc(Long tccId, UsuarioEntity usuarioSolicitante) {
        TccEntity tcc = tccRepository.findById(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        // Verificar se o usuário tem acesso a este TCC
        boolean temAcesso = usuarioSolicitante.getTipoUsuario() == TipoUsuario.COORDENADOR ||
                tcc.getAluno().getId().equals(usuarioSolicitante.getId()) ||
                tcc.getOrientador().getId().equals(usuarioSolicitante.getId());

        if (!temAcesso) {
            throw new RuntimeException("Usuário não tem permissão para acessar estas entregas");
        }

        return entregaRepository.findByTcc(tcc);
    }

    public Optional<EntregaEntity> buscarPorId(Long id) {
        return entregaRepository.findById(id);
    }

    public void deletarEntrega(Long id, UsuarioEntity usuarioSolicitante) {
        EntregaEntity entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));

        TccEntity tcc = entrega.getTcc();

        // Apenas o aluno autor pode excluir sua própria entrega
        if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.ALUNO) {
            if (!tcc.getAluno().getId().equals(usuarioSolicitante.getId())) {
                throw new RuntimeException("Apenas o autor do TCC pode excluir esta entrega");
            }

            // Verificar se já existe avaliação para esta entrega
            if (!entrega.getAvaliacoes().isEmpty()) {
                throw new RuntimeException("Não é possível excluir uma entrega que já possui avaliação");
            }
        } else if (usuarioSolicitante.getTipoUsuario() != TipoUsuario.COORDENADOR) {
            throw new RuntimeException("Usuário não tem permissão para excluir esta entrega");
        }

        // Excluir o arquivo físico
        arquivoStorageService.excluirArquivo(entrega.getCaminhoArquivo());

        entregaRepository.deleteById(id);
    }

    public Optional<EntregaEntity> obterUltimaEntrega(Long tccId) {
        TccEntity tcc = tccRepository.findById(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        return entregaRepository.findFirstByTccOrderByDataEnvioDesc(tcc);
    }

    public List<EntregaEntity> listarEntregasPorTipoETcc(Long tccId, String tipo) {
        TccEntity tcc = tccRepository.findById(tccId)
                .orElseThrow(() -> new RuntimeException("TCC não encontrado"));

        return entregaRepository.findByTccAndTipo(tcc,
                tipo.equalsIgnoreCase("PARCIAL") ?
                        TipoEntrega.PARCIAL :
                        TipoEntrega.FINAL);
    }
}