package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.PerfilTimao;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.exception.RegraDoTimaoException;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern FORMATO_USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Busca um usuário por ID
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // Busca um usuário por username
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Salva um novo usuário (criptografando a senha)
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario criar(String username, String senha, PerfilTimao perfil) {
        String login = validarUsername(username);
        validarSenha(senha, true);
        validarPerfil(perfil);

        if (usuarioRepository.existsByUsername(login)) {
            throw new RegraDoTimaoException("Já existe um usuário com esse username.");
        }

        Usuario novo = new Usuario(); // id nulo: nunca sobrescreve outro usuário
        novo.setUsername(login);
        novo.setPassword(passwordEncoder.encode(senha)); // Criptografa a senha
        novo.setPerfil(perfil);
        return usuarioRepository.save(novo);
    }

    // Deleta um usuário pelo ID
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(String id, String usernameLogado) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraDoTimaoException("Usuário não encontrado."));

        if (existente.getUsername().equals(usernameLogado)) {
            throw new RegraDoTimaoException("Você não pode excluir o seu próprio usuário.");
        }
        if (existente.getPerfil() == PerfilTimao.ADMIN
                && usuarioRepository.countByPerfil(PerfilTimao.ADMIN) <= 1) {
            throw new RegraDoTimaoException("Não é possível excluir o último ADMIN do sistema.");
        }
        usuarioRepository.deleteById(id);
    }

    // Lista todos os usuários
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Atualiza o usuário usuários
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario atualizar(String id, String username, String senha,
                             PerfilTimao perfil, String usernameLogado) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraDoTimaoException("Usuário não encontrado."));

        String login = validarUsername(username);
        validarSenha(senha, false);
        validarPerfil(perfil);

        boolean ehProprioUsuario = existente.getUsername().equals(usernameLogado);

        // Proteção contra autoelevação: ninguém muda o próprio perfil
        if (ehProprioUsuario && perfil != existente.getPerfil()) {
            throw new RegraDoTimaoException("Você não pode alterar o seu próprio perfil.");
        }
        if (ehProprioUsuario && !login.equals(existente.getUsername())) {
            throw new RegraDoTimaoException("Você não pode alterar o seu próprio username.");
        }
        if (!login.equals(existente.getUsername()) && usuarioRepository.existsByUsername(login)) {
            throw new RegraDoTimaoException("Já existe um usuário com esse username.");
        }
        // O sistema nunca pode ficar sem ADMIN
        if (existente.getPerfil() == PerfilTimao.ADMIN && perfil != PerfilTimao.ADMIN
                && usuarioRepository.countByPerfil(PerfilTimao.ADMIN) <= 1) {
            throw new RegraDoTimaoException("Não é possível rebaixar o último ADMIN do sistema.");
        }

        existente.setUsername(login);
        existente.setPerfil(perfil);

        // Só atualiza a senha se uma nova foi informada
        if (senha != null && !senha.isBlank()) {
            existente.setPassword(passwordEncoder.encode(senha));
        }

        return usuarioRepository.save(existente);
    }

    private String validarUsername(String username) {
        String login = username == null ? "" : username.trim();
        if (!FORMATO_USERNAME.matcher(login).matches()) {
            throw new RegraDoTimaoException(
                    "Username deve ter de 3 a 30 caracteres (letras, números, ponto, hífen ou underline).");
        }
        return login;
    }

    private void validarSenha(String senha, boolean obrigatoria) {
        boolean vazia = senha == null || senha.isBlank();
        if (vazia && obrigatoria) {
            throw new RegraDoTimaoException("A senha é obrigatória.");
        }
        if (!vazia && senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraDoTimaoException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private void validarPerfil(PerfilTimao perfil) {
        if (perfil == null) {
            throw new RegraDoTimaoException("Selecione um perfil.");
        }
    }
}