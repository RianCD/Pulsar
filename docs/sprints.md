# Plano de sprints — Pulsar

## Objetivo

Evoluir o protótipo atual para um MVP que coleta notícias sem duplicação, gera resumos e sentimentos válidos e oferece uma interface de consulta.

As sprints seguem a ordem das dependências técnicas. Cada tarefa pode virar uma issue, e seus critérios de aceite definem a conclusão. Duração e responsáveis ainda não foram definidos.

## Sprint 1 — Ambiente seguro e reproduzível

### TASK-01 — Externalizar e renovar credenciais

- [x] Rotacionar a senha do banco e a chave Mediastack atualmente versionadas.
- [x] Remover os valores do código e configurar variáveis de ambiente.
- [x] Ignorar arquivos locais de segredos no Git e fornecer exemplos sem valores reais.

**Status:** externalização, proteção dos arquivos locais e rotação das credenciais concluídas. Conexões externas validadas em 08/11/2026.

**Verificação técnica:** build Maven concluído; importação do `.env`, precedência das variáveis de ambiente e ausência das credenciais antigas nos arquivos atuais verificadas com dados fictícios. As credenciais reais foram carregadas pela configuração do Spring Boot e verificadas com consultas externas: a nova senha PostgreSQL permitiu `SELECT 1`, e a senha antiga foi rejeitada com SQLSTATE `28P01`; a nova chave Mediastack retornou HTTP 200 com uma notícia, e a antiga foi rejeitada com HTTP 401 e `invalid_access_key`. Confirmadas a extensão pgvector, a tabela `public.tb_news` e a coluna `embedding` com tipo `vector(384)`. Nenhum dado foi gravado no banco. A inicialização completa da API, o agendamento e o Ollama não fizeram parte desta validação.

**Critério de aceite:** a aplicação inicia com configuração externa e as credenciais antigas foram invalidadas.

### TASK-02 — Organizar configurações por ambiente

- [ ] Criar perfis de desenvolvimento, teste e produção.
- [ ] Tornar configuráveis o endereço do Ollama, modelos, timeouts, quantidade de notícias e intervalo de coleta.
- [ ] Permitir desabilitar o agendamento para desenvolvimento e testes.

**Critério de aceite:** executar testes ou iniciar o ambiente de desenvolvimento não exige consumir APIs externas.

### TASK-03 — Completar o ambiente local

- [ ] Adicionar PostgreSQL com pgvector ao Docker Compose.
- [ ] Criar inicialização da extensão e verificações de disponibilidade dos serviços.
- [ ] Documentar instalação dos modelos e execução da API no README.

**Critério de aceite:** um novo desenvolvedor consegue subir o projeto seguindo apenas o README.

### TASK-04 — Versionar o esquema do banco

- [ ] Adotar uma ferramenta de migrações e criar o esquema inicial.
- [ ] Planejar a migração do banco existente sem perder notícias.
- [ ] Substituir `ddl-auto=update` por validação do esquema nos ambientes apropriados.

**Critério de aceite:** um banco vazio é preparado automaticamente e o banco existente possui um caminho de atualização documentado.

## Sprint 2 — Coleta e processamento confiáveis

### TASK-05 — Completar os dados das notícias

- [ ] Persistir URL original, fonte, data de publicação e data de coleta.
- [ ] Atualizar os DTOs da Mediastack e da API.
- [ ] Definir tratamento para notícias antigas sem esses campos.

**Critério de aceite:** novas notícias permitem identificar a origem e acessar o conteúdo original.

### TASK-06 — Impedir duplicação

- [ ] Definir uma identificação estável da notícia, preferencialmente com URL normalizada.
- [ ] Criar restrição de unicidade no banco.
- [ ] Verificar duplicação antes das chamadas de IA.

**Critério de aceite:** processar o mesmo lote duas vezes não duplica registros nem repete chamadas desnecessárias aos modelos.

### TASK-07 — Separar resumo e sentimento

- [ ] Solicitar saída estruturada à IA.
- [ ] Validar o resumo e restringir o sentimento a `POSITIVO`, `NEGATIVO` ou `NEUTRO`.
- [ ] Definir tratamento para respostas inválidas e registros antigos com sentimento `Processado`.

**Critério de aceite:** resumo e sentimento são persistidos em campos separados e respostas inválidas não são salvas como sucesso.

### TASK-08 — Tornar o processamento tolerante a falhas

