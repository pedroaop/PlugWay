# 🗺️ PlugWay ETL - Roadmap

> Roadmap de desenvolvimento para expansão de fontes de origem e destino

## 📊 Visão Geral

Este documento apresenta o roadmap para expandir o PlugWay ETL com suporte a múltiplas fontes de origem e destino, transformando-o em uma plataforma de integração de dados mais completa e versátil.

### ✅ Status Atual

**Fontes de Origem Suportadas:**
- ✅ Firebird
- ✅ MySQL
- ✅ PostgreSQL
- ✅ SQL Server

**Destinos Suportados:**
- ✅ APIs REST (POST, GET, PUT, DELETE)

---

## 🎯 Objetivos Estratégicos

1. **Sistema de Plugins Extensível**: Implementar arquitetura modular com interface genérica de conectores, sistema de descoberta automática de plugins, carregamento dinâmico de classes, SDK para desenvolvedores e gerenciamento completo via interface gráfica

2. **Expansão de Formatos de Arquivos**: Adicionar suporte completo para CSV, Excel (XLSX/XLS), JSON e XML como fontes de origem e destino, com funcionalidades avançadas de parsing, transformação e geração

3. **Integração com Message Queues**: Implementar conectores para Kafka e RabbitMQ, suportando consumo e publicação de mensagens, gerenciamento de offsets, consumer groups, exchanges e routing keys

4. **APIs e Web Services**: Expandir suporte a APIs REST como fonte de dados com paginação, rate limiting e OAuth 2.0, além de implementar webhooks para recepção de eventos em tempo real

5. **Cloud Storage e Protocolos**: Adicionar conectores para Amazon S3 e protocolos FTP/SFTP, permitindo integração completa com serviços cloud e sistemas de arquivos remotos

---

## 🔌 Sistema de Plugins

- [ ] **🔧 Arquitetura de Plugins**
  - Interface `Connector` genérica
  - Sistema de descoberta de plugins
  - Carregamento dinâmico de classes

- [ ] **📚 API de Desenvolvimento**
  - SDK para desenvolvedores
  - Documentação de extensibilidade
  - Exemplos de plugins

- [ ] **⚙️ Gerenciamento de Plugins**
  - Instalação/desinstalação via UI
  - Atualização de plugins
  - Validação de compatibilidade

---

## 🔄 Suporte a novas fontes de origem e destino

- [ ] **📄 CSV**
  - Parser com suporte a delimitadores customizados
  - Detecção automática de encoding
  - Tratamento de headers
  - Escrita com delimitadores configuráveis
  - Suporte a headers  

- [ ] **📊 Excel (XLSX/XLS)**
  - Suporte a múltiplas planilhas
  - Leitura de ranges específicos
  - Criação de planilhas
  - Formatação básica
  - Múltiplas planilhas  

- [ ] **📋 JSON**
  - Leitura de arquivos JSON simples
  - Suporte a JSON Lines (JSONL)
  - Arrays e objetos aninhados
  - Escrita formatada ou compacta
  - Suporte a arrays e objetos  

- [ ] **📝 XML**
  - Suporte a XPath para seleção de nós
  - Transformação para JSON
  - Geração de XML a partir de JSON
  - Templates configuráveis  

- [ ] **🚀 Kafka**
  - Consumer groups
  - Offsets management
  - Múltiplos tópicos
  - Producer com acks configuráveis
  - Particionamento customizado
  - Serialização JSON

- [ ] **🐰 RabbitMQ**
  - Consumo de filas
  - Exchanges e routing keys
  - Publicação em filas
  - Suporte a exchanges

- [ ] **🌐 REST**
  - Extração de Dados via REST
  - Suporte a paginação
  - Rate limiting
  - Autenticação OAuth 2.0

- [ ] **🔔 Webhooks**
  - Servidor HTTP embutido
  - Recepção de eventos

- [ ] **☁️ S3**
  - Listagem de objetos
  - Download de arquivos
  - Filtros por prefixo/data
  - Upload de arquivos
  - Multipart upload para arquivos grandes

- [ ] **📁 FTP/SFTP**
  - Listagem e download
  - Upload de arquivos

---

## 🔄 Priorização Contínua

Este roadmap é um documento vivo e será atualizado conforme:
- Feedback dos usuários
- Demandas do mercado
- Novas tecnologias emergentes
- Recursos disponíveis

---

## 🤝 Contribuições

Contribuições são bem-vindas! Se você tem interesse em implementar algum conector específico, entre em contato ou abra uma issue para discussão.