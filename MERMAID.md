# 📊 PlugWay ETL - Diagramas de Arquitetura e Regras de Negócio

Este documento contém diagramas Mermaid que descrevem a arquitetura, fluxos de execução e regras de negócio do PlugWay ETL.

---

## 🏗️ 1. Arquitetura em Camadas

```mermaid
graph TB
    subgraph "Camada de Apresentação"
        UI[JavaFX UI]
        MainController[MainController]
        JobController[JobManagerController]
        ConnectionController[ConnectionManagerController]
        ApiController[ApiManagerController]
        SchedulerController[SchedulerController]
        MonitoringController[MonitoringController]
        LogsController[LogsController]
    end

    subgraph "Camada de Serviços"
        Gateway[MessagingGateway]
        Orchestrator[EtlOrchestrator]
        TransformService[TransformService]
        LoadService[LoadService]
        ExtractService[ExtractService]
        SchedulerService[SchedulerService]
        JobScheduler[JobScheduler]
    end

    subgraph "Padrões EIP"
        Pipeline[EtlPipeline]
        WireTap[WireTap]
        ControlBus[ControlBus]
        MessageTransformer[MessageTransformer]
        DeadLetterChannel[DeadLetterChannel]
        RetryHandler[RetryHandler]
    end

    subgraph "Camada de Acesso a Dados"
        ConnectionManager[ConnectionManager]
        QueryExecutor[QueryExecutor]
        DatabaseEndpoint[DatabaseEndpoint]
        RestApiEndpoint[RestApiEndpoint]
    end

    subgraph "Camada de Persistência"
        JobConfigService[JobConfigService]
        ConnectionConfigService[ConnectionConfigService]
        ApiConfigService[ApiConfigService]
        MessageStore[MessageStore]
    end

    subgraph "Camada de Monitoramento"
        ExecutionMetrics[ExecutionMetrics]
        LogAppender[LogAppender]
    end

    UI --> MainController
    MainController --> JobController
    MainController --> ConnectionController
    MainController --> ApiController
    MainController --> SchedulerController
    MainController --> MonitoringController
    MainController --> LogsController

    JobController --> Gateway
    ConnectionController --> ConnectionConfigService
    ApiController --> ApiConfigService
    SchedulerController --> SchedulerService

    Gateway --> Orchestrator
    Orchestrator --> ExtractService
    Orchestrator --> Pipeline
    Orchestrator --> LoadService
    Orchestrator --> WireTap

    SchedulerService --> JobScheduler
    JobScheduler --> Gateway

    Pipeline --> MessageTransformer
    Pipeline --> WireTap
    LoadService --> RestApiEndpoint
    LoadService --> DeadLetterChannel
    RestApiEndpoint --> RetryHandler

    ExtractService --> ConnectionManager
    ExtractService --> QueryExecutor
    QueryExecutor --> DatabaseEndpoint
    ConnectionManager --> DatabaseEndpoint

    Gateway --> ExecutionMetrics
    Orchestrator --> ExecutionMetrics
    WireTap --> MessageStore
    LogAppender --> ExecutionMetrics

    JobController --> JobConfigService
    ConnectionConfigService --> ConnectionManager
    ApiConfigService --> RestApiEndpoint
```

---

## 🔄 2. Fluxo de Execução ETL (Sequência)

