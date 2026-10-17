package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        List<Produto> produtos = produtoRepository.findAll();
        return produtos != null ? produtos : new ArrayList<>();
    }

    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }

    public Produto salvar(Produto produto) {
        if (produto.getId() == null || produto.getId().isEmpty()) {
            return produtoRepository.save(produto);
        } else {
            if (produtoRepository.existsById(produto.getId())) {
                return produtoRepository.save(produto);
            } else {
                throw new IllegalArgumentException("Produto não encontrado para atualização.");
            }
        }
    }
}