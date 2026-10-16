package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(String operacao, String entidade, String entidadeId,
                          String campoAlterado, String valorAnterior, String novoValor, String justificativa) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null && auth.getName() != null) ? auth.getName() : "SISTEMA";

        String perfil = "DESCONHECIDO";
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            perfil = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .findFirst()
                    .orElse("DESCONHECIDO");
        }

        Auditoria registro = new Auditoria(
                usuario,
                perfil,
                operacao,
                entidade,
                entidadeId,
                campoAlterado,
                valorAnterior,
                novoValor,
                justificativa
        );

        auditoriaRepository.save(registro);
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAll();
    }

    public List<Auditoria> buscarPorEntidade(String entidade, String entidadeId) {
        return auditoriaRepository.findByEntidadeAndEntidadeId(entidade, entidadeId);
    }
}
