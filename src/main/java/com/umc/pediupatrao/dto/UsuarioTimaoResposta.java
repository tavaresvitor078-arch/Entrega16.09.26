package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.PerfilTimao;
import com.umc.pediupatrao.entity.Usuario;

public record UsuarioTimaoResposta(String id, String username, PerfilTimao perfil) {

    public static UsuarioTimaoResposta de(Usuario u) {
        return new UsuarioTimaoResposta(u.getId(), u.getUsername(), u.getPerfil());
    }
}