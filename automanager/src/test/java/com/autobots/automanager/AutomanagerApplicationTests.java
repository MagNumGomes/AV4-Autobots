package com.autobots.automanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AutomanagerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private String loginAndGetToken(String username, String password) throws Exception {
		String body = String.format("{\"nomeUsuario\": \"%s\", \"senha\": \"%s\"}", username, password);
		MvcResult res = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andReturn();
		String authHeader = res.getResponse().getHeader("Authorization");
		assertThat(authHeader).startsWith("Bearer ");
		return authHeader;
	}

	@Test
	void autenticacaoJwtERotasProtegidas() throws Exception {
		// Sem token -> 403 Forbidden
		mockMvc.perform(get("/usuarios/listar"))
				.andExpect(status().isForbidden());

		// Login com credenciais inválidas -> 401 Unauthorized
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nomeUsuario\": \"admin\", \"senha\": \"errada\"}"))
				.andExpect(status().isUnauthorized());

		// Login com admin -> 200 OK com token Bearer
		String adminToken = loginAndGetToken("admin", "admin123");

		mockMvc.perform(get("/usuarios/listar")
				.header("Authorization", adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5));
	}

	@Test
	void autorizacoesPorPerfilTabela1() throws Exception {
		String adminToken = loginAndGetToken("admin", "admin123");
		String gerenteToken = loginAndGetToken("dompedrofuncionario", "123456");
		String vendedorToken = loginAndGetToken("dompedrovendedor", "123456");
		String clienteToken = loginAndGetToken("dompedrocliente", "123456");

		// 1. CLIENTE: lê apenas seu próprio cadastro e vendas das quais foi consumidor
		MvcResult clienteListRes = mockMvc.perform(get("/usuarios/listar").header("Authorization", clienteToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].nomeSocial").value("Dom pedro cliente"))
				.andReturn();
		Number clienteIdNum = com.jayway.jsonpath.JsonPath.read(clienteListRes.getResponse().getContentAsString(), "$[0].id");
		long clienteId = clienteIdNum.longValue();
		long outroId = (clienteId == 1L) ? 2L : 1L;

		mockMvc.perform(get("/usuarios/visualizar/" + clienteId).header("Authorization", clienteToken))
				.andExpect(status().isOk());

		// Cliente tentando ler cadastro de outro usuário -> 403 Forbidden
		mockMvc.perform(get("/usuarios/visualizar/" + outroId).header("Authorization", clienteToken))
				.andExpect(status().isForbidden());

		// Cliente tentando ler serviços ou mercadorias -> 403 Forbidden
		mockMvc.perform(get("/servicos/listar").header("Authorization", clienteToken))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/mercadorias/listar").header("Authorization", clienteToken))
				.andExpect(status().isForbidden());

		// Cliente lendo suas próprias vendas -> 200 OK
		mockMvc.perform(get("/vendas/listar").header("Authorization", clienteToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));

		// 2. VENDEDOR: CRUD de usuários CLIENTE, leitura de serviços e mercadorias, criar/ler próprias vendas
		mockMvc.perform(get("/servicos/listar").header("Authorization", vendedorToken))
				.andExpect(status().isOk());
		mockMvc.perform(get("/mercadorias/listar").header("Authorization", vendedorToken))
				.andExpect(status().isOk());

		// Vendedor não pode criar serviço nem mercadoria -> 403 Forbidden
		mockMvc.perform(post("/servicos/cadastrar").header("Authorization", vendedorToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": \"Polimento\", \"valor\": 120.0}"))
				.andExpect(status().isForbidden());

		// Vendedor pode cadastrar usuário CLIENTE -> 201 Created
		String novoClienteJson = """
				{
				  "nome": "Maria Cliente",
				  "nomeSocial": "Maria",
				  "perfis": ["ROLE_CLIENTE"],
				  "credencial": { "nomeUsuario": "mariacliente", "senha": "123" }
				}
				""";
		mockMvc.perform(post("/usuarios/cadastrar").header("Authorization", vendedorToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoClienteJson))
				.andExpect(status().isCreated());

		// Vendedor NÃO pode cadastrar usuário GERENTE -> 403 Forbidden
		String novoGerenteJson = """
				{
				  "nome": "Gerente Teste",
				  "nomeSocial": "Gerente",
				  "perfis": ["ROLE_GERENTE"],
				  "credencial": { "nomeUsuario": "gerenteteste", "senha": "123" }
				}
				""";
		mockMvc.perform(post("/usuarios/cadastrar").header("Authorization", vendedorToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoGerenteJson))
				.andExpect(status().isForbidden());

		// Vendedor pode criar venda feita por si mesmo -> 201 Created
		String vendaVendedorJson = String.format("""
				{
				  "identificacao": "VENDA-VEND-01",
				  "cliente": { "id": %d },
				  "veiculo": { "id": 1 }
				}
				""", clienteId);
		mockMvc.perform(post("/vendas/cadastrar").header("Authorization", vendedorToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(vendaVendedorJson))
				.andExpect(status().isCreated());

		// Vendedor lê apenas as vendas feitas por si mesmo -> 1 venda
		mockMvc.perform(get("/vendas/listar").header("Authorization", vendedorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].identificacao").value("VENDA-VEND-01"));

		// 3. GERENTE: CRUD de usuários GERENTE/VENDEDOR/CLIENTE e CRUD de serviços, vendas e mercadorias
		mockMvc.perform(post("/usuarios/cadastrar").header("Authorization", gerenteToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoGerenteJson))
				.andExpect(status().isCreated());

		// Gerente NÃO pode cadastrar ADMIN -> 403 Forbidden
		String novoAdminJson = """
				{
				  "nome": "Outro Admin",
				  "nomeSocial": "Admin 2",
				  "perfis": ["ROLE_ADMIN"],
				  "credencial": { "nomeUsuario": "admin2", "senha": "123" }
				}
				""";
		mockMvc.perform(post("/usuarios/cadastrar").header("Authorization", gerenteToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoAdminJson))
				.andExpect(status().isForbidden());

		// Gerente pode cadastrar serviço e mercadoria -> 201 Created
		mockMvc.perform(post("/servicos/cadastrar").header("Authorization", gerenteToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": \"Polimento\", \"valor\": 120.0, \"descricao\": \"Polimento geral\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/mercadorias/cadastrar").header("Authorization", gerenteToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": \"Pneu Aro 15\", \"quantidade\": 10, \"valor\": 400.0}"))
				.andExpect(status().isCreated());

		// 4. ADMIN: CRUD completo na aplicação, incluindo usuários ADMIN e empresas
		mockMvc.perform(post("/usuarios/cadastrar").header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(novoAdminJson))
				.andExpect(status().isCreated());

		mockMvc.perform(put("/empresas/atualizar/1").header("Authorization", adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nomeFantasia\": \"Toyota Service Brasil\"}"))
				.andExpect(status().isOk());
	}
}
