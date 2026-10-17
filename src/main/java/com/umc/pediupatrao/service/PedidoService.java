package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.exception.RegraDoTimaoException;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Pedido criarPedido(Pedido pedido, String usuarioLogado, String perfilUsuario) {
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setResponsavel(usuarioLogado);
        pedido.setStatus("RECEBIDO");

        calcularValores(pedido, perfilUsuario);

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
            usuarioLogado,
            perfilUsuario,
            "CRIACAO DE PEDIDO",
            "Pedido",
            salvo.getId(),
            "status",
            "NENHUM",
            "RECEBIDO",
            null
        );

        return salvo;
    }

    public List<Pedido> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        if (pedidos == null) {
            return new ArrayList<>();
        }
        for (Pedido p : pedidos) {
            if (p.getItens() == null) {
                p.setItens(new ArrayList<>());
            }
        }
        return pedidos;
    }

    public Pedido atualizarStatus(String id, String novoStatus, String usuarioLogado, String perfilUsuario, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraDoTimaoException("Pedido não encontrado."));

        String statusAtual = pedido.getStatus();

        if ("CANCELADO".equals(statusAtual)) {
            throw new RegraDoTimaoException("Não é permitido alterar um pedido cancelado.");
        }

        if ("CANCELADO".equals(novoStatus)) {
            if (!perfilUsuario.contains("GERENTE") && !perfilUsuario.contains("ADMIN")) {
                throw new RegraDoTimaoException("Apenas o GERENTE ou ADMIN pode cancelar pedidos.");
            }
            if ("SAIU_PARA_ENTREGA".equals(statusAtual) || "RETIRADO".equals(statusAtual) || "FINALIZADO".equals(statusAtual)) {
                throw new RegraDoTimaoException("Não é possível cancelar o pedido após a saída ou finalização.");
            }
            if (justificativa == null || justificativa.trim().isEmpty()) {
                throw new RegraDoTimaoException("O cancelamento exige justificativa.");
            }

            pedido.setStatus("CANCELADO");
            Pedido salvo = pedidoRepository.save(pedido);

            auditoriaService.registrar(
                usuarioLogado,
                perfilUsuario,
                "CANCELAMENTO DE PEDIDO",
                "Pedido",
                salvo.getId(),
                "status",
                statusAtual,
                "CANCELADO",
                justificativa
            );

            return salvo;
        }

        validarTransicao(statusAtual, novoStatus);

        pedido.setStatus(novoStatus);
        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar(
            usuarioLogado,
            perfilUsuario,
            "MUDANCA DE STATUS",
            "Pedido",
            salvo.getId(),
            "status",
            statusAtual,
            novoStatus,
            null
        );

        return salvo;
    }

    public void calcularValores(Pedido pedido, String perfilUsuario) {
        double subtotal = 0.0;
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                if (item.getProduto() != null && item.getProduto().getId() != null) {
                    Produto produtoDb = produtoRepository.findById(item.getProduto().getId())
                            .orElseThrow(() -> new RegraDoTimaoException("Produto não encontrado."));
                    item.setPrecoUnitario(produtoDb.getPreco());
                }
                subtotal += item.getPrecoUnitario() * item.getQuantidade();
            }
        }

        double descontoPercentual = pedido.getDesconto();
        if (descontoPercentual > 0) {
            if (!perfilUsuario.contains("GERENTE") && !perfilUsuario.contains("ADMIN")) {
                throw new RegraDoTimaoException("Apenas gerentes podem aplicar descontos.");
            }
            if (descontoPercentual > 20.0) {
                throw new RegraDoTimaoException("O desconto máximo permitido é de 20%.");
            }
        }

        double valorDesconto = subtotal * (descontoPercentual / 100.0);
        pedido.setValorTotal(subtotal - valorDesconto);
    }

    private void validarTransicao(String atual, String novo) {
        if ("RECEBIDO".equals(atual) && !"EM_PREPARACAO".equals(novo)) {
            throw new RegraDoTimaoException("Pedido recebido só pode ir para Em Preparação.");
        }
        if ("EM_PREPARACAO".equals(atual) && !"PRONTO".equals(novo)) {
            throw new RegraDoTimaoException("Pedido em preparação só pode ir para Pronto.");
        }
        if ("PRONTO".equals(atual) && !("SAIU_PARA_ENTREGA".equals(novo) || "RETIRADO".equals(novo))) {
            throw new RegraDoTimaoException("Pedido pronto deve ir para Saiu para Entrega ou Retirado.");
        }
        if (("SAIU_PARA_ENTREGA".equals(atual) || "RETIRADO".equals(atual)) && !"FINALIZADO".equals(novo)) {
            throw new RegraDoTimaoException("Pedido em trânsito/retirada só pode ser Finalizado.");
        }
    }
}