```mermaid
sequenceDiagram
    participant UI as Interface Gráfica
    participant GW as MessagingGateway
    participant EO as EtlOrchestrator
    participant ES as ExtractService
    participant CM as ConnectionManager
    participant QE as QueryExecutor
    participant EP as EtlPipeline
    participant LS as LoadService
    participant API as RestApiEndpoint
    participant WT as WireTap
    participant MS as MessageStore
    participant EM as ExecutionMetrics

    UI->>GW: executeEtlJob(job)
    
    GW->>GW: Validar job.isValid()
    GW->>GW: Verificar job.isEnabled()
    
    GW->>EO: execute(job)
    
    EO->>EM: Criar métricas
    EO->>EO: Criar JobExecutionInfo
    EO->>WT: intercept(job-start)
    WT->>MS: Salvar mensagem
    
    Note over EO: ETAPA 1: EXTRACT
    EO->>ES: extract(config, sqlQuery)
    ES->>CM: getDataSource(config)
    CM-->>ES: DataSource
    ES->>QE: executeQuery(sqlQuery)
    QE->>CM: getConnection()
    CM-->>QE: Connection
    QE->>QE: Executar SQL
    QE-->>ES: ResultSet
    ES->>ES: Converter para EtlMessage
    ES-->>EO: EtlMessage (dados extraídos)
    
    EO->>WT: intercept(extracted)
    WT->>MS: Salvar mensagem
    EO->>EM: endExtract(duration, count)
    
    Note over EO: ETAPA 2: TRANSFORM
    EO->>EP: createPipeline(job)
    EO->>EP: process(extractedMessage)
    
    loop Para cada filtro no pipeline
        EP->>EP: normalizer.transform()
        EP->>WT: intercept(before-filter)
        WT->>MS: Salvar mensagem
        EP->>EP: contentEnricher.transform()
        EP->>EP: translator.transform()
        EP->>WT: intercept(after-filter)
        WT->>MS: Salvar mensagem
    end
    
    EP-->>EO: EtlMessage (transformado)
    EO->>WT: intercept(transformed)
    WT->>MS: Salvar mensagem
    EO->>EM: endTransform(duration)
    
    Note over EO: ETAPA 3: LOAD
    EO->>LS: load(config, transformedMessage)
    LS->>API: connect()
    LS->>API: send(message)
    
    alt Sucesso
        API-->>LS: true
        LS-->>EO: true
        EO->>EM: markSuccess()
        EO->>WT: intercept(job-success)
    else Falha
        API->>API: RetryHandler.retry()
        alt Retry bem-sucedido
            API-->>LS: true
        else Falha definitiva
            API->>API: DeadLetterChannel.send()
            API-->>LS: false
            LS-->>EO: false
            EO->>EM: markFailure()
            EO->>WT: intercept(job-failure)
        end
    end
    
    WT->>MS: Salvar mensagem
    EO->>EO: executionInfo.success() ou fail()
    EO-->>GW: JobExecutionInfo
    GW-->>UI: ExecutionResult
    UI->>UI: Atualizar interface
```

---

## 🧩 3. Diagrama de Componentes

```mermaid
graph LR
    subgraph "PlugWay ETL System"
        subgraph "UI Components"
            A1[MainView]
            A2[JobManagerView]
            A3[ConnectionManagerView]
            A4[ApiManagerView]
            A5[SchedulerView]
            A6[MonitoringView]
            A7[LogsView]
        end

        subgraph "Business Services"
            B1[EtlOrchestrator]
            B2[ExtractService]
            B3[TransformService]
            B4[LoadService]
            B5[SchedulerService]
        end

        subgraph "EIP Patterns"
            C1[EtlPipeline]
            C2[WireTap]
            C3[ControlBus]
            C4[DeadLetterChannel]
            C5[RetryHandler]
            C6[Normalizer]
            C7[ContentEnricher]
            C8[DatabaseToJsonTranslator]
        end

        subgraph "Data Access"
            D1[ConnectionManager]
            D2[QueryExecutor]
            D3[RestApiClient]
            D4[DatabaseEndpoint]
            D5[RestApiEndpoint]
        end

        subgraph "Configuration"
            E1[JobConfigService]
            E2[ConnectionConfigService]
            E3[ApiConfigService]
            E4[ConfigManager]
        end

        subgraph "Monitoring"
            F1[ExecutionMetrics]
            F2[MessageStore]
            F3[LogAppender]
        end

        subgraph "External Systems"
            G1[(Firebird)]
            G2[(MySQL)]
            G3[(PostgreSQL)]
            G4[(SQL Server)]
            G5[REST API]
        end
    end

    A1 --> B1
    A2 --> B1
    A5 --> B5
    B1 --> B2
    B1 --> B3
    B1 --> B4
    B5 --> B1
    B2 --> D1
    B3 --> C1
    B4 --> D3
    C1 --> C6
    C1 --> C7
    C1 --> C8
    C1 --> C2
    B4 --> C4
    B4 --> C5
    D1 --> D4
    D2 --> D4
    D3 --> D5
    D4 --> G1
    D4 --> G2
    D4 --> G3
    D4 --> G4
    D5 --> G5
    E1 --> E4
    E2 --> E4
    E3 --> E4
    B1 --> F1
    C2 --> F2
    F3 --> F1
```

---

## 📦 4. Modelo de Dados (Entidades Principais)

