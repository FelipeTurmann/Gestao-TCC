package com.example.gestaotcc.service;

import com.example.gestaotcc.entity.TccEntity;
import com.example.gestaotcc.entity.enums;
import com.example.gestaotcc.repository.TccRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TccService {

    @Autowired
    private TccRepository repository;

    public TccEntity criarTcc(TccEntity tcc) {
        return repository.save(tcc);
    }

    public List<TccEntity> listarTodos() {
        return repository.findAll();
    }

    public TccEntity atualizarStatus(Long id, enums.StatusTcc novoStatus) throws Exception {
        TccEntity tcc = repository.findById(id)
                .orElseThrow(() -> new ClassNotFoundException("TCC não encontrado"));
        //tcc.setStatus(novoStatus);
        return repository.save(tcc);
    }
}
