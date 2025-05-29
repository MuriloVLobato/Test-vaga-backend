# Test-vaga-backend

API RESTful desenvolvida em Java com Spring Boot para gerenciamento e consulta de Pontos de Interesse (POIs). Permite cadastrar POIs com coordenadas (x, y) e listar todos os POIs ou apenas aqueles próximos a uma determinada localização, dentro de um raio especificado.

## ✨ Funcionalidades

* Cadastro de novos Pontos de Interesse.
* Listagem de todos os Pontos de Interesse cadastrados.
* Listagem de Pontos de Interesse próximos a uma coordenada (x, y) e um raio (d_max) definidos.

## 💻 Tecnologias Utilizadas

* Java 17
* Spring Boot 3.5.0 (incluindo Spring Web, Spring Data JPA)
* Maven (Gerenciador de dependências)
* MySQL (Banco de Dados Relacional)
* Hibernate (Framework ORM)

## ⚙️ Pré-requisitos

Antes de começar, você precisará ter instalado em sua máquina:

* [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) ou superior.
* [Apache Maven](https://maven.apache.org/download.cgi) 3.6.x ou superior.
* [MySQL Server](https://dev.mysql.com/downloads/mysql/) (Recomendado versão 8.x).
* Uma IDE de sua preferência (IntelliJ IDEA, Eclipse, VS Code com extensões Java).
* Uma ferramenta para testar APIs, como [Postman](https://www.postman.com/) ou [Insomnia](https://insomnia.rest/).

## 🛠️ Configuração do Ambiente

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/MuriloVLobato/Test-vaga-backend.git](https://github.com/MuriloVLobato/Test-vaga-backend.git)
    cd Test-vaga-backend
    ```

2.  **Configure o Banco de Dados MySQL:**
    * Certifique-se de que seu servidor MySQL está em execução.
    * Crie um banco de dados chamado `pontosinteresse`.
        ```sql
        CREATE DATABASE pontosinteresse;
        ```
    * As configurações de conexão com o banco de dados estão no arquivo `src/main/resources/application.yml`. Por padrão, são:
        * **URL:** `jdbc:mysql://localhost:3306/pontosinteresse`
        * **Usuário:** `root`
        * **Senha:** `150292`
        * **Driver:** `com.mysql.cj.jdbc.Driver`

    ⚠️ **Atenção:** A senha está diretamente no arquivo de configuração (`150292`). Para ambientes de produção, utilize variáveis de ambiente ou Spring Cloud Config para gerenciar senhas de forma segura.

    * A aplicação utiliza `spring.jpa.hibernate.ddl-auto: update`, o que significa que o Hibernate tentará atualizar o schema do banco de dados (criar tabelas) automaticamente ao iniciar a aplicação.

## ▶️ Executando a Aplicação

1.  **Navegue até a pasta raiz do subprojeto `TestVagaBackend` (se houver uma dentro da raiz clonada, ou fique na raiz se o `pom.xml` principal estiver lá).**
    *No seu caso, parece que o projeto principal é `TestVagaBackend` dentro do repositório.*
    ```bash
    cd TestVagaBackend
    ```
2.  **Execute a aplicação usando o Maven:**
    ```bash
    mvn spring-boot:run
    ```
    A aplicação estará disponível em `http://localhost:8080`.

## Endpoints da API

A seguir, estão os detalhes dos endpoints disponíveis na API.

---

### 1. Cadastrar Ponto de Interesse

* **Método:** `POST`
* **URL:** `/pontos-de-interesse`
* **Descrição:** Cria um novo ponto de interesse no banco de dados.
* **Corpo da Requisição (Request Body):** `JSON`
    ```json
    {
        "nome": "Lanchonete",
        "x": 27,
        "y": 12
    }
    ```
    * `nome` (String): Nome do ponto de interesse.
    * `x` (Long): Coordenada X.
    * `y` (Long): Coordenada Y.
* **Resposta de Sucesso (Response):**
    * **Código:** `200 OK`
    * **Corpo:** Vazio

---

### 2. Listar Todos os Pontos de Interesse

* **Método:** `GET`
* **URL:** `/listar/pontos-de-interesse`
* **Descrição:** Retorna uma lista de todos os pontos de interesse cadastrados.
* **Parâmetros:** Nenhum.
* **Resposta de Sucesso (Response):**
    * **Código:** `200 OK`
    * **Corpo:** `JSON`
    ```json
    [
        {
            "id": 1,
            "nome": "Lanchonete",
            "x": 27,
            "y": 12
        },
        {
            "id": 2,
            "nome": "Posto",
            "x": 31,
            "y": 18
        }
    ]
    ```

---

### 3. Listar Pontos de Interesse Próximos

* **Método:** `GET`
* **URL:** `/listar/pontos-proximos`
* **Descrição:** Retorna uma lista de pontos de interesse que estão dentro de um raio (`raio`) a partir de uma coordenada de referência (`x`, `y`).
* **Parâmetros da Requisição (Query Parameters):**
    * `x` (Long): Coordenada X de referência.
    * `y` (Long): Coordenada Y de referência.
    * `raio` (Long): Distância máxima (raio) para considerar um ponto como próximo.
* **Exemplo de URL:** `http://localhost:8080/listar/pontos-proximos?x=20&y=10&raio=15`
* **Resposta de Sucesso (Response):**
    * **Código:** `200 OK`
    * **Corpo:** `JSON` (lista de pontos que atendem ao critério)
    ```json
    [
        {
            "id": 1,
            "nome": "Lanchonete",
            "x": 27,
            "y": 12
        }
        // ... outros pontos próximos
    ]
    ```

#### 🗺️ Lógica de Proximidade e Funcionamento do Raio

A busca por pontos próximos é realizada em duas etapas para otimizar a performance:

1.  **Filtro de "Bounding Box" (Caixa Delimitadora) no Banco de Dados:**
    Primeiramente, para reduzir o número de registros a serem processados pela aplicação e aproveitar a indexação do banco de dados, é feita uma consulta que seleciona apenas os pontos que estão dentro de um quadrado (bounding box) ao redor do ponto de referência (`x_ref`, `y_ref`). As coordenadas deste quadrado são calculadas da seguinte forma:
    * `xMin = x_ref - raio`
    * `xMax = x_ref + raio`
    * `yMin = y_ref - raio`
    * `yMax = y_ref + raio`
    A query SQL (gerada pelo Hibernate a partir do método `findPontosInteresseProximos`) se assemelha a:
    ```sql
    SELECT * FROM tb_pontos_interesse p
    WHERE p.x >= xMin AND p.x <= xMax AND p.y >= yMin AND p.y <= yMax;
    ```

2.  **Filtro por Distância Euclidiana (Círculo Real) na Aplicação:**
    Após receber os pontos do banco de dados (que estão dentro do quadrado), a aplicação realiza um segundo filtro em memória. Este filtro calcula a distância euclidiana real entre o ponto de referência (`x_ref`, `y_ref`) e cada um dos pontos (`x_ponto`, `y_ponto`) retornados pela primeira etapa. Somente os pontos cuja distância é menor ou igual ao `raio` fornecido são incluídos na resposta final.
    A fórmula da distância euclidiana ($D$) é:
    $$ D = \sqrt{((x_{ponto} - x_{ref})^2 + (y_{ponto} - y_{ref})^2)} $$
    Um ponto é considerado próximo se $D \le \text{raio}$.

![desmos-graph (1)](https://github.com/user-attachments/assets/d32a9bf7-b7aa-460a-8326-7f0d48db4d39)