```mermaid
classDiagram
    class EtlJob {
        -String id
        -String name
        -String description
        -boolean enabled
        -DatabaseConfig sourceConfig
        -String sqlQuery
        -Map queryParameters
        -ApiConfig targetConfig
        -Map transformations
        -ScheduleConfig schedule
        +isValid() boolean
        +isEnabled() boolean
    }

    class DatabaseConfig {
        -String name
        -DatabaseType type
        -String host
        -int port
        -String database
        -String username
        -String password
        -Map properties
        +isValid() boolean
        +buildJdbcUrl() String
    }

    class ApiConfig {
        -String id
        -String name
        -String baseUrl
        -String endpoint
        -HttpMethod method
        -AuthType authType
        -String authToken
        -Map headers
        -int timeout
        -int retries
        +isValid() boolean
    }

    class EtlMessage {
        -String messageId
        -Object payload
        -Map headers
        -MessageType type
        -Date timestamp
        +addHeader(String, String)
        +getHeader(String) String
    }

    class JobExecutionInfo {
        -String jobId
        -JobStatus status
        -Date startTime
        -Date endTime
        -long durationMillis
        -int recordsProcessed
        -String errorMessage
        -Exception exception
        +start()
        +success(int)
        +fail(String, Exception)
        +cancel()
    }

    class ScheduleConfig {
        -boolean enabled
        -String cronExpression
        -Date startDate
        -Date endDate
        +isValid() boolean
    }

    class ExecutionMetrics {
        -String jobId
        -long extractDuration
        -long transformDuration
        -long loadDuration
        -int recordCount
        -boolean success
        -String errorMessage
        +startExtract()
        +endExtract(long, int)
        +startTransform()
        +endTransform(long)
        +startLoad()
        +endLoad(long)
        +markSuccess()
        +markFailure(String)
    }

    EtlJob --> DatabaseConfig : sourceConfig
    EtlJob --> ApiConfig : targetConfig
    EtlJob --> ScheduleConfig : schedule
    JobExecutionInfo --> JobStatus : status
    ExecutionMetrics --> EtlJob : jobId
```

---

## 🔀 5. Pipeline de Transformação (Pipes and Filters)

```mermaid
graph LR
    Input[EtlMessage<br/>Dados do Banco] --> Normalizer[Normalizer<br/>- Normalizar datas<br/>- Normalizar decimais<br/>- Normalizar nomes de colunas<br/>- Tratar valores nulos]
    
    Normalizer --> ContentEnricher[ContentEnricher<br/>- Adicionar metadata<br/>- Adicionar estatísticas]
    
    ContentEnricher --> Translator[DatabaseToJsonTranslator<br/>- Converter ResultSet para JSON<br/>- Formatação opcional]
    
    Translator --> Output[EtlMessage<br/>JSON Formatado]
    
    Normalizer -.->|WireTap| Monitor[MessageStore<br/>Auditoria]
    ContentEnricher -.->|WireTap| Monitor
    Translator -.->|WireTap| Monitor
    Output -.->|WireTap| Monitor
    
    style Input fill:#e1f5ff
    style Output fill:#e1f5ff
    style Monitor fill:#fff4e1
```

---

## 🎯 6. Estados do Job ETL

```mermaid
stateDiagram-v2
    [*] --> Created: Criar Job
    
    Created --> Validating: Validar Configuração
    
    Validating --> Invalid: Validação Falhou
    Validating --> Enabled: Validação OK
    
    Enabled --> Disabled: Desabilitar
    Disabled --> Enabled: Habilitar
    
    Enabled --> Scheduled: Agendar Job
    Enabled --> Running: Executar Manualmente
    
    Scheduled --> Running: Trigger Agendado
    Scheduled --> Paused: Pausar
    Paused --> Scheduled: Retomar
    
    Running --> Extracting: Iniciar Extract
    Extracting --> Transforming: Extract OK
    Extracting --> Failed: Extract Falhou
    
    Transforming --> Loading: Transform OK
    Transforming --> Failed: Transform Falhou
    
    Loading --> Success: Load OK
    Loading --> Failed: Load Falhou
    
    Failed --> DeadLetter: Enviar para DLC
    Failed --> Enabled: Retry Configurado
    
    Success --> Enabled: Concluído
    DeadLetter --> [*]: Finalizado
    
    Invalid --> [*]: Job Inválido
    
    note right of Running
        Job em execução
        - Extract
        - Transform
        - Load
    end note
    
    note right of Failed
        Tratamento de Erro:
        - Retry Handler
        - Dead Letter Channel
        - Log de Erro
    end note
```

