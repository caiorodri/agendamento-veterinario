# API Agendamento Veterinário

API REST para gerenciamento de agendamentos em clínicas veterinárias, desenvolvida em Java com Spring Boot e MySQL.

-----

## 🚀 Tecnologias Utilizadas

  * **Java 21**
  * **Spring Boot** (Web, Data JPA, Security, Mail)
  * **MySQL**
  * **Maven**
  * **Lombok**
  * **MapStruct**
  * **Swagger** (via springdoc-openapi)

-----

## 📋 Funcionalidades Principais

  * **Gerenciamento Completo:** Consulta, cadastro, atualização e remoção de usuários, animais e agendamentos.
  * **Notificações por E-mail:** Envio de e-mails para notificações importantes.
  * **Design RESTful:** Endpoints RESTful desenvolvidos seguindo boas práticas.
  * **Documentação Interativa:** Acesso fácil à documentação da API via Swagger UI.

-----

## 💾 Banco de Dados

  * **Modelo Relacional:** Estrutura de banco de dados organizada com tabelas para Usuário, Animal, Agendamento, Status, Tipo, e outras entidades relacionadas.
  * **Scripts SQL:** Os scripts SQL necessários para a criação da base de dados estão disponíveis na pasta `/scripts`.

-----

## ⚙️ Como Rodar a Aplicação

### Pré-requisitos

Para executar a aplicação, certifique-se de ter os seguintes itens instalados:

  * **Java 21**
  * **MySQL**
  * **Maven**

### Configuração do Banco de Dados

Siga os passos abaixo para configurar o banco de dados. Informe sua senha quando solicitado.

1.  Execute o script SQL para criar o banco de dados, as tabelas e as estruturas necessárias. Substitua `seu_usuario` pelo seu nome de usuário do MySQL e `bd_agendamento_veterinario.sql` pelo nome do arquivo SQL apropriado dentro da pasta `scripts` ( caso o nome esteja diferente ).

    * **Prompt de comando:**

        ```cmd
        mysql -u seu_usuario -p < scripts\bd_agendamento_veterinario.sql
        ```

    Agora o banco de dados está configurado e pronto para ser utilizado pela aplicação.

### Configuração da Aplicação

Configure as propriedades de conexão com o banco de dados e as credenciais do servidor de e-mail no arquivo `src/main/resources/application.yml`. Veja um exemplo:

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agendamento_veterinario?useSSL=false&serverTimezone=UTC
    username: seu_usuario
    password: sua_senha

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

  mail:
    host: smtp.gmail.com
    port: 587
    username: seu_email@gmail.com
    password: sua_senha_app
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**Observação sobre `sua_senha_app`:** Para o `spring.mail.password` ao usar o Gmail, é altamente recomendável gerar uma **senha de aplicativo** em suas configurações de segurança do Google, em vez de usar a senha da sua conta principal, para maior segurança.

### Executando a Aplicação

Siga estes passos para clonar o repositório e executar a aplicação:

1.  Clone o repositório:

    ```bash
    git clone https://github.com/caiorodri/agendamento-veterinario.git
    cd agendamento-veterinario
    ```

2.  Rode a aplicação usando Maven:

    ```bash
    mvn spring-boot:run
    ```

A API estará disponível em:

```text
http://localhost:8080/agendamento-veterinario
```

A documentação Swagger pode ser acessada em:

```bash
http://localhost:8080/agendamento-veterinario/swagger-ui.html
```

-----

## 📖 Uso da API

Todos os endpoints da API estão detalhadamente documentados no Swagger UI, incluindo exemplos de requisição e resposta para facilitar a integração.

