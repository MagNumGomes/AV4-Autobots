package com.autobots.automanager.modelos.cadastrador;

import com.autobots.automanager.DTO.UsuarioDto;
import com.autobots.automanager.entitades.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CadastradorUsuario {

    private static String codificarSenha(String senha) {
        if (senha == null) {
            return "";
        }
        if (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$")) {
            return senha;
        }
        return new BCryptPasswordEncoder().encode(senha);
    }

    public static Usuario cadastrarUsuario(UsuarioDto usuario){
        Usuario setUsario = new Usuario();
        setUsario.setNome(usuario.nome());
        setUsario.setNomeSocial(usuario.nomeSocial());
        if (usuario.perfis() != null) {
            setUsario.setPerfis(usuario.perfis());
        }

        if (usuario.telefones() != null) {
            for (Telefone telefone : usuario.telefones()){
                Telefone setTelefone = new Telefone();
                setTelefone.setDdd(telefone.getDdd());
                setTelefone.setNumero(telefone.getNumero());
                setUsario.getTelefones().add(setTelefone);
            }
        }

        if (usuario.endereco() != null) {
            Endereco setEndereco = new Endereco();
            setEndereco.setEstado(usuario.endereco().getEstado());
            setEndereco.setCidade(usuario.endereco().getCidade());
            setEndereco.setBairro(usuario.endereco().getBairro());
            setEndereco.setRua(usuario.endereco().getRua());
            setEndereco.setNumero(usuario.endereco().getNumero());
            setEndereco.setCodigoPostal(usuario.endereco().getCodigoPostal());
            if (usuario.endereco().getInformacoesAdicionais() != null){
                setEndereco.setInformacoesAdicionais(usuario.endereco().getInformacoesAdicionais());
            }
            setUsario.setEndereco(setEndereco);
        }

        if (usuario.documentos() != null) {
            for (Documento documento : usuario.documentos()){
                Documento setDocumento = new Documento();
                setDocumento.setTipo(documento.getTipo());
                setDocumento.setNumero(documento.getNumero());
                setDocumento.setDataEmissao(documento.getDataEmissao() != null ? documento.getDataEmissao() : new Date());
                setUsario.getDocumentos().add(setDocumento);
            }
        }

        if (usuario.emails() != null) {
            for (Email email : usuario.emails()){
                Email setEmail = new Email();
                setEmail.setEndereco(email.getEndereco());
                setUsario.getEmails().add(setEmail);
            }
        }

        if (usuario.credencial() != null){
            Credencial credencial = new Credencial();
            credencial.setNomeUsuario(usuario.credencial().getNomeUsuario());
            credencial.setSenha(codificarSenha(usuario.credencial().getSenha()));
            credencial.setInativo(false);
            setUsario.setCredencial(credencial);
        }

        if (usuario.mercadorias() != null) {
            for (Mercadoria mercadoria : usuario.mercadorias()){
                Mercadoria setMercadoria = new Mercadoria();
                setMercadoria.setNome(mercadoria.getNome());
                setMercadoria.setValor(mercadoria.getValor());
                setMercadoria.setQuantidade(mercadoria.getQuantidade());
                setMercadoria.setCadastro(mercadoria.getCadastro() != null ? mercadoria.getCadastro() : new Date());
                setMercadoria.setFabricao(mercadoria.getFabricao() != null ? mercadoria.getFabricao() : new Date());
                setMercadoria.setValidade(mercadoria.getValidade() != null ? mercadoria.getValidade() : new Date());
                setMercadoria.setDescricao(mercadoria.getDescricao());
                setUsario.getMercadorias().add(setMercadoria);
            }
        }

        if (usuario.vendas() != null){
            setUsario.setVendas(usuario.vendas());
        }

        if (usuario.veiculos() != null){
            for (Veiculo veiculo : usuario.veiculos()) {
                veiculo.setProprietario(setUsario);
                setUsario.getVeiculos().add(veiculo);
            }
        }
        return setUsario;
    }

    public Usuario cadastrarUsuario(Usuario usuario){
        Usuario setUsario = new Usuario();
        setUsario.setNome(usuario.getNome());
        setUsario.setNomeSocial(usuario.getNomeSocial());
        if (usuario.getPerfis() != null) {
            setUsario.setPerfis(usuario.getPerfis());
        }

        if (usuario.getTelefones() != null) {
            for (Telefone telefone : usuario.getTelefones()){
                Telefone setTelefone = new Telefone();
                setTelefone.setDdd(telefone.getDdd());
                setTelefone.setNumero(telefone.getNumero());
                setUsario.getTelefones().add(setTelefone);
            }
        }

        if (usuario.getEndereco() != null) {
            Endereco setEndereco = new Endereco();
            setEndereco.setEstado(usuario.getEndereco().getEstado());
            setEndereco.setCidade(usuario.getEndereco().getCidade());
            setEndereco.setBairro(usuario.getEndereco().getBairro());
            setEndereco.setRua(usuario.getEndereco().getRua());
            setEndereco.setNumero(usuario.getEndereco().getNumero());
            setEndereco.setCodigoPostal(usuario.getEndereco().getCodigoPostal());
            if (usuario.getEndereco().getInformacoesAdicionais() != null){
                setEndereco.setInformacoesAdicionais(usuario.getEndereco().getInformacoesAdicionais());
            }
            setUsario.setEndereco(setEndereco);
        }

        if (usuario.getDocumentos() != null) {
            for (Documento documento : usuario.getDocumentos()){
                Documento setDocumento = new Documento();
                setDocumento.setTipo(documento.getTipo());
                setDocumento.setNumero(documento.getNumero());
                setDocumento.setDataEmissao(documento.getDataEmissao() != null ? documento.getDataEmissao() : new Date());
                setUsario.getDocumentos().add(setDocumento);
            }
        }

        if (usuario.getEmails() != null) {
            for (Email email : usuario.getEmails()){
                Email setEmail = new Email();
                setEmail.setEndereco(email.getEndereco());
                setUsario.getEmails().add(setEmail);
            }
        }

        if (usuario.getCredencial() != null) {
            Credencial credencial = new Credencial();
            credencial.setNomeUsuario(usuario.getCredencial().getNomeUsuario());
            credencial.setSenha(codificarSenha(usuario.getCredencial().getSenha()));
            credencial.setInativo(false);
            setUsario.setCredencial(credencial);
        }

        if (usuario.getMercadorias() != null){
            for (Mercadoria mercadoria : usuario.getMercadorias()){
                Mercadoria setMercadoria = new Mercadoria();
                setMercadoria.setNome(mercadoria.getNome());
                setMercadoria.setValor(mercadoria.getValor());
                setMercadoria.setQuantidade(mercadoria.getQuantidade());
                setMercadoria.setCadastro(mercadoria.getCadastro() != null ? mercadoria.getCadastro() : new Date());
                setMercadoria.setFabricao(mercadoria.getFabricao() != null ? mercadoria.getFabricao() : new Date());
                setMercadoria.setValidade(mercadoria.getValidade() != null ? mercadoria.getValidade() : new Date());
                setMercadoria.setDescricao(mercadoria.getDescricao());
                setUsario.getMercadorias().add(setMercadoria);
            }
        }

        if (usuario.getVendas() != null){
            setUsario.setVendas(usuario.getVendas());
        }

        if (usuario.getVeiculos() != null){
            for (Veiculo veiculo : usuario.getVeiculos()) {
                veiculo.setProprietario(setUsario);
                setUsario.getVeiculos().add(veiculo);
            }
        }

        return setUsario;
    }
}