---

## 🔐 7. Regras de Negócio - Validação de Job

```mermaid
flowchart TD
    Start([Iniciar Validação]) --> CheckID{ID válido?}
    CheckID -->|Não| Invalid1[Job Inválido:<br/>ID obrigatório]
    CheckID -->|Sim| CheckName{Nome válido?}
    
    CheckName -->|Não| Invalid2[Job Inválido:<br/>Nome obrigatório]
    CheckName -->|Sim| CheckSource{SourceConfig válido?}
    
    CheckSource -->|Não| Invalid3[Job Inválido:<br/>Configuração de origem inválida]
    CheckSource -->|Sim| CheckSQL{SQL Query válida?}
    
    CheckSQL -->|Não| Invalid4[Job Inválido:<br/>Query SQL obrigatória]
    CheckSQL -->|Sim| CheckTarget{TargetConfig válido?}
    
    CheckTarget -->|Não| Invalid5[Job Inválido:<br/>Configuração de destino inválida]
    CheckTarget -->|Sim| Valid[Job Válido ✓]
    
    Invalid1 --> End([Fim])
    Invalid2 --> End
    Invalid3 --> End
    Invalid4 --> End
    Invalid5 --> End
    Valid --> End
    
    style Valid fill:#90EE90
    style Invalid1 fill:#FFB6C6
    style Invalid2 fill:#FFB6C6
    style Invalid3 fill:#FFB6C6
    style Invalid4 fill:#FFB6C6
    style Invalid5 fill:#FFB6C6
```

---

## 🔄 8. Regras de Negócio - Fluxo de Execução

```mermaid
flowchart TD
    Start([Iniciar Execução]) --> CheckValid{Job válido?}
    CheckValid -->|Não| Error1[Erro: Job inválido]
    CheckValid -->|Sim| CheckEnabled{Job habilitado?}
    
    CheckEnabled -->|Não| Cancelled[Execução Cancelada:<br/>Job desabilitado]
    CheckEnabled -->|Sim| StartExecution[Iniciar Execução]
    
    StartExecution --> Extract[ETAPA 1: EXTRACT]
    Extract --> ExtractSuccess{Extract OK?}
    
    ExtractSuccess -->|Não| Fail1[Falha: Erro na extração]
    ExtractSuccess -->|Sim| Transform[ETAPA 2: TRANSFORM]
    
    Transform --> TransformSuccess{Transform OK?}
    TransformSuccess -->|Não| Fail2[Falha: Erro na transformação]
    TransformSuccess -->|Sim| Load[ETAPA 3: LOAD]
    
    Load --> LoadSuccess{Load OK?}
    LoadSuccess -->|Não| Retry{Retry disponível?}
    LoadSuccess -->|Sim| Success[Sucesso ✓]
    
    Retry -->|Sim| Load
    Retry -->|Não| Fail3[Falha: Erro no carregamento]
    Fail3 --> DeadLetter[Enviar para Dead Letter Channel]
    
    Extract --> Monitor[Registrar Métricas]
    Transform --> Monitor
    Load --> Monitor
    Success --> Monitor
    Fail1 --> Monitor
    Fail2 --> Monitor
    Fail3 --> Monitor
    
    Error1 --> End([Fim])
    Cancelled --> End
    Success --> End
    DeadLetter --> End
    
    style Success fill:#90EE90
    style Error1 fill:#FFB6C6
    style Fail1 fill:#FFB6C6
    style Fail2 fill:#FFB6C6
    style Fail3 fill:#FFB6C6
    style Cancelled fill:#FFE4B5
    style DeadLetter fill:#DDA0DD
```

---

## 🔌 9. Gerenciamento de Conexões

