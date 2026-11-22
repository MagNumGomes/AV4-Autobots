package com.autobots.automanager.servicos;

import com.autobots.automanager.DTO.AtualizarVendaDto;
import com.autobots.automanager.entitades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelos.adicionadores.AdicionadorLinkVenda;
import com.autobots.automanager.modelos.atualizador.AtualizadorMercadoria;
import com.autobots.automanager.modelos.atualizador.AtualizadorServico;
import com.autobots.automanager.modelos.cadastrador.CadastradorVenda;
import com.autobots.automanager.modelos.outros.SelecionadorUsuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VendaServico {

    @Autowired
    private RepositorioVenda repositorioVenda;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioVeiculo repositorioVeiculo;

    @Autowired
    private CadastradorVenda cadastradorVenda;

    @Autowired
    private AdicionadorLinkVenda adicionadorLinkVenda;

    @Autowired
    private AtualizadorMercadoria atualizadorMercadoria;

    @Autowired
    private AtualizadorServico atualizadorServico;

    @Autowired
    private SelecionadorUsuario selecionadorUsuario;

    private void validarPermissaoCriacaoVenda(Venda venda, String username) {
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(repositorioUsuario.findAll(), username);
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado");
        }
        boolean isAdminOuGerente = usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE);
        if (!isAdminOuGerente && usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
            if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null
                    && !venda.getFuncionario().getId().equals(usuarioLogado.getId())) {
                throw new SecurityException("Vendedor só tem autorização para criar vendas feitas por si mesmo");
            }
            venda.setFuncionario(usuarioLogado);
        }
    }

    public void cadastrarVenda(Venda venda, String username) {
        validarPermissaoCriacaoVenda(venda, username);
        Venda vendaCadastrada = cadastradorVenda.cadastrarVenda(venda);
        repositorioVenda.save(vendaCadastrada);
    }

    public void cadastrarVendaEmpresa(Long idEmpresa, Venda venda, String username) {
        validarPermissaoCriacaoVenda(venda, username);
        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        Venda vendaCadastrada = cadastradorVenda.cadastrarVenda(venda);
        empresa.getVendas().add(vendaCadastrada);
        repositorioEmpresa.save(empresa);
    }

    public void cadastrarVendaUsuario(Long idUsuario, Venda venda, String tipoUsuario, String username) {
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(repositorioUsuario.findAll(), username);
        boolean isAdminOuGerente = usuarioLogado != null && (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE));
        if (!isAdminOuGerente && usuarioLogado != null && usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
            if (tipoUsuario.equalsIgnoreCase("funcionario") && !usuarioLogado.getId().equals(idUsuario)) {
                throw new SecurityException("Vendedor só tem autorização para criar vendas feitas por si mesmo");
            }
            if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null
                    && !venda.getFuncionario().getId().equals(usuarioLogado.getId())) {
                throw new SecurityException("Vendedor só tem autorização para criar vendas feitas por si mesmo");
            }
            venda.setFuncionario(usuarioLogado);
        }
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        Venda vendaCadastrada = cadastradorVenda.cadastrarVenda(venda);
        if (tipoUsuario.equalsIgnoreCase("cliente")) {
            vendaCadastrada.setCliente(usuario);
        } else if (tipoUsuario.equalsIgnoreCase("funcionario")) {
            vendaCadastrada.setFuncionario(usuario);
        }
        repositorioVenda.save(vendaCadastrada);
        usuario.getVendas().add(vendaCadastrada);
        repositorioUsuario.save(usuario);
    }

    public void deletarVenda(Long id) {
        Venda venda = repositorioVenda.findById(id).orElse(null);
        if (venda == null) {
            throw new IllegalArgumentException("Venda não encontrada");
        }

        List<Usuario> usuarios = repositorioUsuario.findAll();
        for (Usuario usuario : usuarios) {
            if (usuario.getVendas().removeIf(v -> v.getId().equals(id))) {
                repositorioUsuario.save(usuario);
            }
        }

        List<Empresa> empresas = repositorioEmpresa.findAll();
        for (Empresa empresa : empresas) {
            if (empresa.getVendas().removeIf(v -> v.getId().equals(id))) {
                repositorioEmpresa.save(empresa);
            }
        }

        List<Veiculo> veiculos = repositorioVeiculo.findAll();
        for (Veiculo veiculo : veiculos) {
            if (veiculo.getVendas().removeIf(v -> v.getId().equals(id))) {
                repositorioVeiculo.save(veiculo);
            }
        }

        venda.setCliente(null);
        venda.setFuncionario(null);
        venda.setVeiculo(null);
        venda.getMercadorias().clear();
        venda.getServicos().clear();
        repositorioVenda.save(venda);

        repositorioVenda.delete(venda);
    }

    public void atualizarVenda(Long vendaId, AtualizarVendaDto venda) {
        Venda vendaAtualizada = repositorioVenda.findById(vendaId)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
        if (venda.identificacao().isPresent()) {
            vendaAtualizada.setIdentificacao(venda.identificacao().get());
        }
        if (venda.cliente().isPresent()) {
            Usuario cliente = venda.cliente().get();
            if (cliente.getId() != null) {
                cliente = repositorioUsuario.findById(cliente.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
            }
            vendaAtualizada.setCliente(cliente);
        }
        if (venda.funcionario().isPresent()) {
            Usuario funcionario = venda.funcionario().get();
            if (funcionario.getId() != null) {
                funcionario = repositorioUsuario.findById(funcionario.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
            }
            vendaAtualizada.setFuncionario(funcionario);
        }
        if (venda.mercadorias().isPresent()) {
            for (Mercadoria mercadoria : venda.mercadorias().get()) {
                if (mercadoria.getId() != null) {
                    for (Mercadoria mercadoriaAtual : vendaAtualizada.getMercadorias()) {
                        if (mercadoriaAtual.getId().equals(mercadoria.getId())) {
                            atualizadorMercadoria.atualizarMercadoria(mercadoriaAtual, mercadoria);
                        }
                    }
                }
            }
        }
        if (venda.servicos().isPresent()) {
            for (Servico servico : venda.servicos().get()) {
                if (servico.getId() != null) {
                    for (Servico servicoAtual : vendaAtualizada.getServicos()) {
                        if (servicoAtual.getId().equals(servico.getId())) {
                            atualizadorServico.atualizarServico(servicoAtual, servico);
                        }
                    }
                }
            }
        }
        if (venda.veiculo().isPresent()) {
            Veiculo veiculo = venda.veiculo().get();
            if (veiculo.getId() != null) {
                veiculo = repositorioVeiculo.findById(veiculo.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));
            }
            vendaAtualizada.setVeiculo(veiculo);
        }

        repositorioVenda.save(vendaAtualizada);
    }

    public List<Venda> listarVendas(String username) {
        List<Venda> todasVendas = repositorioVenda.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(repositorioUsuario.findAll(), username);
        if (usuarioLogado == null) {
            return new ArrayList<>();
        }
        List<Venda> vendas = new ArrayList<>();
        if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
            vendas.addAll(todasVendas);
        } else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
            for (Venda v : todasVendas) {
                if (v.getFuncionario() != null && v.getFuncionario().getId().equals(usuarioLogado.getId())) {
                    vendas.add(v);
                }
            }
        } else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
            for (Venda v : todasVendas) {
                if (v.getCliente() != null && v.getCliente().getId().equals(usuarioLogado.getId())) {
                    vendas.add(v);
                }
            }
        }
        adicionadorLinkVenda.adicionarLink(vendas);
        return vendas;
    }

    public List<Venda> listarVendas() {
        List<Venda> vendas = repositorioVenda.findAll();
        adicionadorLinkVenda.adicionarLink(vendas);
        return vendas;
    }

    public Venda visualizarVenda(Long id, String username) {
        Venda venda = repositorioVenda.findById(id).orElse(null);
        if (venda == null) {
            return null;
        }
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(repositorioUsuario.findAll(), username);
        if (usuarioLogado != null) {
            boolean isAdminOuGerente = usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                    || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE);
            if (!isAdminOuGerente) {
                boolean isVendedorDaVenda = usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)
                        && venda.getFuncionario() != null && venda.getFuncionario().getId().equals(usuarioLogado.getId());
                boolean isClienteDaVenda = usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)
                        && venda.getCliente() != null && venda.getCliente().getId().equals(usuarioLogado.getId());
                if (!isVendedorDaVenda && !isClienteDaVenda) {
                    throw new SecurityException("Usuário sem permissão para visualizar esta venda");
                }
            }
        }
        adicionadorLinkVenda.adicionarLink(venda);
        return venda;
    }

    public Venda visualizarVenda(Long id) {
        Venda venda = repositorioVenda.findById(id).orElse(null);
        if (venda != null) {
            adicionadorLinkVenda.adicionarLink(venda);
        }
        return venda;
    }

    public List<Venda> visualizarVendasEmpresa(Long idEmpresa) {
        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        Set<Venda> vendas = empresa.getVendas();
        List<Venda> vendasLista = new ArrayList<>(vendas);
        adicionadorLinkVenda.adicionarLink(vendasLista);
        return vendasLista;
    }

    public List<Venda> visualizarVendasUsuario(Long idUsuario, String username) {
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(repositorioUsuario.findAll(), username);
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (usuarioLogado != null) {
            boolean isAdminOuGerente = usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)
                    || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE);
            if (!isAdminOuGerente && !usuarioLogado.getId().equals(idUsuario)) {
                throw new SecurityException("Usuário só tem permissão para consultar suas próprias vendas");
            }
        }
        Set<Venda> vendas = new LinkedHashSet<>(usuario.getVendas());
        for (Venda v : repositorioVenda.findAll()) {
            if ((v.getCliente() != null && v.getCliente().getId().equals(idUsuario))
                    || (v.getFuncionario() != null && v.getFuncionario().getId().equals(idUsuario))) {
                vendas.add(v);
            }
        }
        List<Venda> vendasLista = new ArrayList<>(vendas);
        adicionadorLinkVenda.adicionarLink(vendasLista);
        return vendasLista;
    }

    public void vincularVendaUsuario(Long idVenda, Long idUsuario, String tipoUsuario) {
        Venda venda = repositorioVenda.findById(idVenda).orElse(null);
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);
        if (venda == null) {
            throw new IllegalArgumentException("Venda não encontrada");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (tipoUsuario.equalsIgnoreCase("cliente")) {
            venda.setCliente(usuario);
        } else if (tipoUsuario.equalsIgnoreCase("funcionario")) {
            venda.setFuncionario(usuario);
        }
        repositorioVenda.save(venda);
        usuario.getVendas().add(venda);
        repositorioUsuario.save(usuario);
    }

    public void vincularVendaEmpresa(Long idVenda, Long idEmpresa) {
        Venda venda = repositorioVenda.findById(idVenda).orElse(null);
        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (venda == null) {
            throw new IllegalArgumentException("Venda não encontrada");
        }
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        empresa.getVendas().add(venda);
        repositorioEmpresa.save(empresa);
    }

    public void desvincularVendaUsuario(Long idVenda, Long idUsuario, String tipoUsuario) {
        Venda venda = repositorioVenda.findById(idVenda).orElse(null);
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);
        if (venda == null) {
            throw new IllegalArgumentException("Venda não encontrada");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (tipoUsuario.equalsIgnoreCase("cliente")) {
            venda.setCliente(null);
        } else if (tipoUsuario.equalsIgnoreCase("funcionario")) {
            venda.setFuncionario(null);
        }
        repositorioVenda.save(venda);
        usuario.getVendas().removeIf(v -> v.getId().equals(idVenda));
        repositorioUsuario.save(usuario);
    }

    public void desvincularVendaEmpresa(Long idVenda, Long idEmpresa) {
        Venda venda = repositorioVenda.findById(idVenda).orElse(null);
        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (venda == null) {
            throw new IllegalArgumentException("Venda não encontrada");
        }
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        empresa.getVendas().removeIf(v -> v.getId().equals(idVenda));
        repositorioEmpresa.save(empresa);
    }
}
