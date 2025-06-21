package com.example.gestaotcc.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ArquivoStorageService {

    @Value("${app.arquivo.diretorio-upload}")
    private String diretorioUpload;

    public String salvarArquivo(MultipartFile arquivo) {
        // Criar diretório se não existir
        try {
            Path diretorioPath = Paths.get(diretorioUpload);
            if (!Files.exists(diretorioPath)) {
                Files.createDirectories(diretorioPath);
            }

            // Gerar nome único para o arquivo
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            String novoNome = UUID.randomUUID().toString() + extensao;

            Path destino = Paths.get(diretorioUpload).resolve(novoNome);
            Files.copy(arquivo.getInputStream(), destino);

            return novoNome;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo: " + e.getMessage());
        }
    }

    public void excluirArquivo(String nomeArquivo) {
        try {
            Path arquivo = Paths.get(diretorioUpload).resolve(nomeArquivo);
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao excluir arquivo: " + e.getMessage());
        }
    }

    public Path obterCaminhoCompleto(String nomeArquivo) {
        return Paths.get(diretorioUpload).resolve(nomeArquivo);
    }
}