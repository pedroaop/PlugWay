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
- [Desenvolvimento](#-desenvolvimento)
- [Testes](#-testes)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

## 🎯 Sobre

**PlugWay ETL** é uma aplicação desktop desenvolvida em Java que facilita a integração entre diferentes fontes de dados (bancos de dados relacionais) e APIs REST. A aplicação implementa padrões EIP (Enterprise Integration Patterns) para garantir uma arquitetura robusta, escalável e fácil de manter.

### Principais Características

- **Extract (Extração)**: Consulta dados de bancos de dados relacionais (Firebird, MySQL, PostgreSQL, SQL Server)
- **Transform (Transformação)**: Aplica transformações nos dados usando padrões EIP
- **Load (Carregamento)**: Envia dados transformados para APIs REST
- **Interface Gráfica**: Interface desktop moderna e intuitiva desenvolvida com JavaFX
- **Agendamento**: Execução automática de jobs através de agendamento configurável
- **Monitoramento**: Acompanhamento em tempo real de execuções e métricas
- **Logs**: Sistema completo de logging para auditoria e debugging

## ✨ Funcionalidades

### 🔌 Gerenciamento de Conexões

- Configuração de múltiplas conexões de banco de dados
- Suporte para Firebird, MySQL, PostgreSQL e SQL Server
- Pool de conexões configurável (HikariCP)
- Teste de conectividade em tempo real
- Gerenciamento de credenciais seguro

### 📡 Gerenciamento de APIs

- Configuração de endpoints REST
- Suporte para autenticação (Bearer Token, Basic Auth, API Key)
- Configuração de timeouts e retries
- Validação de conectividade

### ⚙️ Gerenciamento de Jobs ETL

- Criação e edição de jobs ETL
- Configuração de queries SQL para extração
- Definição de transformações de dados
- Configuração de endpoints de destino
- Validação de configurações antes da execução
- Execução manual ou agendada

### 🔄 Transformações

- Normalização de dados
- Tradução de formato (Database → JSON)
- Validação de schema JSON
- Enriquecimento de conteúdo
- Pipeline de transformações configurável

### 📅 Agendamento

- Agendamento de execução de jobs
- Suporte a expressões Cron
- Execução única ou recorrente
- Controle de jobs agendados (ativar/desativar)

### 📊 Monitoramento

- Dashboard em tempo real
- Métricas de execução (tempo, registros processados)
- Histórico de execuções
- Status de jobs (sucesso, falha, em execução)
- Visualização de mensagens interceptadas

### 📝 Logs

- Visualização de logs em tempo real
- Filtros por nível (INFO, WARN, ERROR)
- Exportação de logs
- Histórico completo de execuções

### 🏗️ Padrões EIP Implementados

- **Pipeline**: Encadeamento de transformações
- **Wire Tap**: Interceptação de mensagens para monitoramento
- **Control Bus**: Gerenciamento centralizado de mensagens
- **Message Transformer**: Transformação de mensagens
- **Dead Letter Channel**: Tratamento de mensagens com falha
- **Retry Handler**: Re-tentativas automáticas
- **Content Enricher**: Enriquecimento de dados

## 🛠️ Tecnologias

### Core

- **Java 17**: Linguagem de programação
- **JavaFX 21.0.1**: Framework de interface gráfica
- **Maven**: Gerenciamento de dependências e build

### Banco de Dados

- **HikariCP 5.0.1**: Pool de conexões
- **Jaybird 4.0.9**: Driver Firebird
- **MySQL Connector 8.1.0**: Driver MySQL
- **PostgreSQL 42.6.0**: Driver PostgreSQL
- **Microsoft SQL Server JDBC 12.4.1**: Driver SQL Server

### Integração e Processamento

- **Jackson 2.15.2**: Processamento JSON
- **Quartz 2.3.2**: Agendamento de tarefas
- **Typesafe Config 1.4.3**: Gerenciamento de configurações

### Logging

- **SLF4J 2.0.9**: API de logging
- **Logback 1.4.11**: Implementação de logging

### Validação

- **Jakarta Validation 3.0.2**: Validação de dados
- **Hibernate Validator 8.0.0**: Implementação de validação

### Testes

- **JUnit 5.10.0**: Framework de testes
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

1. **application.properties**: Propriedades simples
2. **application.conf**: Configuração HOCON (Typesafe Config)

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

### Diretórios

A aplicação cria os seguintes diretórios automaticamente:

- `config/`: Configurações de jobs e conexões
- `config/jobs/`: Definições de jobs ETL
- `logs/`: Arquivos de log
- `data/`: Dados temporários e cache
- `data/message-store/`: Armazenamento de mensagens para auditoria

## 💻 Uso

### Primeiros Passos

1. **Iniciar a Aplicação**
   - Execute o JAR ou use o Maven para iniciar
   - A interface gráfica será exibida

2. **Configurar Conexão de Banco de Dados**
   - Acesse o menu "Conexões" → "Gerenciar Conexões DB"
   - Clique em "Nova Conexão"
   - Preencha os dados de conexão (tipo, host, porta, banco, usuário, senha)
   - Teste a conexão antes de salvar

3. **Configurar API de Destino**
   - Acesse o menu "Conexões" → "Gerenciar APIs"
   - Clique em "Nova API"
   - Configure a URL base, método HTTP e autenticação
   - Teste a conectividade

4. **Criar um Job ETL**
   - Acesse "Jobs" → "Novo Job"
   - Configure:
     - **Origem**: Selecione a conexão de banco e informe a query SQL
     - **Transformação**: Configure as transformações necessárias
     - **Destino**: Selecione a API de destino e configure o endpoint
   - Valide o job
   - Salve o job

5. **Executar um Job**
   - Na lista de jobs, selecione o job desejado
   - Clique em "Executar"
   - Acompanhe o progresso no painel de monitoramento

6. **Agendar um Job**
   - Acesse "Jobs" → "Agendamento"
   - Selecione o job
   - Configure a expressão Cron ou horário
   - Ative o agendamento

### Exemplo de Job ETL

```sql
-- Query de Extração (Origem)
SELECT 
    id,
    nome,
    email,
    data_cadastro
FROM usuarios
WHERE data_cadastro >= CURRENT_DATE - INTERVAL '7' DAY
```

**Transformação**: Converter para JSON e normalizar campos

**Destino**: POST para `https://api.exemplo.com/v1/usuarios`

## 🏛️ Arquitetura

### Padrão Arquitetural

A aplicação segue uma arquitetura em camadas:

```
┌─────────────────────────────────────┐
│         Interface Gráfica           │
│          (JavaFX UI)                │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│         Camada de Serviços          │
│  - Orchestrator                     │
│  - Transform                        │
│  - Load                             │
│  - Scheduler                        │
│  - Monitoring                       │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│      Padrões EIP (Enterprise        │
│      Integration Patterns)          │
│  - Pipeline                         │
│  - Wire Tap                         │
│  - Control Bus                      │
│  - Message Transformer              │
└─────────────────────────────────────┘
              │
┌─────────────────────────────────────┐
│      Camada de Acesso a Dados       │
│  - Extract Service                  │
│  - Connection Manager               │
│  - Query Executor                   │
└─────────────────────────────────────┘
```

### Fluxo de Execução ETL

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────┐
│ Extract  │ --> │  Transform   │ --> │    Load     │ --> │   API    │
│   (DB)   │     │   (Pipeline) │     │  (REST API) │     │  (Dest)  │
└──────────┘     └──────────────┘     └─────────────┘     └──────────┘
      │                  │                     │
      ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────┐
│              Monitoramento e Logging                    │
└─────────────────────────────────────────────────────────┘
```

## 📁 Estrutura do Projeto

```
plugway/
├── src/
│   ├── main/
│   │   ├── java/com/plugway/etl/
│   │   │   ├── config/          # Gerenciamento de configurações
│   │   │   ├── dao/             # Acesso a dados (Extract)
│   │   │   ├── eip/             # Padrões Enterprise Integration Patterns
│   │   │   ├── model/           # Modelos de dados
│   │   │   ├── service/         # Serviços de negócio
│   │   │   │   ├── load/        # Serviço de carga (Load)
│   │   │   │   ├── monitoring/  # Monitoramento
│   │   │   │   ├── orchestrator/# Orquestração ETL
│   │   │   │   ├── scheduler/   # Agendamento
│   │   │   │   └── transform/   # Transformações
│   │   │   ├── ui/              # Interface gráfica (JavaFX)
│   │   │   └── util/            # Utilitários
│   │   └── resources/
│   │       ├── fxml/            # Arquivos FXML da interface
│   │       ├── css/             # Estilos CSS
│   │       ├── images/          # Imagens
│   │       ├── application.properties
│   │       ├── application.conf
│   │       └── logback.xml
│   └── test/                    # Testes unitários e de integração
├── config/                      # Configurações de jobs
├── data/                        # Dados temporários
├── logs/                        # Arquivos de log
├── pom.xml                      # Configuração Maven
└── README.md                    # Este arquivo
```

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

### Comandos Úteis

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Gerar JAR executável
mvn clean package

# Executar aplicação
mvn javafx:run

# Limpar e reconstruir
mvn clean install

# Gerar relatório de cobertura de testes
mvn test jacoco:report
```

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=NomeDaClasseTest

# Testes com cobertura
mvn test jacoco:report
```

### Tipos de Testes

- **Testes Unitários**: Serviços e utilitários
- **Testes de Integração**: Fluxo ETL completo
- **Testes de Interface**: TestFX para componentes JavaFX

### Cobertura de Testes

A aplicação possui testes para:

- ✅ Serviços de extração (ExtractService)
- ✅ Serviços de transformação (TransformService)
- ✅ Serviços de carga (LoadService)
- ✅ Orquestrador ETL (EtlOrchestrator)
- ✅ Padrões EIP (Pipeline, WireTap)
- ✅ Gerenciamento de conexões
- ✅ Agendamento

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
- Documente classes e métodos públicos
- Adicione testes para novas funcionalidades
- Mantenha a cobertura de testes acima de 70%

## 📄 Licença

Este projeto está licenciado sob a [Apache License 2.0](LICENSE). Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 📞 Suporte

Para dúvidas, problemas ou sugestões:

- Abra uma [Issue](https://github.com/seu-usuario/plugway/issues)
- Entre em contato com a equipe de desenvolvimento

## 🙏 Agradecimentos

- Comunidade JavaFX
- Projeto Apache Camel (inspiração para padrões EIP)
- Todos os contribuidores e mantenedores

---

**Desenvolvido com ❤️ usando Java e JavaFX**

