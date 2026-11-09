# Pulsar — Curadoria Inteligente de Conteúdo

## Credenciais locais

Na raiz do repositório, copie o arquivo de exemplo:

```bash
cp -n .env.example .env
```

Preencha o `.env` com os dados de conexão PostgreSQL e a chave Mediastack:

| Variável | Valor |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL do ambiente escolhido, sem senha embutida |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco correspondente à conexão escolhida |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco |
| `MEDIASTACK_API_ACCESS_KEY` | Chave de acesso da Mediastack |

No perfil `dev`, o Spring Boot importa explicitamente o `.env` como Java Properties. Escreva `CHAVE=valor`, sem `export` e sem envolver os valores em aspas. Use a senha literal, sem codificação de URL; represente uma barra invertida com `\\`. Não execute `source .env`: o arquivo é lido pela aplicação.

No perfil `dev`, o arquivo pode ficar na raiz do repositório ou em `pulsar/`. Mantenha apenas uma cópia; se ambas existirem, a do diretório de execução tem precedência. Variáveis de ambiente do processo prevalecem sobre o arquivo. As credenciais são obrigatórias em `dev` e `prod` e não possuem valores padrão.

Com o banco e o Ollama preparados, execute a API a partir de `pulsar/`:

```bash
cd pulsar
bash mvnw spring-boot:run
```

O `.env` é ignorado pelo Git. Nunca inclua credenciais em arquivos versionados ou em URLs de conexão. O `.env.example` contém somente exemplos e pode ser versionado.

## Perfis de execução — TASK-02

| Perfil | Credenciais | Banco | Coleta automática |
| --- | --- | --- | --- |
| `dev` (padrão) | `.env` ou variáveis de ambiente | PostgreSQL real; mantém `ddl-auto=update` durante o desenvolvimento | Desabilitada por padrão |
| `test` | Não importa `.env`; usa doubles definidos pela suíte | Autoconfiguração de datasource e JPA desabilitada | Desabilitada |
| `prod` | Variáveis de ambiente; não importa `.env` automaticamente | PostgreSQL real; `ddl-auto=validate`, sem atualizar o esquema | Desabilitada por padrão; exige ativação explícita |

O perfil `test` é destinado à suíte automatizada, que fornece mocks do repositório, cliente Mediastack e modelos de IA. Ele não é um modo de demonstração para executar a API isoladamente.

Para desenvolvimento, em `pulsar/`:

```bash
bash mvnw spring-boot:run
```

Para executar a suíte sem credenciais reais, banco ou Ollama:

```bash
bash mvnw test
```

Os testes que iniciam a aplicação selecionam `test` com `@ActiveProfiles("test")`; os testes unitários não precisam de um perfil.

Para produção, forneça as quatro variáveis de credenciais no ambiente do processo e selecione o perfil explicitamente:

```bash
SPRING_PROFILES_ACTIVE=prod bash mvnw spring-boot:run
```

O banco de produção precisa ter o esquema preparado antes da inicialização. As migrações serão implementadas na TASK-04.

### Integrações e agendamento

As opções abaixo podem ser definidas no `.env` em `dev` ou no ambiente do processo. Credenciais existentes no `.env` não precisam ser recopiadas; as opções têm valores padrão.

| Variável | Padrão | Uso |
| --- | --- | --- |
| `MEDIASTACK_API_URL` | `https://api.mediastack.com/v1/news` | Endpoint de notícias |
| `MEDIASTACK_API_LIMIT` | `3` | Notícias por requisição, de 1 a 100, sujeito ao plano contratado |
| `MEDIASTACK_API_CONNECT_TIMEOUT` | `PT10S` | Timeout de conexão HTTP |
| `MEDIASTACK_API_READ_TIMEOUT` | `PT30S` | Timeout de resposta HTTP |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Endereço do Ollama |
| `OLLAMA_CHAT_MODEL` | `llama3` | Modelo de resumo e sentimento |
| `OLLAMA_EMBEDDING_MODEL` | `all-minilm` | Modelo de embeddings |
| `OLLAMA_CHAT_TIMEOUT` | `PT2M` | Timeout do modelo de linguagem |
| `OLLAMA_EMBEDDING_TIMEOUT` | `PT2M` | Timeout de embeddings |
| `OLLAMA_TEMPERATURE` | `0.3` | Temperatura do modelo, entre 0 e 1 |
| `PULSAR_NEWS_COLLECTION_ENABLED` | `false` | Habilita a coleta automática com `true` |
| `PULSAR_NEWS_COLLECTION_INTERVAL` | `PT1H` | Período entre os inícios das coletas, mantendo a semântica de `fixedRate` |
| `PULSAR_NEWS_COLLECTION_INITIAL_DELAY` | `PT30S` | Espera antes da primeira coleta |

Use durações ISO-8601: `PT10S` para dez segundos, `PT2M` para dois minutos e `PT1H` para uma hora. Timeouts e intervalo devem ser positivos; o atraso inicial pode ser zero (`PT0S`).

Para ativar a coleta no desenvolvimento:

```bash
PULSAR_NEWS_COLLECTION_ENABLED=true bash mvnw spring-boot:run
```

Com a coleta desabilitada, iniciar a API não faz chamadas à Mediastack ou aos modelos de IA. A API ainda precisa do PostgreSQL em `dev` e `prod`; a listagem consulta o banco e a busca semântica usa o Ollama sob demanda.

O modelo de embeddings deve continuar produzindo 384 dimensões, compatíveis com `vector(384)`. Trocar o modelo também exige reavaliar os vetores existentes e reprocessá-los, mesmo que a dimensão permaneça igual.

Referências: [configuração e precedência do Spring Boot 3.3](https://docs.spring.io/spring-boot/3.3/reference/features/external-config.html) e [agendamento do Spring Framework 6.1](https://docs.spring.io/spring-framework/docs/6.1.x/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html).

## Rotação das credenciais expostas — TASK-01

1. Pare as instâncias da aplicação que usam as credenciais antigas.
2. No Supabase, abra o projeto em **Database → Settings** e redefina a senha do banco. Em **Connect**, obtenha os dados de conexão correspondentes ao seu ambiente. Essa senha é a do PostgreSQL, diferente da senha de login da sua conta e das chaves da API Supabase.
3. No painel da Mediastack, localize a chave de API e a opção de regeneração. Caso a opção não esteja disponível, solicite a rotação ao suporte. Confirme que a chave antiga foi revogada.
4. Atualize o `.env` e quaisquer outros ambientes que usem essas credenciais.
5. Valide o acesso com as novas credenciais e confirme a invalidação das antigas antes de concluir a tarefa.

Remover os valores dos arquivos atuais não os remove do histórico do Git; por isso, a rotação é necessária. Não publique logs contendo credenciais ou URLs de requisição com `access_key`.

Referências: [configuração externa do Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html), [redefinição da senha do banco Supabase](https://supabase.com/kb/troubleshooting/how-do-i-reset-my-supabase-database-password-oTs5sB) e [Mediastack](https://mediastack.com/).

## Planejamento

O plano de desenvolvimento e os critérios de aceite estão em [docs/sprints.md](docs/sprints.md).
