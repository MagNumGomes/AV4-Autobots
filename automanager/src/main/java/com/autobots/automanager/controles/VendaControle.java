package com.autobots.automanager.controles;

import com.autobots.automanager.DTO.AtualizarVendaDto;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.servicos.VendaServico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaControle {

    @Autowired
    private VendaServico vendaServico;

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarVenda(@RequestBody Venda venda, Authentication authentication) {
        try {
            vendaServico.cadastrarVenda(venda, authentication.getName());
            return ResponseEntity.created(null).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @PostMapping("/cadastrar/empresa/{idEmpresa}")
    public ResponseEntity<?> cadastrarVendaEmpresa(@PathVariable Long idEmpresa, @RequestBody Venda venda, Authentication authentication) {
        try {
            vendaServico.cadastrarVendaEmpresa(idEmpresa, venda, authentication.getName());
            return ResponseEntity.created(null).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @PostMapping("/cadastrar/usuario/{idUsuario}/{tipoUsuario}")
    public ResponseEntity<?> cadastrarVendaUsuario(@PathVariable Long idUsuario, @RequestBody Venda venda, @PathVariable String tipoUsuario, Authentication authentication) {
        try {
            vendaServico.cadastrarVendaUsuario(idUsuario, venda, tipoUsuario, authentication.getName());
            return ResponseEntity.created(null).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarVenda(@PathVariable Long id) {
        try {
            vendaServico.deletarVenda(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarVenda(@PathVariable Long id, @RequestBody AtualizarVendaDto venda) {
        try {
            vendaServico.atualizarVenda(id, venda);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/listar")
    public ResponseEntity<List<Venda>> listarVendas(Authentication authentication) {
        List<Venda> vendas = vendaServico.listarVendas(authentication.getName());
        if (vendas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(vendas);
    }

    public ResponseEntity<List<Venda>> listarVendas() {
        List<Venda> vendas = vendaServico.listarVendas();
        if (vendas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(vendas);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/visualizar/{id}")
    public ResponseEntity<?> visualizarVenda(@PathVariable Long id, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        try {
            Venda venda = (username != null)
                    ? vendaServico.visualizarVenda(id, username)
                    : vendaServico.visualizarVenda(id);
            if (venda == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(venda);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    public ResponseEntity<Venda> visualizarVenda(@PathVariable Long id) {
        Venda venda = vendaServico.visualizarVenda(id);
        if (venda == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(venda);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @GetMapping("/visualizar/empresa/{idEmpresa}")
    public ResponseEntity<?> listarVendasEmpresa(@PathVariable Long idEmpresa) {
        try {
            List<Venda> vendas = vendaServico.visualizarVendasEmpresa(idEmpresa);
            if (vendas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(vendas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/visualizar/usuario/{idUsuario}")
    public ResponseEntity<?> listarVendasUsuario(@PathVariable Long idUsuario, Authentication authentication) {
        try {
            List<Venda> vendas = vendaServico.visualizarVendasUsuario(idUsuario, authentication.getName());
            if (vendas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(vendas);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/vincular/{idVenda}/usuario/{idUsuario}/{tipoUsuario}")
    public ResponseEntity<?> vincularVendaUsuario(@PathVariable Long idVenda, @PathVariable Long idUsuario, @PathVariable String tipoUsuario) {
        try {
            vendaServico.vincularVendaUsuario(idVenda, idUsuario, tipoUsuario);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/vincular/{idVenda}/empresa/{idEmpresa}")
    public ResponseEntity<?> vincularVendaEmpresa(@PathVariable Long idVenda, @PathVariable Long idEmpresa) {
        try {
            vendaServico.vincularVendaEmpresa(idVenda, idEmpresa);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/desvincular/{idVenda}/usuario/{idUsuario}/{tipoUsuario}")
    public ResponseEntity<?> desvincularVendaUsuario(@PathVariable Long idVenda, @PathVariable Long idUsuario, @PathVariable String tipoUsuario) {
        try {
            vendaServico.desvincularVendaUsuario(idVenda, idUsuario, tipoUsuario);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/desvincular/{idVenda}/empresa/{idEmpresa}")
    public ResponseEntity<?> desvincularVendaEmpresa(@PathVariable Long idVenda, @PathVariable Long idEmpresa) {
        try {
            vendaServico.desvincularVendaEmpresa(idVenda, idEmpresa);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
