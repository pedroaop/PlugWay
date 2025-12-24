package com.plugway.etl.ui;

import com.plugway.etl.model.EtlJob;
import com.plugway.etl.service.JobConfigService;
import com.plugway.etl.service.monitoring.LogAppender;
import com.plugway.etl.service.orchestrator.MessagingGateway;
import com.plugway.etl.ui.LogsController.LogEntry;
import com.plugway.etl.util.LoggerUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller da tela principal da aplicação.
 */
public class MainController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(MainController.class);
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private BorderPane contentArea;
    
    @FXML
    private TextArea terminalLogsArea;
    
    @FXML
    private Button clearLogsButton;
    
    private VBox terminalContent;
    
    @FXML
    private MenuItem terminalMenuItem;
    @FXML
    private MenuItem manageJobsMenuItem;
    @FXML
    private MenuItem databasesMenuItem;
    @FXML
    private MenuItem apisMenuItem;
    @FXML
    private MenuItem clearLogsMenuItem;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem executeJobsMenuItem;
    @FXML
    private MenuItem schedulerMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    
    private Stage stage;
    private LogAppender logAppender;
    private static final int MAX_TERMINAL_LINES = 1000;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando MainController");
        
        if (statusLabel != null) {
            statusLabel.setText("Sistema ETL iniciado com sucesso");
        }
        
        // Armazena referência ao conteúdo original do Terminal
        if (contentArea != null && contentArea.getCenter() != null) {
            terminalContent = (VBox) contentArea.getCenter();
        }
        
        // Configura terminal de logs
        setupTerminalLogs();
        
        // Configura itens de menu de navegação com ícones
        if (terminalMenuItem != null) {
            // Usa CODE como ícone de terminal (TERMINAL pode não existir em todas as versões do FontAwesome)
            IconHelper.setIconToMenuItem(terminalMenuItem, FontAwesomeSolid.CODE, IconHelper.SIZE_SMALL);
            terminalMenuItem.setOnAction(e -> showTerminal());
        }
        if (manageJobsMenuItem != null) {
            IconHelper.setIconToMenuItem(manageJobsMenuItem, FontAwesomeSolid.TASKS, IconHelper.SIZE_SMALL);
            manageJobsMenuItem.setOnAction(e -> showJobs());
        }
        if (databasesMenuItem != null) {
            IconHelper.setIconToMenuItem(databasesMenuItem, FontAwesomeSolid.DATABASE, IconHelper.SIZE_SMALL);
            databasesMenuItem.setOnAction(e -> showConnections());
        }
        if (apisMenuItem != null) {
            IconHelper.setIconToMenuItem(apisMenuItem, FontAwesomeSolid.GLOBE, IconHelper.SIZE_SMALL);
            apisMenuItem.setOnAction(e -> showApis());
        }
        if (clearLogsMenuItem != null) {
            IconHelper.setIconToMenuItem(clearLogsMenuItem, FontAwesomeSolid.BROOM, IconHelper.SIZE_SMALL);
        }
        
        // Configura ícones para menu Arquivo
        if (exitMenuItem != null) {
            IconHelper.setIconToMenuItem(exitMenuItem, FontAwesomeSolid.SIGN_OUT_ALT, IconHelper.SIZE_SMALL);
        }
        
        // Configura ícones para menu Jobs
        if (executeJobsMenuItem != null) {
            IconHelper.setIconToMenuItem(executeJobsMenuItem, FontAwesomeSolid.PLAY, IconHelper.SIZE_SMALL);
        }
        if (schedulerMenuItem != null) {
            IconHelper.setIconToMenuItem(schedulerMenuItem, FontAwesomeSolid.CLOCK, IconHelper.SIZE_SMALL);
        }
        
        // Configura ícone para menu Ajuda
        if (aboutMenuItem != null) {
            IconHelper.setIconToMenuItem(aboutMenuItem, FontAwesomeSolid.INFO_CIRCLE, IconHelper.SIZE_SMALL);
        }
    }
    
    @FXML
    private void showTerminal() {
        logger.debug("Mostrando Terminal");
        
        if (contentArea != null) {
            // Restaura o conteúdo original do Terminal (terminal de logs)
            if (terminalContent != null) {
                contentArea.setCenter(terminalContent);
            } else {
                // Se não tiver referência, recria o conteúdo do Terminal
                terminalContent = createTerminalContent();
                contentArea.setCenter(terminalContent);
            }
        }
        
        updateStatus("Terminal de Logs");
    }
    
    /**
     * Cria o conteúdo do Terminal (terminal de logs).
     */
    private VBox createTerminalContent() {
        VBox vbox = new VBox(5);
        vbox.setStyle("-fx-padding: 10;");
        
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Terminal de Logs");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearBtn = new Button("Limpar");
        clearBtn.getStyleClass().add("terminal-button");
        clearBtn.setOnAction(e -> clearTerminalLogs());
        
        hbox.getChildren().addAll(titleLabel, spacer, clearBtn);
        
        // Usa o terminalLogsArea existente (já configurado no initialize)
        if (terminalLogsArea != null) {
            VBox.setVgrow(terminalLogsArea, Priority.ALWAYS);
            vbox.getChildren().addAll(hbox, terminalLogsArea);
        } else {
            // Se por algum motivo não existir, cria um novo
            TextArea newTerminal = new TextArea();
            newTerminal.setEditable(false);
            newTerminal.getStyleClass().add("terminal-text-area");
            newTerminal.setStyle(
                "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                "-fx-font-size: 11px; " +
                "-fx-background-color: #1e1e1e; " +
                "-fx-text-fill: #d4d4d4; " +
                "-fx-control-inner-background: #1e1e1e;"
            );
            VBox.setVgrow(newTerminal, Priority.ALWAYS);
            vbox.getChildren().addAll(hbox, newTerminal);
        }
        
        return vbox;
    }
    
    @FXML
    private void showJobs() {
        logger.debug("Mostrando Jobs");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/JobManagerView.fxml"));
            Parent root = loader.load();
            
            JobManagerController controller = loader.getController();
            controller.setStage(stage);
            // TODO: Injetar conexões e APIs compartilhadas
            
            if (contentArea != null) {
                contentArea.setCenter(root);
            } else {
                // Abre em nova janela se contentArea não estiver disponível
                openInNewWindow(root, "Gerenciamento de Jobs ETL");
            }
            
            updateStatus("Gerenciamento de Jobs");
        } catch (IOException e) {
            logger.error("Erro ao carregar tela de Jobs", e);
        }
    }
    
    @FXML
    private void showConnections() {
        logger.debug("Mostrando Conexões DB");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConnectionManagerView.fxml"));
            Parent root = loader.load();
            
            ConnectionManagerController controller = loader.getController();
            controller.setStage(stage);
            
            if (contentArea != null) {
                contentArea.setCenter(root);
            } else {
                openInNewWindow(root, "Gerenciamento de Conexões DB");
            }
            
            updateStatus("Gerenciamento de Conexões DB");
        } catch (IOException e) {
            logger.error("Erro ao carregar tela de Conexões", e);
        }
    }
    
    @FXML
    private void showApis() {
        logger.debug("Mostrando APIs");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ApiManagerView.fxml"));
            Parent root = loader.load();
            
            ApiManagerController controller = loader.getController();
            controller.setStage(stage);
            
            if (contentArea != null) {
                contentArea.setCenter(root);
            } else {
                openInNewWindow(root, "Gerenciamento de APIs REST");
            }
            
            updateStatus("Gerenciamento de APIs REST");
        } catch (IOException e) {
            logger.error("Erro ao carregar tela de APIs", e);
        }
    }
    
    @FXML
    private void showScheduler() {
        logger.debug("Mostrando Agendamento");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SchedulerView.fxml"));
            Parent root = loader.load();
            
            SchedulerController controller = loader.getController();
            controller.setStage(stage);
            
            // Carrega jobs disponíveis
            JobConfigService jobConfigService = JobConfigService.getInstance();
            javafx.collections.ObservableList<EtlJob> jobs = 
                javafx.collections.FXCollections.observableArrayList(jobConfigService.loadJobs());
            controller.setAvailableJobs(jobs);
            controller.refreshScheduledJobs();
            
            if (contentArea != null) {
                contentArea.setCenter(root);
            } else {
                openInNewWindow(root, "Agendamento de Jobs");
            }
            
            updateStatus("Agendamento de Jobs");
        } catch (IOException e) {
            logger.error("Erro ao carregar tela de Agendamento", e);
        }
    }
    
    private void openInNewWindow(Parent root, String title) {
        Stage newStage = new Stage();
        newStage.setTitle(title);
        newStage.initModality(Modality.NONE);
        newStage.initOwner(stage);
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        
        newStage.setScene(scene);
        newStage.show();
    }
    
    /**
     * Define o Stage principal.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Atualiza o status na interface.
     */
    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        logger.debug("Status atualizado: {}", message);
    }
    
    /**
     * Configura o terminal de logs estilo console.
     */
    private void setupTerminalLogs() {
        if (terminalLogsArea == null) {
            return;
        }
        
        // Configura estilo do terminal
        terminalLogsArea.setStyle(
            "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
            "-fx-font-size: 11px; " +
            "-fx-background-color: #1e1e1e; " +
            "-fx-text-fill: #d4d4d4; " +
            "-fx-control-inner-background: #1e1e1e;"
        );
        
        // Conecta ao LogAppender
        logAppender = LogAppender.getInstance();
        logAppender.addLogListener(this::appendLogToTerminal);
        
        // Configura botão de limpar
        if (clearLogsButton != null) {
            clearLogsButton.setOnAction(e -> clearTerminalLogs());
        }
        
        // Mensagem inicial
        appendToTerminal("=== PlugWay ETL - Terminal de Logs ===\n");
        appendToTerminal("Sistema iniciado. Aguardando logs...\n\n");
    }
    
    /**
     * Adiciona uma entrada de log ao terminal.
     */
    private void appendLogToTerminal(LogEntry entry) {
        Platform.runLater(() -> {
            StringBuilder logLine = new StringBuilder();
            
            // Formata timestamp
            logLine.append("[").append(entry.getTimestamp()).append("] ");
            
            // Formata nível
            String level = entry.getLevel();
            String levelPrefix = "";
            
            switch (level) {
                case "ERROR":
                    levelPrefix = "[ERROR]";
                    break;
                case "WARN":
                    levelPrefix = "[WARN ]";
                    break;
                case "INFO":
                    levelPrefix = "[INFO ]";
                    break;
                case "DEBUG":
                    levelPrefix = "[DEBUG]";
                    break;
                case "TRACE":
                    levelPrefix = "[TRACE]";
                    break;
                default:
                    levelPrefix = "[" + level + "]";
            }
            
            logLine.append(levelPrefix).append(" ");
            
            // Logger (simplificado)
            String loggerName = entry.getLogger();
            if (loggerName.length() > 30) {
                loggerName = "..." + loggerName.substring(loggerName.length() - 27);
            }
            logLine.append(String.format("%-30s", loggerName)).append(" - ");
            
            // Mensagem
            logLine.append(entry.getMessage());
            
            // Exceção se houver
            if (entry.getException() != null && !entry.getException().isEmpty()) {
                logLine.append("\n  └─ ").append(entry.getException());
            }
            
            logLine.append("\n");
            
            appendToTerminal(logLine.toString());
        });
    }
    
    /**
     * Adiciona texto ao terminal e mantém limite de linhas.
     */
    private void appendToTerminal(String text) {
        if (terminalLogsArea == null) {
            return;
        }
        
        terminalLogsArea.appendText(text);
        
        // Limita número de linhas para não consumir muita memória
        String content = terminalLogsArea.getText();
        String[] lines = content.split("\n");
        
        if (lines.length > MAX_TERMINAL_LINES) {
            // Mantém apenas as últimas MAX_TERMINAL_LINES linhas
            StringBuilder sb = new StringBuilder();
            int startIndex = lines.length - MAX_TERMINAL_LINES;
            for (int i = startIndex; i < lines.length; i++) {
                sb.append(lines[i]);
                if (i < lines.length - 1) {
                    sb.append("\n");
                }
            }
            terminalLogsArea.setText(sb.toString());
        }
        
        // Auto-scroll para o final
        terminalLogsArea.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Limpa o terminal de logs.
     */
    @FXML
    private void clearTerminalLogs() {
        if (terminalLogsArea != null) {
            terminalLogsArea.clear();
            appendToTerminal("=== Terminal limpo ===\n");
            appendToTerminal("Aguardando novos logs...\n\n");
        }
    }
    
    /**
     * Handler para sair da aplicação.
     */
    @FXML
    private void handleExit() {
        logger.info("Saindo da aplicação");
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Saída");
        alert.setHeaderText("Sair do PlugWay ETL");
        alert.setContentText("Deseja realmente sair da aplicação?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }
    
    /**
     * Handler para executar todos os jobs configurados.
     */
    @FXML
    private void handleExecuteJob() {
        logger.info("Iniciando execução de todos os jobs configurados");
        
        // Garante que o terminal está visível
        showTerminal();
        
        // Limpa o terminal e exibe cabeçalho
        appendToTerminal("\n");
        appendToTerminal("========================================\n");
        appendToTerminal("  EXECUÇÃO DE JOBS ETL\n");
        appendToTerminal("========================================\n");
        appendToTerminal("Iniciando execução de jobs configurados...\n\n");
        
        // Executa em thread separada para não travar a UI
        new Thread(() -> {
            try {
                // Carrega todos os jobs salvos
                JobConfigService jobConfigService = JobConfigService.getInstance();
                List<EtlJob> allJobs = jobConfigService.loadJobs();
                
                // Filtra apenas jobs ativos
                List<EtlJob> activeJobs = allJobs.stream()
                    .filter(EtlJob::isEnabled)
                    .collect(Collectors.toList());
                
                if (activeJobs.isEmpty()) {
                    Platform.runLater(() -> {
                        appendToTerminal("Nenhum job ativo encontrado para execução.\n");
                        appendToTerminal("========================================\n\n");
                        updateStatus("Nenhum job ativo para executar");
                    });
                    return;
                }
                
                Platform.runLater(() -> {
                    appendToTerminal(String.format("Total de jobs ativos encontrados: %d\n\n", activeJobs.size()));
                });
                
                // Executa cada job
                final int[] successCount = {0};
                final int[] failureCount = {0};
                final Instant startTime = Instant.now();
                final int totalJobs = activeJobs.size();
                
                for (int i = 0; i < activeJobs.size(); i++) {
                    final EtlJob job = activeJobs.get(i);
                    final int jobIndex = i + 1;
                    
                    Platform.runLater(() -> {
                        appendToTerminal(String.format("--- Executando Job %d/%d: %s (ID: %s) ---\n", 
                            jobIndex, totalJobs, job.getName(), job.getId()));
                    });
                    
                    try {
                        // Valida o job antes de executar
                        if (!job.isValid()) {
                            Platform.runLater(() -> {
                                appendToTerminal("  ❌ Job inválido. Verifique as configurações.\n\n");
                            });
                            failureCount[0]++;
                            continue;
                        }
                        
                        // Executa o job
                        MessagingGateway gateway = new MessagingGateway();
                        MessagingGateway.ExecutionResult result = gateway.executeEtlJob(job);
                        
                        // Exibe resultado no terminal
                        final MessagingGateway.ExecutionResult finalResult = result;
                        Platform.runLater(() -> {
                            if (finalResult.isSuccess()) {
                                appendToTerminal(String.format("  ✅ Sucesso! Registros processados: %d | Duração: %s\n\n",
                                    finalResult.getRecordsProcessed(), finalResult.getFormattedDuration()));
                            } else {
                                appendToTerminal(String.format("  ❌ Falhou: %s\n\n",
                                    finalResult.getErrorMessage() != null ? finalResult.getErrorMessage() : "Erro desconhecido"));
                            }
                        });
                        
                        if (result.isSuccess()) {
                            successCount[0]++;
                        } else {
                            failureCount[0]++;
                        }
                        
                    } catch (Exception e) {
                        final String errorMessage = e.getMessage();
                        Platform.runLater(() -> {
                            appendToTerminal(String.format("  ❌ Erro ao executar: %s\n\n", errorMessage));
                        });
                        logger.error("Erro ao executar job: {}", job.getId(), e);
                        failureCount[0]++;
                    }
                }
                
                // Exibe resumo final
                final Instant endTime = Instant.now();
                final Duration totalDuration = Duration.between(startTime, endTime);
                final int finalSuccessCount = successCount[0];
                final int finalFailureCount = failureCount[0];
                
                Platform.runLater(() -> {
                    appendToTerminal("========================================\n");
                    appendToTerminal("  RESUMO DA EXECUÇÃO\n");
                    appendToTerminal("========================================\n");
                    appendToTerminal(String.format("Total de jobs executados: %d\n", totalJobs));
                    appendToTerminal(String.format("  ✅ Sucesso: %d\n", finalSuccessCount));
                    appendToTerminal(String.format("  ❌ Falhas: %d\n", finalFailureCount));
                    appendToTerminal(String.format("Duração total: %d segundos\n", totalDuration.getSeconds()));
                    appendToTerminal("========================================\n\n");
                    
                    updateStatus(String.format("Execução concluída: %d sucesso, %d falhas", finalSuccessCount, finalFailureCount));
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    appendToTerminal(String.format("❌ Erro ao carregar jobs: %s\n\n", e.getMessage()));
                    appendToTerminal("========================================\n\n");
                    updateStatus("Erro ao executar jobs");
                });
                logger.error("Erro ao executar jobs", e);
            }
        }).start();
    }
    
    /**
     * Handler para mostrar informações sobre a aplicação.
     */
    @FXML
    private void handleAbout() {
        logger.debug("Mostrando informações sobre a aplicação");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("PlugWay ETL");
        alert.setContentText(
            "PlugWay ETL - Sistema ETL com padrões EIP\n\n" +
            "Versão: 1.0.0-SNAPSHOT\n\n" +
            "Aplicação Desktop para integração entre bancos de dados e APIs REST.\n" +
            "Implementa padrões Enterprise Integration Patterns (EIP).\n\n" +
            "Desenvolvido com Java e JavaFX"
        );
        alert.showAndWait();
    }
}