```mermaid
graph TB
    subgraph "Connection Manager"
        CM[ConnectionManager<br/>Singleton]
        DSMap[Map de DataSources<br/>ConcurrentHashMap]
    end

    subgraph "Pool de Conexões (HikariCP)"
        HikariPool1[HikariPool-Firebird]
        HikariPool2[HikariPool-MySQL]
        HikariPool3[HikariPool-PostgreSQL]
        HikariPool4[HikariPool-SQLServer]
    end

    subgraph "Bancos de Dados"
        DB1[(Firebird)]
        DB2[(MySQL)]
        DB3[(PostgreSQL)]
        DB4[(SQL Server)]
    end

    CM --> DSMap
    DSMap -->|Cria/Gerencia| HikariPool1
    DSMap -->|Cria/Gerencia| HikariPool2
    DSMap -->|Cria/Gerencia| HikariPool3
    DSMap -->|Cria/Gerencia| HikariPool4

    HikariPool1 --> DB1
    HikariPool2 --> DB2
    HikariPool3 --> DB3
    HikariPool4 --> DB4

    note1[Regras de Pool:<br/>- minSize: 2<br/>- maxSize: 10<br/>- connectionTimeout: 30000ms<br/>- idleTimeout: 600000ms<br/>- maxLifetime: 1800000ms]

    style CM fill:#e1f5ff
    style note1 fill:#fff4e1
```

---

## 📋 10. Regras de Negócio - Lista Detalhada

### 10.1. Regras de Validação de Job

```mermaid
mindmap
  root((Validação<br/>de Job))
    ID
      Não pode ser nulo
      Não pode ser vazio
      Deve ser único
    Nome
      Não pode ser nulo
      Não pode ser vazio
      Mínimo 3 caracteres
    Configuração de Origem
      DatabaseConfig válido
      Conexão testável
      Query SQL não vazia
    Configuração de Destino
      ApiConfig válido
      URL válida
      Método HTTP válido
    Agendamento
      Cron expression válida (se habilitado)
      Data de início <= Data de fim
```

### 10.2. Regras de Execução

```mermaid
mindmap
  root((Regras de<br/>Execução))
    Pré-Execução
      Job deve estar válido
      Job deve estar habilitado
      Conexão de origem deve estar disponível
      API de destino deve estar acessível
    Durante Execução
      Cada etapa pode falhar independentemente
      Falha em uma etapa interrompe o fluxo
      Métricas são coletadas em cada etapa
      Mensagens são interceptadas (WireTap)
    Pós-Execução
      Resultado é registrado
      Métricas são armazenadas
      Mensagens de auditoria são salvas
      Dead Letter Channel recebe falhas
```

### 10.3. Regras de Transformação

```mermaid
mindmap
  root((Regras de<br/>Transformação))
    Normalização
      Datas: Converter para ISO 8601
      Decimais: Normalizar precisão
      Colunas: Snake_case ou CamelCase
      Nulos: Manter ou remover
    Enriquecimento
      Adicionar metadata (timestamp, jobId)
      Adicionar estatísticas (contagem)
      Opcional conforme configuração
    Tradução
      ResultSet → JSON
      Arrays de objetos
      Formatação configurável
      Preservar tipos de dados
```

### 10.4. Regras de Tratamento de Erro

```mermaid
mindmap
  root((Tratamento<br/>de Erro))
    Retry Handler
      Número máximo de tentativas
      Delay entre tentativas
      Backoff exponencial opcional
    Dead Letter Channel
      Mensagens falhadas são armazenadas
      Permite reprocessamento manual
      Mantém histórico de erros
    Logging
      Todos os erros são logados
      Níveis: ERROR, WARN, INFO
      Arquivo de log persistente
```

---

## 🗄️ 11. Persistência de Dados

```mermaid
graph LR
    subgraph "Configurações (JSON)"
        Jobs[config/jobs.json<br/>- EtlJob[]]
        Connections[config/connections.json<br/>- DatabaseConfig[]]
        Apis[config/apis.json<br/>- ApiConfig[]]
    end

    subgraph "Dados Temporários"
        MessageStore[data/message-store/<br/>*.json<br/>Mensagens interceptadas]
        DeadLetter[data/dead-letter/<br/>*.json<br/>Mensagens falhadas]
        Logs[logs/<br/>etl-application.log<br/>Logs do sistema]
    end

    subgraph "Serviços de Configuração"
        JobService[JobConfigService]
        ConnectionService[ConnectionConfigService]
        ApiService[ApiConfigService]
    end

    JobService --> Jobs
    ConnectionService --> Connections
    ApiService --> Apis
    WireTap --> MessageStore
    DeadLetterChannel --> DeadLetter
    LogAppender --> Logs
```

---

## 🎛️ 12. Agendamento de Jobs

