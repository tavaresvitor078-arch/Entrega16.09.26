package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.PerfilTimao;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        criarSeNaoExistir("admin", "Admin@2026", PerfilTimao.ADMIN);
        criarSeNaoExistir("gerente", "Gerente@2026", PerfilTimao.GERENTE);
        criarSeNaoExistir("atendente", "Atendente@2026", PerfilTimao.ATENDENTE);
    }

    private void criarSeNaoExistir(String username, String senha, PerfilTimao perfil) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }

        Usuario usuario = new Usuario(); 
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(senha)); 
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);

        log.info("Usuário inicial '{}' criado com o perfil {}.", username, perfil);
    }
}