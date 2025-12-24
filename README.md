# PlugWay ETL

> Aplicação Desktop ETL com padrões EIP para integração entre bancos de dados e APIs REST

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.x-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📋 Índice

- [Sobre](#-sobre)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Requisitos](#-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Uso](#-uso)
- [Arquitetura](#-arquitetura)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Documentação Técnica](#-documentação-técnica)
- [Desenvolvimento](#-desenvolvimento)
- [Testes](#-testes)
- [Roadmap](#-roadmap)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

## 🎯 Sobre

**PlugWay ETL** é uma aplicação desktop desenvolvida em Java que facilita a integração entre diferentes fontes de dados (bancos de dados relacionais) e APIs REST. A aplicação implementa padrões EIP (Enterprise Integration Patterns) para garantir uma arquitetura robusta, escalável e fácil de manter.

### Principais Características

- **Extract (Extração)**: Consulta dados de bancos de dados relacionais (Firebird, MySQL, PostgreSQL, SQL Server)
- **Transform (Transformação)**: Aplica transformações nos dados usando padrões EIP com pipeline configurável
- **Load (Carregamento)**: Envia dados transformados para APIs REST com retry automático e tratamento de erros
- **Interface Gráfica**: Interface desktop moderna e intuitiva desenvolvida com JavaFX
- **Agendamento**: Execução automática de jobs através de agendamento configurável com Quartz Scheduler
- **Monitoramento**: Acompanhamento em tempo real de execuções, métricas e auditoria
- **Logs**: Sistema completo de logging para auditoria e debugging

## ✨ Funcionalidades

### 🔌 Gerenciamento de Conexões de Banco de Dados

- Configuração de múltiplas conexões simultâneas
- Suporte para **Firebird**, **MySQL**, **PostgreSQL** e **SQL Server**
- Pool de conexões configurável usando HikariCP
- Teste de conectividade em tempo real
- Validação automática de configurações
- Gerenciamento seguro de credenciais
- Reutilização de conexões para melhor performance

### 📡 Gerenciamento de APIs REST

- Configuração de múltiplos endpoints REST
- Suporte para métodos HTTP: **POST**, **PUT**, **PATCH**, **GET**, **DELETE**
- Autenticação flexível:
  - Bearer Token
  - Basic Authentication
  - API Key (com header customizável)
  - Sem autenticação
- Headers HTTP customizáveis
- Configuração de timeouts e retries
- Backoff exponencial configurável
- Validação de conectividade antes do uso

### ⚙️ Gerenciamento de Jobs ETL

- Criação e edição de jobs ETL através de interface gráfica
- Configuração completa de:
  - **Origem**: Seleção de conexão de banco e query SQL
  - **Transformação**: Pipeline configurável de transformações
  - **Destino**: Seleção de API REST e configuração de endpoint
- Parâmetros dinâmicos em queries SQL
- Validação completa de configurações antes da execução
- Execução manual ou agendada
- Habilitar/desabilitar jobs individualmente
- Visualização de histórico de execuções

### 🔄 Transformações de Dados

O pipeline de transformação suporta:

- **Normalizer**: Normalização de dados
  - Normalização de datas para formato ISO 8601
  - Normalização de valores decimais
  - Normalização de nomes de colunas (snake_case/camelCase)
  - Tratamento configurável de valores nulos

- **ContentEnricher**: Enriquecimento de conteúdo
  - Adição de metadata (timestamp, jobId, etc.)
  - Adição de estatísticas (contagem de registros)
  - Configuração opcional por job

- **DatabaseToJsonTranslator**: Conversão para JSON
  - Conversão automática de ResultSet para JSON
  - Formatação opcional (pretty print)
  - Preservação de tipos de dados

- **JsonSchemaValidator**: Validação de schema JSON (preparado para uso futuro)

### 📅 Agendamento de Jobs

- Agendamento baseado em expressões Cron
- Interface gráfica para configuração de agendamentos
- Pausar/retomar jobs agendados
- Visualização do próximo horário de execução
- Execução única ou recorrente
- Integração com Quartz Scheduler
- Gerenciamento de múltiplos jobs agendados simultaneamente

### 📊 Monitoramento e Auditoria

- **Dashboard em tempo real** com métricas de execução
- **Métricas coletadas**:
  - Duração de cada etapa (Extract, Transform, Load)
  - Número de registros processados
  - Taxa de sucesso/falha
  - Status detalhado de cada execução
- **MessageStore**: Armazenamento de todas as mensagens interceptadas
- **Visualização de mensagens** em cada etapa do pipeline
- **Histórico completo** de execuções
- **Dead Letter Channel**: Mensagens com falha são armazenadas para reprocessamento

### 📝 Sistema de Logs

- Visualização de logs em tempo real
- Filtros por nível (INFO, WARN, ERROR, DEBUG)
- Logs persistidos em arquivo
- Rotação automática de logs
- Exportação de logs
- Terminal integrado na interface gráfica

### 🏗️ Padrões EIP Implementados

A aplicação implementa os seguintes padrões Enterprise Integration Patterns:

- **Pipeline (Pipes-and-Filters)**: Encadeamento sequencial de transformações
- **Wire Tap**: Interceptação de mensagens para monitoramento e auditoria
- **Control Bus**: Gerenciamento centralizado de mensagens e controle
- **Message Transformer**: Transformação de mensagens entre formatos
- **Dead Letter Channel**: Tratamento de mensagens com falha definitiva
- **Retry Handler**: Re-tentativas automáticas com backoff configurável
- **Content Enricher**: Enriquecimento de dados com informações adicionais
- **Messaging Gateway**: Interface simplificada para execução de jobs

## 🛠️ Tecnologias

### Core

- **Java 17**: Linguagem de programação
- **JavaFX 21.0.1**: Framework de interface gráfica
- **Maven**: Gerenciamento de dependências e build

### Banco de Dados

- **HikariCP 5.0.1**: Pool de conexões de alta performance
- **Jaybird 4.0.9**: Driver Firebird
- **MySQL Connector 8.1.0**: Driver MySQL
- **PostgreSQL 42.6.0**: Driver PostgreSQL
- **Microsoft SQL Server JDBC 12.4.1**: Driver SQL Server

### Integração e Processamento

- **Jackson 2.15.2**: Processamento JSON (serialização/deserialização)
- **Quartz 2.3.2**: Agendamento de tarefas com expressões Cron
- **Typesafe Config 1.4.3**: Gerenciamento de configurações (HOCON)

### Interface Gráfica

- **Ikonli 12.3.1**: Biblioteca de ícones (FontAwesome 5)
- **JavaFX FXML**: Definição de layouts

### Logging

- **SLF4J 2.0.9**: API de logging
- **Logback 1.4.11**: Implementação de logging

### Validação

- **Jakarta Validation 3.0.2**: API de validação de dados
- **Hibernate Validator 8.0.0**: Implementação de validação

### Testes

- **JUnit 5.10.0**: Framework de testes unitários
- **Mockito 5.5.0**: Framework de mock
- **TestFX 4.0.18**: Testes de interface gráfica

## 📦 Requisitos

### Sistema Operacional

- Windows 10/11
- Linux (distribuições modernas)
- macOS 10.14 ou superior

### Software

- **Java JDK 17** ou superior
- **Maven 3.6+** (para build a partir do código-fonte)

### Dependências Opcionais

- Bancos de dados: Firebird, MySQL, PostgreSQL ou SQL Server (conforme necessário)
- APIs REST para destino dos dados

## 🚀 Instalação

### Opção 1: Build a partir do código-fonte

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/plugway.git
   cd plugway
   ```

2. **Compile o projeto:**
   ```bash
   mvn clean package
   ```

3. **Execute a aplicação:**
   ```bash
   java -jar target/plugway-1.0.0-SNAPSHOT.jar
   ```

   Ou usando o plugin Maven do JavaFX:
   ```bash
   mvn javafx:run
   ```

### Opção 2: Executar em modo de desenvolvimento

```bash
mvn clean compile exec:java
```

## ⚙️ Configuração

### Arquivos de Configuração

A aplicação utiliza dois formatos de configuração:

1. **application.properties**: Propriedades simples do sistema
2. **application.conf**: Configuração HOCON (Typesafe Config) para configurações mais complexas

### Configurações Principais

#### Logging

```properties
logging.level=INFO
logging.file.enabled=true
logging.file.path=logs/etl-application.log
logging.file.maxSize=10MB
logging.file.maxHistory=30
```

#### Pool de Conexões

```properties
database.pool.minSize=2
database.pool.maxSize=10
database.pool.connectionTimeout=30000
database.pool.idleTimeout=600000
database.pool.maxLifetime=1800000
```

#### API REST

```properties
api.default.timeout=30000
api.default.retries=3
api.default.retryDelay=1000
```

#### Agendamento

```properties
scheduler.enabled=true
scheduler.threadPoolSize=5
```

### Diretórios Criados Automaticamente

A aplicação cria os seguintes diretórios automaticamente:

- `config/`: Configurações de jobs e conexões
  - `config/jobs.json`: Definições de jobs ETL
  - `config/connections.json`: Configurações de conexões de banco
  - `config/apis.json`: Configurações de APIs REST
- `logs/`: Arquivos de log
  - `logs/etl-application.log`: Log principal
- `data/`: Dados temporários e cache
  - `data/message-store/`: Armazenamento de mensagens interceptadas (auditoria)
  - `data/dead-letter/`: Mensagens com falha definitiva

## 💻 Uso

### Primeiros Passos

#### 1. Iniciar a Aplicação

Execute o JAR ou use o Maven para iniciar. A interface gráfica será exibida com um menu principal.

#### 2. Configurar Conexão de Banco de Dados

1. Acesse o menu **"Conexões"** → **"Bancos de Dados"**
2. Clique em **"Nova Conexão"**
3. Preencha os dados de conexão:
   - Nome da conexão
   - Tipo de banco (Firebird, MySQL, PostgreSQL, SQL Server)
   - Host e porta
   - Nome do banco de dados
   - Usuário e senha
4. Clique em **"Testar Conexão"** para validar
5. Salve a conexão

#### 3. Configurar API de Destino

1. Acesse o menu **"Conexões"** → **"APIs"**
2. Clique em **"Nova API"**
3. Configure:
   - Nome da API
   - URL base (ex: `https://api.exemplo.com`)
   - Endpoint (ex: `/v1/dados`)
   - Método HTTP (POST, PUT, PATCH, etc.)
   - Tipo de autenticação e credenciais
   - Headers customizados (se necessário)
   - Timeout e configurações de retry
4. Clique em **"Testar Conexão"** para validar
5. Salve a API

#### 4. Criar um Job ETL

1. Acesse o menu **"Jobs"** → **"Gerenciar Jobs"**
2. Clique em **"Novo Job"**
3. Preencha as informações básicas:
   - ID do job (único)
   - Nome descritivo
   - Descrição (opcional)
4. Configure a **Origem**:
   - Selecione a conexão de banco configurada
   - Informe a query SQL para extração
   - Configure parâmetros se necessário (opcional)
5. Configure as **Transformações**:
   - Normalização de dados (ativo por padrão)
   - Enriquecimento de conteúdo (opcional)
   - Formatação JSON (pretty print opcional)
6. Configure o **Destino**:
   - Selecione a API configurada
   - O endpoint será usado da configuração da API
7. Clique em **"Validar"** para verificar a configuração
8. Salve o job

#### 5. Executar um Job

1. Na lista de jobs, selecione o job desejado
2. Clique em **"Executar Job"**
3. Acompanhe o progresso na interface
4. Visualize o resultado (sucesso ou erro com detalhes)

#### 6. Agendar um Job

1. Acesse o menu **"Jobs"** → **"Agendamento"**
2. Selecione o job que deseja agendar
3. Configure a expressão Cron (ex: `0 0 2 * * ?` para executar diariamente às 2h)
4. Ative o agendamento
5. Visualize o próximo horário de execução

### Exemplo de Job ETL

#### Query de Extração (Origem)

```sql
SELECT 
    id,
    nome,
    email,
    data_cadastro,
    status
FROM usuarios
WHERE data_cadastro >= CURRENT_DATE - INTERVAL '7' DAY
  AND status = 'ATIVO'
ORDER BY data_cadastro DESC
```

#### Transformação Configurada

- ✅ Normalização de datas para ISO 8601
- ✅ Normalização de nomes de colunas (snake_case)
- ✅ Enriquecimento com metadata
- ✅ Conversão para JSON formatado

#### Destino

- **URL**: `https://api.exemplo.com/v1/usuarios`
- **Método**: POST
- **Autenticação**: Bearer Token
- **Headers**: `Content-Type: application/json`

#### Resultado JSON Enviado

```json
{
  "metadata": {
    "jobId": "job-001",
    "timestamp": "2024-01-15T10:30:00Z",
    "recordCount": 150
  },
  "data": [
    {
      "id": 1,
      "nome": "João Silva",
      "email": "joao@exemplo.com",
      "dataCadastro": "2024-01-10T08:00:00Z",
      "status": "ATIVO"
    },
    ...
  ],
  "statistics": {
    "totalRecords": 150,
    "processingTime": 1250
  }
}
```

## 🏛️ Arquitetura

### Padrão Arquitetural

A aplicação segue uma arquitetura em camadas:

```
┌─────────────────────────────────────┐
│      Interface Gráfica (JavaFX)     │
│  - MainController                   │
│  - JobManagerController             │
│  - ConnectionManagerController      │
│  - ApiManagerController             │
│  - SchedulerController              │
│  - MonitoringController             │
│  - LogsController                   │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│         Camada de Serviços          │
│  - MessagingGateway                 │
│  - EtlOrchestrator                  │
│  - ExtractService                   │
│  - TransformService                 │
│  - LoadService                      │
│  - SchedulerService                 │
│  - JobConfigService                 │
│  - ConnectionConfigService          │
│  - ApiConfigService                 │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Padrões EIP (Enterprise           │
│   Integration Patterns)             │
│  - EtlPipeline                      │
│  - WireTap                          │
│  - ControlBus                       │
│  - MessageTransformer               │
│  - DeadLetterChannel                │
│  - RetryHandler                     │
│  - Normalizer                       │
│  - ContentEnricher                  │
│  - DatabaseToJsonTranslator         │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Camada de Acesso a Dados          │
│  - ConnectionManager                │
│  - QueryExecutor                    │
│  - DatabaseEndpoint                 │
│  - RestApiEndpoint                  │
│  - RestApiClient                    │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│   Persistência e Monitoramento      │
│  - MessageStore                     │
│  - ExecutionMetrics                 │
│  - LogAppender                      │
│  - ConfigManager                    │
└─────────────────────────────────────┘
```

### Fluxo de Execução ETL

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────┐
│ Extract  │ --> │  Transform   │ --> │    Load     │ --> │   API    │
│   (DB)   │     │   (Pipeline) │     │  (REST API) │     │  (Dest)  │
└──────────┘     └──────────────┘     └─────────────┘     └──────────┘
      │                  │                     │
      │                  │                     │
      ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────┐
│         Monitoramento e Auditoria                       │
│  - WireTap intercepta todas as mensagens                │
│  - ExecutionMetrics coleta métricas                     │
│  - MessageStore armazena para auditoria                 │
│  - DeadLetterChannel para mensagens falhadas            │
│  - LogAppender registra todos os eventos                │
└─────────────────────────────────────────────────────────┘
```

Para mais detalhes sobre a arquitetura, consulte o arquivo [MERMAID.md](MERMAID.md) com diagramas detalhados.

## 📁 Estrutura do Projeto

```
plugway/
├── src/
│   ├── main/
│   │   ├── java/com/plugway/etl/
│   │   │   ├── config/              # Gerenciamento de configurações
│   │   │   │   ├── ApplicationProperties.java
│   │   │   │   └── ConfigManager.java
│   │   │   ├── dao/                 # Acesso a dados (Extract)
│   │   │   │   ├── ConnectionManager.java
│   │   │   │   ├── DatabaseConnectionFactory.java
│   │   │   │   ├── DatabaseEndpoint.java
│   │   │   │   ├── ExtractService.java
│   │   │   │   └── QueryExecutor.java
│   │   │   ├── eip/                 # Padrões Enterprise Integration Patterns
│   │   │   │   ├── ControlBus.java
│   │   │   │   ├── EtlPipeline.java
│   │   │   │   ├── MessageEndpoint.java
│   │   │   │   ├── MessageInterceptor.java
│   │   │   │   ├── MessageTransformer.java
│   │   │   │   └── WireTap.java
│   │   │   ├── model/               # Modelos de dados
│   │   │   │   ├── ApiConfig.java
│   │   │   │   ├── AuthType.java
│   │   │   │   ├── DatabaseConfig.java
│   │   │   │   ├── DatabaseType.java
│   │   │   │   ├── EtlJob.java
│   │   │   │   ├── EtlMessage.java
│   │   │   │   ├── JobExecutionInfo.java
│   │   │   │   ├── JobStatus.java
│   │   │   │   ├── MessageType.java
│   │   │   │   └── ScheduleConfig.java
│   │   │   ├── service/             # Serviços de negócio
│   │   │   │   ├── ApiConfigService.java
│   │   │   │   ├── ConnectionConfigService.java
│   │   │   │   ├── JobConfigService.java
│   │   │   │   ├── load/            # Serviço de carga (Load)
│   │   │   │   │   ├── DeadLetterChannel.java
│   │   │   │   │   ├── LoadService.java
│   │   │   │   │   ├── RestApiClient.java
│   │   │   │   │   ├── RestApiEndpoint.java
│   │   │   │   │   └── RetryHandler.java
│   │   │   │   ├── monitoring/      # Monitoramento
│   │   │   │   │   ├── ExecutionMetrics.java
│   │   │   │   │   ├── LogAppender.java
│   │   │   │   │   └── MessageStore.java
│   │   │   │   ├── orchestrator/    # Orquestração ETL
│   │   │   │   │   ├── EtlOrchestrator.java
│   │   │   │   │   └── MessagingGateway.java
│   │   │   │   ├── scheduler/       # Agendamento
│   │   │   │   │   ├── JobScheduler.java
│   │   │   │   │   └── SchedulerService.java
│   │   │   │   └── transform/       # Transformações
│   │   │   │       ├── ContentEnricher.java
│   │   │   │       ├── DatabaseToJsonTranslator.java
│   │   │   │       ├── JsonSchemaValidator.java
│   │   │   │       ├── Normalizer.java
│   │   │   │       └── TransformService.java
│   │   │   ├── ui/                  # Interface gráfica (JavaFX)
│   │   │   │   ├── ApiManagerController.java
│   │   │   │   ├── ConnectionManagerController.java
│   │   │   │   ├── IconHelper.java
│   │   │   │   ├── JobManagerController.java
│   │   │   │   ├── LogsController.java
│   │   │   │   ├── MainController.java
│   │   │   │   ├── MonitoringController.java
│   │   │   │   └── SchedulerController.java
│   │   │   ├── util/                # Utilitários
│   │   │   │   └── LoggerUtil.java
│   │   │   └── Main.java            # Classe principal
│   │   └── resources/
│   │       ├── fxml/                 # Arquivos FXML da interface
│   │       │   ├── ApiManagerView.fxml
│   │       │   ├── ConnectionManagerView.fxml
│   │       │   ├── JobManagerView.fxml
│   │       │   ├── MainView.fxml
│   │       │   └── SchedulerView.fxml
│   │       ├── css/                  # Estilos CSS
│   │       │   └── application.css
│   │       ├── images/               # Imagens
│   │       ├── application.properties
│   │       ├── application.conf
│   │       └── logback.xml
│   └── test/                         # Testes unitários e de integração
│       └── java/com/plugway/etl/
│           ├── dao/
│           ├── eip/
│           ├── integration/
│           └── service/
├── config/                           # Configurações de jobs (criado em runtime)
│   ├── jobs.json
│   ├── connections.json
│   └── apis.json
├── data/                             # Dados temporários (criado em runtime)
│   ├── message-store/
│   └── dead-letter/
├── logs/                             # Arquivos de log (criado em runtime)
│   └── etl-application.log
├── pom.xml                           # Configuração Maven
├── README.md                         # Este arquivo
├── ROADMAP.md                        # Roadmap de desenvolvimento
├── MERMAID.md                        # Diagramas de arquitetura
└── LICENSE                           # Licença Apache 2.0
```

## 📚 Documentação Técnica

Para entender melhor a arquitetura e as regras de negócio do projeto, consulte:

- **[MERMAID.md](MERMAID.md)**: Diagramas detalhados da arquitetura, fluxos de execução, regras de negócio e modelos de dados usando Mermaid
- **[ROADMAP.md](ROADMAP.md)**: Roadmap de desenvolvimento com funcionalidades planejadas

## 🔧 Desenvolvimento

### Setup do Ambiente de Desenvolvimento

1. **Instale o JDK 17:**
   - Baixe em: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - Configure a variável de ambiente `JAVA_HOME`

2. **Instale o Maven:**
   - Baixe em: https://maven.apache.org/download.cgi
   - Configure a variável de ambiente `MAVEN_HOME`

3. **Configure sua IDE:**
   - Importe o projeto como projeto Maven
   - Configure o JDK 17 como Java SDK
   - Instale plugins JavaFX (se necessário)
   - Configure o caminho do JavaFX SDK se necessário

### Comandos Úteis

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Gerar JAR executável (com todas as dependências)
mvn clean package

# Executar aplicação via Maven JavaFX plugin
mvn javafx:run

# Executar aplicação via exec plugin
mvn clean compile exec:java

# Limpar e reconstruir tudo
mvn clean install

# Verificar dependências
mvn dependency:tree
```

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=NomeDaClasseTest

# Testes com detalhes
mvn test -X
```

### Cobertura de Testes

A aplicação possui testes para:

- ✅ Serviços de extração (ExtractService, QueryExecutor)
- ✅ Serviços de transformação (TransformService, Normalizer, DatabaseToJsonTranslator)
- ✅ Serviços de carga (LoadService)
- ✅ Orquestrador ETL (EtlOrchestrator)
- ✅ Padrões EIP (Pipeline, WireTap)
- ✅ Gerenciamento de conexões (ConnectionManager)
- ✅ Agendamento (JobScheduler, SchedulerService)
- ✅ Monitoramento (ExecutionMetrics, MessageStore)

### Tipos de Testes

- **Testes Unitários**: Serviços e utilitários isolados
- **Testes de Integração**: Fluxo ETL completo end-to-end
- **Testes de Interface**: TestFX para componentes JavaFX (preparado para uso)

## 🗺️ Roadmap

Consulte o arquivo [ROADMAP.md](ROADMAP.md) para ver o planejamento completo de funcionalidades futuras, incluindo:

- Sistema de plugins extensível
- Suporte a novos formatos (CSV, Excel, JSON, XML)
- Integração com message queues (Kafka, RabbitMQ)
- Expansão de APIs REST como fonte de dados
- Cloud storage (S3) e protocolos FTP/SFTP

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Faça um Fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- Siga as convenções Java padrão
- Use nomes descritivos para classes e métodos
- Documente classes e métodos públicos com JavaDoc
- Adicione testes para novas funcionalidades
- Mantenha a cobertura de testes adequada
- Siga os padrões EIP já implementados

### Estrutura de Commits

Use mensagens de commit descritivas:

```
feat: Adiciona suporte a novo tipo de banco
fix: Corrige problema de pool de conexões
docs: Atualiza documentação do README
refactor: Refatora serviço de transformação
test: Adiciona testes para novo componente
```

## 📄 Licença

Este projeto está licenciado sob a [Apache License 2.0](LICENSE). Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Suporte

Para dúvidas, problemas ou sugestões:

- Abra uma [Issue](https://github.com/seu-usuario/plugway/issues)
- Consulte a documentação técnica em [MERMAID.md](MERMAID.md)
- Veja o roadmap de desenvolvimento em [ROADMAP.md](ROADMAP.md)

## 🙏 Agradecimentos

- Comunidade JavaFX
- Projeto Apache Camel (inspiração para padrões EIP)
- Todos os contribuidores e mantenedores
- Comunidades open source das bibliotecas utilizadas

---

**Desenvolvido com ❤️ usando Java e JavaFX**
