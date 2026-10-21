# Atividades Avaliativas - Microsserviços Java

Alunos: Gabriel Nogueira dos Santos - RA: 1127692
        Ronaldo Castelani - RA:

Projeto com quatro serviços, usando Consul, PostgreSQL e Flyway:

- `gateway-api` (porta 8080): entrada única. Valida o login (JWT) nas rotas protegidas.
- `auth-api` (porta 8200): cadastro e login de usuários.
- `product-api` (porta 8000): produtos.
- `currency-api` (porta 8100): cotações de moedas.

## Como executar

1. Ter o PostgreSQL rodando com os bancos `db_product`, `db_currency` e `db_user`.
2. Rodar o `microservices/start_consul.bat`.
3. Subir `auth-api`, `currency-api`, `product-api` e por último o `gateway-api`.

## Testes (sempre pelo gateway, porta 8080)

- Login: `POST http://localhost:8080/auth/signin` com `admin@admin.dev` / `teste` (devolve o token JWT).
- Cadastro de usuário comum: `POST http://localhost:8080/auth/signup`.
- Produto: `GET http://localhost:8080/products/1?targetCurrency=EUR`.
- Cadastrar produto (rota protegida): `POST http://localhost:8080/ws/products` com o token em Authorization (Bearer).
  - Sem token: 401. Usuário comum: 403. Administrador: 201.

## Atividades realizadas

1. Criei o currency-api, que informa a cotação entre moedas. Ele segue o mesmo modelo do product-api.
2. Fiz o product-api buscar a cotação no currency-api. Adicionei proteções para quando o currency-api falha ou demora: valor padrão, limite de tempo, novas tentativas, circuit breaker (pausa as chamadas depois de várias falhas) e cache.
3. Fiz o currency-api buscar a cotação no site do Banco Central, usando as mesmas proteções. Se o Banco Central falhar ou não tiver a cotação, ele usa o banco de dados local. Se não achar em nenhum dos dois, devolve erro 404.
4. Criei o gateway-api e o login com JWT. O gateway confere o token nas rotas que começam com `/ws/` e envia os dados do usuário aos serviços. O product-api só deixa o administrador cadastrar produtos.
