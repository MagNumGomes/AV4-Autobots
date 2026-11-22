package com.autobots.automanager.servicos;

import com.autobots.automanager.DTO.AtualizarUsuarioDto;
import com.autobots.automanager.DTO.UsuarioDto;
import com.autobots.automanager.entitades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelos.adicionadores.AdicionadorLinkUsuario;
import com.autobots.automanager.modelos.atualizador.AtualizadorDocumento;
import com.autobots.automanager.modelos.atualizador.AtualizadorEmail;
import com.autobots.automanager.modelos.atualizador.AtualizadorTelefone;
import com.autobots.automanager.modelos.cadastrador.CadastradorUsuario;
import com.autobots.automanager.modelos.outros.SelecionadorUsuario;
import com.autobots.automanager.modelos.outros.VerificadorPermissao;
import com.autobots.automanager.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private AdicionadorLinkUsuario adicionadorLinkUsuario;

    @Autowired
    private RepositorioEmpresa repositorioEmpresa;

    @Autowired
    private RepositorioVenda repositorioVenda;

    @Autowired
    private RepositorioVeiculo repositorioVeiculo;

    @Autowired
    private AtualizadorDocumento atualizadorDocumento;

    @Autowired
    private AtualizadorTelefone atualizadorTelefone;

    @Autowired
    private AtualizadorEmail atualizadorEmail;

    @Autowired
    private SelecionadorUsuario selecionadorUsuario;

    @Autowired
    private VerificadorPermissao verificadorPermissao;

    public List<Usuario> listarUsuarios(String username) {
        List<Usuario> todosUsuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(todosUsuarios, username);
        if (usuarioLogado == null) {
            return new ArrayList<>();
        }

        List<Usuario> usuarios = new ArrayList<>();
        if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN)) {
            usuarios.addAll(todosUsuarios);
        } else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
            for (Usuario u : todosUsuarios) {
                if (verificadorPermissao.verificar(usuarioLogado.getPerfis(), u.getPerfis())) {
                    usuarios.add(u);
                }
            }
        } else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
            for (Usuario u : todosUsuarios) {
                if (verificadorPermissao.verificar(usuarioLogado.getPerfis(), u.getPerfis())) {
                    usuarios.add(u);
                }
            }
        } else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
            usuarios.add(usuarioLogado);
        }
        adicionadorLinkUsuario.adicionarLink(usuarios);
        return usuarios;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        adicionadorLinkUsuario.adicionarLink(usuarios);
        return usuarios;
    }

    public Usuario visualizarUsuario(Long id, String username) {
        List<Usuario> todosUsuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(todosUsuarios, username);
        Usuario usuario = repositorioUsuario.findById(id).orElse(null);
        if (usuario == null) {
            return null;
        }
        if (usuarioLogado != null) {
            boolean proprioUsuario = usuarioLogado.getId().equals(usuario.getId());
            boolean temPermissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.getPerfis());
            if (!proprioUsuario && !temPermissao) {
                throw new SecurityException("Usuário sem permissão para visualizar este cadastro");
            }
        }
        adicionadorLinkUsuario.adicionarLink(usuario);
        return usuario;
    }

    public Usuario visualizarUsuario(Long id) {
        Usuario usuario = repositorioUsuario.findById(id).orElse(null);
        if (usuario != null) {
            adicionadorLinkUsuario.adicionarLink(usuario);
        }
        return usuario;
    }

    public void cadastrarUsuario(String username, UsuarioDto usuario) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);

        boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.perfis());
        if (!permissao) {
            throw new SecurityException("Usuário sem permissão para cadastrar usuário com este perfil");
        }

        Usuario usuarioCadastrado = CadastradorUsuario.cadastrarUsuario(usuario);
        repositorioUsuario.save(usuarioCadastrado);
    }

    public void cadastrarUsuarioEmpresa(UsuarioDto usuario, Long idEmpresa, String username) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);

        boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.perfis());
        if (!permissao) {
            throw new SecurityException("Usuário sem permissão para cadastrar usuário com este perfil");
        }

        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        Usuario usuarioCadastrado = CadastradorUsuario.cadastrarUsuario(usuario);
        Usuario usuarioSalvo = repositorioUsuario.save(usuarioCadastrado);
        empresa.getUsuarios().add(usuarioSalvo);
        repositorioEmpresa.save(empresa);
    }

    public void vincularUsuarioEmpresa(Long idUsuario, Long idEmpresa, String username) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.getPerfis());
        if (!permissao) {
            throw new SecurityException("Usuário sem permissão para gerenciar este usuário");
        }

        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        empresa.getUsuarios().add(usuario);
        repositorioEmpresa.save(empresa);
    }

    public void desvincularUsuarioEmpresa(Long idUsuario, Long idEmpresa, String username) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);
        Usuario usuario = repositorioUsuario.findById(idUsuario).orElse(null);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.getPerfis());
        if (!permissao) {
            throw new SecurityException("Usuário sem permissão para gerenciar este usuário");
        }

        Empresa empresa = repositorioEmpresa.findById(idEmpresa).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não encontrada");
        }
        empresa.getUsuarios().removeIf(u -> u.getId().equals(idUsuario));
        repositorioEmpresa.save(empresa);
    }

    public ResponseEntity<?> atualizarUsuario(Long id, AtualizarUsuarioDto usuario, String username) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);
        Usuario usuarioAtual = repositorioUsuario.findById(id).orElse(null);

        if (usuarioAtual != null) {
            boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuarioAtual.getPerfis());
            if (!permissao) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário sem permissão para atualizar este cadastro");
            }
            if (usuario.perfis().isPresent()) {
                if (!verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.perfis().get())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário sem permissão para atribuir este perfil");
                }
                usuarioAtual.setPerfis(usuario.perfis().get());
            }
            if (usuario.nome().isPresent()) {
                usuarioAtual.setNome(usuario.nome().get());
            }
            if (usuario.nomeSocial().isPresent()) {
                usuarioAtual.setNomeSocial(usuario.nomeSocial().get());
            }
            if (usuario.telefones().isPresent()) {
                for (Telefone telefone : usuario.telefones().get()) {
                    if (telefone.getId() != null) {
                        for (Telefone telefoneAtual : usuarioAtual.getTelefones()) {
                            if (telefone.getId().equals(telefoneAtual.getId())) {
                                atualizadorTelefone.atualizarTelefone(telefoneAtual, telefone);
                            }
                        }
                    } else {
                        usuarioAtual.getTelefones().add(telefone);
                    }
                }
            }
            if (usuario.endereco().isPresent()) {
                usuarioAtual.setEndereco(usuario.endereco().get());
            }
            if (usuario.documentos().isPresent()) {
                for (Documento documento : usuario.documentos().get()) {
                    if (documento.getId() != null) {
                        for (Documento documentoAtual : usuarioAtual.getDocumentos()) {
                            if (documento.getId().equals(documentoAtual.getId())) {
                                atualizadorDocumento.atualizarDocumento(documentoAtual, documento);
                            }
                        }
                    } else {
                        if (documento.getDataEmissao() == null) {
                            documento.setDataEmissao(new Date());
                        }
                        usuarioAtual.getDocumentos().add(documento);
                    }
                }
            }
            if (usuario.emails().isPresent()) {
                for (Email email : usuario.emails().get()) {
                    if (email.getId() != null) {
                        for (Email emailAtual : usuarioAtual.getEmails()) {
                            if (email.getId().equals(emailAtual.getId())) {
                                atualizadorEmail.atualizarEmail(emailAtual, email);
                            }
                        }
                    } else {
                        usuarioAtual.getEmails().add(email);
                    }
                }
            }
            if (usuario.credencial().isPresent()) {
                Credencial nova = usuario.credencial().get();
                if (nova.getSenha() != null && !nova.getSenha().startsWith("$2a$")) {
                    nova.setSenha(new BCryptPasswordEncoder().encode(nova.getSenha()));
                }
                usuarioAtual.setCredencial(nova);
            }
            if (usuario.mercadorias().isPresent()) {
                usuarioAtual.getMercadorias().addAll(usuario.mercadorias().get());
            }
            if (usuario.vendas().isPresent()) {
                usuarioAtual.getVendas().addAll(usuario.vendas().get());
            }
            if (usuario.veiculos().isPresent()) {
                usuarioAtual.getVeiculos().addAll(usuario.veiculos().get());
            }
            repositorioUsuario.save(usuarioAtual);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public void deletarUsuario(Long id, String username) {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        Usuario usuarioLogado = selecionadorUsuario.selecionarUsername(usuarios, username);
        Usuario usuario = repositorioUsuario.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(), usuario.getPerfis());
        if (!permissao) {
            throw new SecurityException("Usuário sem permissão para deletar este usuário");
        }

        List<Venda> vendas = repositorioVenda.findAll();
        for (Venda venda : vendas) {
            boolean alterada = false;
            if (venda.getCliente() != null && venda.getCliente().getId().equals(id)) {
                venda.setCliente(null);
                alterada = true;
            }
            if (venda.getFuncionario() != null && venda.getFuncionario().getId().equals(id)) {
                venda.setFuncionario(null);
                alterada = true;
            }
            if (alterada) {
                repositorioVenda.save(venda);
            }
        }

        List<Veiculo> veiculos = repositorioVeiculo.findAll();
        for (Veiculo veiculo : veiculos) {
            if (veiculo.getProprietario() != null && veiculo.getProprietario().getId().equals(id)) {
                veiculo.setProprietario(null);
                repositorioVeiculo.save(veiculo);
            }
        }

        List<Empresa> empresas = repositorioEmpresa.findAll();
        for (Empresa empresa : empresas) {
            if (empresa.getUsuarios().removeIf(u -> u.getId().equals(id))) {
                repositorioEmpresa.save(empresa);
            }
        }

        usuario.getVeiculos().clear();
        usuario.getVendas().clear();
        usuario.getDocumentos().clear();
        usuario.setEndereco(null);
        usuario.getTelefones().clear();
        usuario.getEmails().clear();
        usuario.setCredencial(null);
        usuario.getMercadorias().clear();
        repositorioUsuario.save(usuario);

        repositorioUsuario.deleteById(id);
    }
}
