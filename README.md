# Projeto Integrador - Microsserviços Java

Alunos: Gabriel Nogueira dos Santos - RA: 1127692
Ronaldo Castelani - RA:1130584

Neste projeto temos quatro serviços que conversam entre si. Usamos Consul, PostgreSQL e Flyway.

- gateway-api (porta 8080): é a porta de entrada. Todas as requisições passam por ele.
- auth-api (porta 8200): faz o cadastro e o login dos usuários.
- product-api (porta 8000): cuida dos produtos.
- currency-api (porta 8100): informa a cotação das moedas.

## Como rodar

1. Deixe o PostgreSQL ligado e crie os bancos db_product, db_currency e db_user.
2. Coloque o consul.exe na pasta microservices e abra o start_consul.bat.
3. Rode os serviços nesta ordem: auth-api, currency-api, product-api e, por último, gateway-api.

## Como testar

Use o arquivo testes.http no IntelliJ. As requisições sempre vão pelo gateway (porta 8080).

- Login: POST /auth/signin com admin@admin.dev e senha teste. Devolve um token.
- Cadastrar usuário comum: POST /auth/signup.
- Ver produto: GET /products/1?targetCurrency=EUR (não precisa de login).
- Cadastrar produto: POST /ws/products, enviando o token no cabeçalho Authorization (Bearer).
  - Sem token: erro 401.
  - Usuário comum: erro 403.
  - Administrador: 201, produto criado.

## O que foi feito

1. Criei o currency-api, que informa a cotação entre moedas. Ele segue o mesmo modelo do product-api.
2. Fiz o product-api buscar a cotação no currency-api. Coloquei proteções para quando o currency-api falha ou demora: valor padrão, limite de tempo, novas tentativas, circuit breaker (para de chamar o serviço depois de várias falhas) e cache.
3. Fiz o currency-api buscar a cotação no site do Banco Central, com as mesmas proteções. Se o Banco Central falhar ou não tiver a cotação, ele usa o banco de dados local. Se não achar em nenhum dos dois, devolve erro 404.
4. Criei o gateway-api e o login com token (JWT). O gateway confere o token nas rotas que começam com /ws/. O product-api só deixa o administrador cadastrar produtos.
