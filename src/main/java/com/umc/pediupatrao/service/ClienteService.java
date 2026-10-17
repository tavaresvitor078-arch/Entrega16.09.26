package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.exception.RegraDoTimaoException;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes != null ? clientes : new java.util.ArrayList<>();
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    public Cliente salvarCliente(Cliente cliente, String usuarioLogado, String perfilUsuario) {
        Cliente salvo = clienteRepository.save(cliente);
        try {
            auditoriaService.registrar(
                usuarioLogado,
                perfilUsuario,
                "CRIACAO DE CLIENTE",
                "Cliente",
                salvo.getId(),
                "nome",
                "NENHUM",
                salvo.getNome(),
                null
            );
        } catch (Exception e) {
            // Ignora se auditoria falhar
        }
        return salvo;
    }

    public Cliente atualizarCliente(String id, Cliente clienteAtualizado, String usuarioLogado, String perfilUsuario) {
        Cliente clienteAntigo = clienteRepository.findById(id)
                .orElseThrow(() -> new RegraDoTimaoException("Cliente não encontrado."));

        clienteAntigo.setNome(clienteAtualizado.getNome());
        clienteAntigo.setTelefone(clienteAtualizado.getTelefone());
        clienteAntigo.setLogradouro(clienteAtualizado.getLogradouro());

        Cliente salvo = clienteRepository.save(clienteAntigo);
        try {
            auditoriaService.registrar(
                usuarioLogado,
                perfilUsuario,
                "ALTERACAO DE CLIENTE",
                "Cliente",
                id,
                "dados",
                "ANTIGO",
                "ATUALIZADO",
                null
            );
        } catch (Exception e) {
            // Ignora se auditoria falhar
        }
        return salvo;
    }

    public void deletarCliente(String id, String usuarioLogado, String perfilUsuario) {
        clienteRepository.deleteById(id);
    }
}