- [ ] Tratar campos nulos e erros retornados pela Mediastack.
- [ ] Isolar falhas por notícia para continuar processando o lote.
- [ ] Adicionar timeouts e tentativas limitadas para falhas transitórias.
- [ ] Registrar o estado de processamento e permitir reprocessar falhas.

**Critério de aceite:** uma notícia com erro não bloqueia as demais; falhas podem ser identificadas e reprocessadas.

## Sprint 3 — API e busca semântica

### TASK-09 — Corrigir e avaliar a busca semântica

- [ ] Alinhar a consulta SQL à distância de cosseno documentada.
- [ ] Validar dimensões dos embeddings e tratar registros sem vetor.
- [ ] Criar exemplos de consultas com resultados esperados, incluindo buscas em português sobre notícias em inglês.

**Critério de aceite:** a busca usa a métrica definida e sua relevância é avaliada com exemplos reproduzíveis.

### TASK-10 — Adicionar paginação e filtros

- [ ] Paginar a listagem, com ordenação previsível.
- [ ] Adicionar filtros por sentimento, fonte e período.
- [ ] Limitar o tamanho das páginas e a quantidade de resultados da busca.

**Critério de aceite:** o cliente navega pelas notícias sem carregar toda a base.

### TASK-11 — Validar entradas e padronizar erros

- [ ] Rejeitar buscas vazias, parâmetros inválidos e textos acima do limite.
- [ ] Retornar códigos HTTP adequados sem expor detalhes internos.
- [ ] Atualizar o Swagger com os contratos e erros reais.

**Critério de aceite:** entradas inválidas retornam erros claros de cliente, sem serem convertidas genericamente em erro 500.

## Sprint 4 — Validação automatizada

Esta frente acompanha as sprints anteriores: cada correção deve incluir os testes correspondentes. A configuração inicial de testes e CI deve começar na Sprint 1.

### TASK-12 — Criar cobertura dos fluxos críticos e CI

- [ ] Testar deduplicação, saída da IA, falhas por notícia e validação dos endpoints.
- [ ] Testar migrações e consultas vetoriais com PostgreSQL/pgvector real em ambiente isolado.
- [ ] Simular Mediastack e Ollama nos testes automatizados.
- [ ] Configurar build e testes em cada pull request.

**Critério de aceite:** a suíte roda sem credenciais reais e detecta regressões nos fluxos principais.

## Sprint 5 — Interface do MVP

### TASK-13 — Construir o frontend do MVP

- [ ] Criar um feed paginado com título, resumo, sentimento, fonte e data.
- [ ] Adicionar busca semântica, filtros e link para a notícia original.
- [ ] Implementar estados de carregamento, vazio e erro, além de layout responsivo.

**Critério de aceite:** o usuário consegue descobrir e consultar notícias sem utilizar o Swagger.

## Sprint 6 — Preparação para publicação

### TASK-14 — Adicionar monitoramento e proteção operacional

- [ ] Expor verificações de saúde e registrar duração, sucessos e falhas da coleta.
- [ ] Restringir CORS aos ambientes utilizados.
- [ ] Definir limites de uso da busca, que consome recursos do Ollama.
- [ ] Proteger operações administrativas e impedir coletas concorrentes se houver múltiplas instâncias.

**Critério de aceite:** indisponibilidades são identificáveis e operações custosas possuem controles definidos.

### TASK-15 — Criar e validar o deploy

- [ ] Criar Dockerfile da API e configuração de produção.
- [ ] Definir hospedagem do backend, frontend, banco e Ollama.
- [ ] Configurar segredos, migrações, backup e procedimento de reversão.
- [ ] Executar um teste completo em homologação.

**Critério de aceite:** uma notícia é coletada, processada, persistida e encontrada pela interface no ambiente publicado.

## Ordem de execução recomendada

1. Concluir TASK-01 a TASK-04, acompanhadas da configuração inicial de testes e CI da TASK-12.
2. Priorizar TASK-05 a TASK-08 para garantir a qualidade dos dados.
3. Consolidar busca e API com TASK-09 a TASK-11 e completar a cobertura da TASK-12.
4. Entregar a interface na TASK-13.
5. Preparar a operação e publicação com TASK-14 e TASK-15.

## Fora do MVP inicial

- Personalização por usuário.
- Favoritos.
- Notificações.
- Múltiplas fontes de notícias.
- Recomendações individuais.

Essas funcionalidades podem entrar no próximo ciclo, depois que coleta, processamento e busca estiverem confiáveis.
