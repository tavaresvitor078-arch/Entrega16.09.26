package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.UsuarioTimaoRequisicao;
import com.umc.pediupatrao.dto.UsuarioTimaoResposta;
import com.umc.pediupatrao.exception.RegraDoTimaoException;
import com.umc.pediupatrao.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para listar todos os usuários
    @GetMapping
    public List<UsuarioTimaoResposta> listarTodos() {
        return usuarioService.listarTodos().stream().map(UsuarioTimaoResposta::de).toList();
    }

    // Endpoint para buscar um usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioTimaoResposta> buscarPorId(@PathVariable String id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(UsuarioTimaoResposta.de(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para criar um novo usuário
    @PostMapping
    public ResponseEntity<UsuarioTimaoResposta> criarUsuario(@RequestBody UsuarioTimaoRequisicao requisicao) {
        var criado = usuarioService.criar(requisicao.username(), requisicao.password(), requisicao.perfil());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioTimaoResposta.de(criado));
    }

    // Endpoint para atualizar um usuário
    @PutMapping("/{id}")
    public UsuarioTimaoResposta atualizarUsuario(@PathVariable String id,
                                                 @RequestBody UsuarioTimaoRequisicao requisicao,
                                                 Authentication authentication) {
        var atualizado = usuarioService.atualizar(id, requisicao.username(), requisicao.password(),
                requisicao.perfil(), authentication.getName());
        return UsuarioTimaoResposta.de(atualizado);
    }

    // Endpoint para deletar um usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable String id, Authentication authentication) {
        usuarioService.deletar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // Devolve a mensagem da regra violada em vez de um erro genérico
    @ExceptionHandler(RegraDoTimaoException.class)
    public ResponseEntity<Map<String, String>> regraViolada(RegraDoTimaoException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}