package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(String usuario, String perfil, String operacao, String entidadeAfetada, 
                          String entidadeId, String campoModificado, String valorAnterior, 
                          String valorNovo, String justificativa) {
        
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setPerfil(perfil);
        auditoria.setOperacao(operacao);
        auditoria.setEntidadeAfetada(entidadeAfetada);
        auditoria.setEntidadeId(entidadeId);
        auditoria.setCampoModificado(campoModificado);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNovo(valorNovo);
        auditoria.setJustificativa(justificativa);
        auditoria.setDataHora(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy — HH:mm")));

        auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> listarAuditorias() {
        return auditoriaRepository.findAll();
    }
}