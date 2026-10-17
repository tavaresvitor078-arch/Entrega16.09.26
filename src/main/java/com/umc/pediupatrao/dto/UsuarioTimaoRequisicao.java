package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.PerfilTimao;

public record UsuarioTimaoRequisicao(String username, String password, PerfilTimao perfil) {
}