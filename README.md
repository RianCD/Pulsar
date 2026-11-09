# Pulsar — Curadoria Inteligente de Conteúdo

## Credenciais locais

Na raiz do repositório, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

Preencha o `.env` com os dados de conexão PostgreSQL e a chave Mediastack:

| Variável | Valor |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL do ambiente escolhido, sem senha embutida |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco correspondente à conexão escolhida |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco |
| `MEDIASTACK_API_ACCESS_KEY` | Chave de acesso da Mediastack |

O Spring Boot importa explicitamente o `.env` como Java Properties. Escreva `CHAVE=valor`, sem `export` e sem envolver os valores em aspas. Use a senha literal, sem codificação de URL; represente uma barra invertida com `\\`. Não execute `source .env`: o arquivo é lido pela aplicação.

O arquivo pode ficar na raiz do repositório ou em `pulsar/`. Mantenha apenas uma cópia; se ambas existirem, a do diretório de execução tem precedência. Variáveis de ambiente do processo prevalecem sobre o arquivo. A configuração é obrigatória e não possui credenciais padrão.

Com o banco e o Ollama preparados, execute a API a partir de `pulsar/`:

```bash
cd pulsar
bash mvnw spring-boot:run
```

O `.env` é ignorado pelo Git. Nunca inclua credenciais em arquivos versionados ou em URLs de conexão. O `.env.example` contém somente exemplos e pode ser versionado.

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
