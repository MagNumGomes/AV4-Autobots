package com.autobots.automanager.modelos.outros;

import com.autobots.automanager.enumeracoes.PerfilUsuario;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class VerificadorPermissao {
    public boolean verificar(Set<PerfilUsuario> cargoUsuario, Set<PerfilUsuario> permissao) {
        if (cargoUsuario == null || cargoUsuario.isEmpty() || permissao == null || permissao.isEmpty()) {
            return false;
        }
        if (cargoUsuario.contains(PerfilUsuario.ROLE_ADMIN)) {
            return true;
        }
        if (cargoUsuario.contains(PerfilUsuario.ROLE_GERENTE)) {
            for (PerfilUsuario perfilAlvo : permissao) {
                if (perfilAlvo != PerfilUsuario.ROLE_GERENTE
                        && perfilAlvo != PerfilUsuario.ROLE_VENDEDOR
                        && perfilAlvo != PerfilUsuario.ROLE_CLIENTE) {
                    return false;
                }
            }
            return true;
        }
        if (cargoUsuario.contains(PerfilUsuario.ROLE_VENDEDOR)) {
            for (PerfilUsuario perfilAlvo : permissao) {
                if (perfilAlvo != PerfilUsuario.ROLE_CLIENTE) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}