```mermaid
sequenceDiagram
    participant UI as Interface
    participant SS as SchedulerService
    participant JS as JobScheduler
    participant Quartz as Quartz Scheduler
    participant EO as EtlOrchestrator

    UI->>SS: scheduleJob(job)
    SS->>SS: Validar ScheduleConfig
    alt Configuração Inválida
        SS-->>UI: false
    else Configuração Válida
        SS->>JS: scheduleJob(job, scheduleConfig)
        JS->>Quartz: Criar Trigger (Cron)
        JS->>Quartz: Criar JobDetail
        Quartz-->>JS: Trigger agendado
        JS-->>SS: true
        SS-->>UI: true
    end

    Note over Quartz: Quando trigger dispara
    Quartz->>JS: execute(jobContext)
    JS->>EO: execute(job)
    EO-->>JS: JobExecutionInfo
    JS->>JS: Registrar resultado
```

---

## 📊 13. Monitoramento e Métricas

```mermaid
graph TB
    subgraph "Coleta de Métricas"
        EO[EtlOrchestrator] --> EM[ExecutionMetrics]
        Extract --> EM
        Transform --> EM
        Load --> EM
    end

    subgraph "Armazenamento"
        EM --> MS[MessageStore<br/>Mensagens Interceptadas]
        EM --> Logs[LogAppender<br/>Logs do Sistema]
    end

    subgraph "Visualização"
        MS --> MC[MonitoringController<br/>Dashboard]
        Logs --> MC
        EM --> MC
    end

    subgraph "Métricas Coletadas"
        M1[Duração por Etapa]
        M2[Contagem de Registros]
        M3[Taxa de Sucesso/Falha]
        M4[Status da Execução]
        M5[Mensagens de Erro]
    end

    EM --> M1
    EM --> M2
    EM --> M3
    EM --> M4
    EM --> M5

    style EO fill:#e1f5ff
    style MC fill:#90EE90
```

---

## 🔒 14. Autenticação em APIs

```mermaid
graph LR
    subgraph "Tipos de Autenticação"
        None[Nenhuma]
        Bearer[Bearer Token]
        Basic[Basic Auth]
        ApiKey[API Key]
    end

    subgraph "Configuração"
        Config[ApiConfig] --> AuthType
        AuthType --> None
        AuthType --> Bearer
        AuthType --> Basic
        AuthType --> ApiKey
    end

    subgraph "Aplicação"
        Client[RestApiClient] --> Headers[HTTP Headers]
        Bearer --> Headers
        Basic --> Headers
        ApiKey --> Headers
        Headers --> Request[HTTP Request]
    end

    style Config fill:#e1f5ff
    style Request fill:#90EE90
```

---

## 📝 Resumo das Regras de Negócio

### Regras de Validação

1. **Job ETL deve ser válido antes da execução:**
   - ID obrigatório e não vazio
   - Nome obrigatório e não vazio
   - Configuração de origem (DatabaseConfig) válida
   - Query SQL obrigatória e não vazia
   - Configuração de destino (ApiConfig) válida

2. **Job deve estar habilitado para executar:**
   - Jobs desabilitados não são executados
   - Status é verificado antes de iniciar execução

### Regras de Execução

3. **Fluxo ETL é sequencial:**
   - Extract → Transform → Load
   - Falha em qualquer etapa interrompe o fluxo
   - Métricas são coletadas em cada etapa

4. **Tratamento de erros:**
   - Retry automático configurável (LoadService)
   - Dead Letter Channel para mensagens falhadas
   - Logs detalhados de todas as operações

### Regras de Conexão

5. **Pool de conexões:**
   - Uma conexão por DatabaseConfig
   - Pool gerenciado pelo HikariCP
   - Validação automática de conexões

6. **Reutilização de conexões:**
   - DataSources são reutilizados
   - Conexões são obtidas do pool
   - Fechamento automático após uso

### Regras de Transformação

7. **Pipeline de transformação:**
   - Normalização obrigatória
   - Enriquecimento opcional
   - Tradução para JSON obrigatória

8. **Interceptação de mensagens:**
   - WireTap intercepta todas as mensagens
   - Mensagens são armazenadas para auditoria
   - Monitoramento em tempo real

### Regras de Agendamento

9. **Agendamento de jobs:**
   - Expressões Cron válidas
   - Jobs podem ser pausados/retomados
   - Agendamento independente da execução manual

---

**Última atualização:** Baseado no código-fonte atual do projeto PlugWay ETL

