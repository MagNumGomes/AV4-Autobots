# Autobots — atualização de segurança (ATVIV)

Microsserviço REST em Java com Spring Boot, Spring Security, JSON Web Token (JWT) e HATEOAS para gestão de lojas de manutenção veicular e autopeças, implementando controle de autenticação e autorização baseado nos perfis da Tabela 1 (`ROLE_ADMIN`, `ROLE_GERENTE`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`).

## Requisitos e execução

- Java 17 ou superior
- Maven Wrapper (`automanager/mvnw.cmd`)

PowerShell:

```powershell
cd automanager
./mvnw.cmd spring-boot:run
```

## Autenticação JWT (`POST /login`)

O filtro `Autenticador` (`UsernamePasswordAuthenticationFilter`) autentica credenciais via `POST /login` e retorna o token JWT no cabeçalho `Authorization: Bearer <token>`.

Usuários carregados inicialmente em `AutomanagerApplication`:

| Usuário | Senha | Perfil |
| --- | --- | --- |
| `admin` | `admin123` | `ROLE_ADMIN` |
| `dompedrofuncionario` | `123456` | `ROLE_GERENTE` |
| `dompedrovendedor` | `123456` | `ROLE_VENDEDOR` |
| `dompedrocliente` | `123456` | `ROLE_CLIENTE` |
| `dompedrofornecedor` | `123456` | `ROLE_FORNECEDOR` |

## Perfis e Autorizações (Tabela 1)

- **Administrador (`ROLE_ADMIN`)**: Todas as operações de CRUD na aplicação, incluindo adicionar ou remover usuários administradores, empresas e credenciais.
- **Gerente (`ROLE_GERENTE`)**: Todas as operações de CRUD sobre usuários dos perfis gerente, vendedor e cliente, além de todas as operações de CRUD sobre serviços, vendas e mercadorias.
- **Vendedor (`ROLE_VENDEDOR`)**: Todas as operações de CRUD sobre usuários do perfil cliente, leitura de serviços e mercadorias, e criação/leitura de vendas realizadas por si mesmo.
- **Cliente (`ROLE_CLIENTE`)**: Leitura de informações sobre o seu próprio cadastro e leitura das vendas das quais foi consumidor.

## Testes

```powershell
cd automanager
./mvnw.cmd test
```
