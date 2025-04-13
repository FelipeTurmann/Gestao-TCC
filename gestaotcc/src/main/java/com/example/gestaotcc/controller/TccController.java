package com.example.gestaotcc.controller;

import com.example.gestaotcc.entity.TccEntity;
import com.example.gestaotcc.entity.enums;
import com.example.gestaotcc.service.TccService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tcc")
public class TccController {

    @Autowired
    private TccService service;

    @PostMapping
    public ResponseEntity<TccEntity> criar(@RequestBody TccEntity dto) {
        return ResponseEntity.ok(service.criarTcc(dto));
    }

    @GetMapping
    public List<TccEntity> listar() {
        return service.listarTodos();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TccEntity> atualizarStatus(@PathVariable Long id, @RequestBody enums.StatusTcc status) throws Exception {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }
}