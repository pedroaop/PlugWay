package com.plugway.etl.ui;

import com.plugway.etl.service.monitoring.LogAppender;
import com.plugway.etl.util.LoggerUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller para visualização de logs em tempo real.
 */
public class LogsController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(LogsController.class);
    
    @FXML
    private TableView<LogEntry> logsTable;
    @FXML
    private TableColumn<LogEntry, String> timestampColumn;
    @FXML
    private TableColumn<LogEntry, String> levelColumn;
    @FXML
    private TableColumn<LogEntry, String> loggerColumn;
    @FXML
    private TableColumn<LogEntry, String> messageColumn;
    
    @FXML
    private TextArea detailsArea;
    
    @FXML
    private ComboBox<String> levelFilter;
    @FXML
    private TextField loggerFilter;
    @FXML
    private TextField messageFilter;
    
    @FXML
    private CheckBox autoScrollCheckBox;
    @FXML
    private Button clearButton;
    @FXML
    private Button exportButton;
    
    private ObservableList<LogEntry> allLogs;
    private ObservableList<LogEntry> filteredLogs;
    private LogAppender logAppender;
    private Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando LogsController");
        
        // Inicializa listas
        allLogs = FXCollections.observableArrayList();
        filteredLogs = FXCollections.observableArrayList();
        logsTable.setItems(filteredLogs);
        
        // Configura colunas
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        loggerColumn.setCellValueFactory(new PropertyValueFactory<>("logger"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        
        // Formatação de nível com cores
        levelColumn.setCellFactory(column -> new TableCell<LogEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    LogEntry entry = getTableView().getItems().get(getIndex());
                    if (entry != null) {
                        switch (entry.getLevel()) {
                            case "ERROR":
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            case "WARN":
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                                break;
                            case "INFO":
                                setStyle("-fx-text-fill: #3498db;");
                                break;
                            case "DEBUG":
                                setStyle("-fx-text-fill: #95a5a6;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            }
        });
        
        // Configura filtros
        levelFilter.setItems(FXCollections.observableArrayList("ALL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"));
        levelFilter.setValue("ALL");
        levelFilter.setOnAction(e -> applyFilters());
        
        loggerFilter.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        messageFilter.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        
        // Auto-scroll
        autoScrollCheckBox.setSelected(true);
        
        // Seleção na tabela
        logsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showLogDetails(newValue);
                } else {
                    detailsArea.clear();
                }
            });
        
        // Conecta ao LogAppender
        connectToLogAppender();
        
        // Adiciona ícones aos botões
        IconHelper.setIconWithText(clearButton, FontAwesomeSolid.BROOM, "Limpar");
        IconHelper.setIconWithText(exportButton, FontAwesomeSolid.DOWNLOAD, "Exportar");
    }
    
    /**
     * Conecta ao LogAppender para receber logs em tempo real.
     */
    private void connectToLogAppender() {
        logAppender = LogAppender.getInstance();
        logAppender.addLogListener(this::addLogEntry);
        logger.info("Conectado ao LogAppender");
    }
    
    /**
     * Adiciona uma entrada de log à lista.
     */
    public void addLogEntry(LogEntry entry) {
        Platform.runLater(() -> {
            allLogs.add(entry);
            
            // Limita tamanho da lista (mantém últimos 10000 logs)
            if (allLogs.size() > 10000) {
                allLogs.remove(0, allLogs.size() - 10000);
            }
            
            applyFilters();
            
            // Auto-scroll
            if (autoScrollCheckBox.isSelected() && filteredLogs.size() > 0) {
                logsTable.scrollTo(filteredLogs.size() - 1);
            }
        });
    }
    
    /**
     * Aplica filtros à lista de logs.
     */
    private void applyFilters() {
        filteredLogs.clear();
        
        String levelFilterValue = levelFilter.getValue();
        String loggerFilterValue = loggerFilter.getText().toLowerCase();
        String messageFilterValue = messageFilter.getText().toLowerCase();
        
        for (LogEntry entry : allLogs) {
            // Filtro de nível
            if (!"ALL".equals(levelFilterValue) && !entry.getLevel().equals(levelFilterValue)) {
                continue;
            }
            
            // Filtro de logger
            if (!loggerFilterValue.isEmpty() && 
                !entry.getLogger().toLowerCase().contains(loggerFilterValue)) {
                continue;
            }
            
            // Filtro de mensagem
            if (!messageFilterValue.isEmpty() && 
                !entry.getMessage().toLowerCase().contains(messageFilterValue)) {
                continue;
            }
            
            filteredLogs.add(entry);
        }
    }
    
    /**
     * Mostra detalhes de uma entrada de log.
     */
    private void showLogDetails(LogEntry entry) {
        StringBuilder details = new StringBuilder();
        details.append("Timestamp: ").append(entry.getTimestamp()).append("\n");
        details.append("Level: ").append(entry.getLevel()).append("\n");
        details.append("Logger: ").append(entry.getLogger()).append("\n");
        details.append("Thread: ").append(entry.getThread()).append("\n");
        details.append("\nMensagem:\n").append(entry.getMessage()).append("\n");
        
        if (entry.getException() != null && !entry.getException().isEmpty()) {
            details.append("\nExceção:\n").append(entry.getException());
        }
        
        detailsArea.setText(details.toString());
    }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Limpeza");
        alert.setHeaderText("Limpar Logs");
        alert.setContentText("Deseja realmente limpar todos os logs exibidos?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                allLogs.clear();
                filteredLogs.clear();
                detailsArea.clear();
            }
        });
    }
    
    @FXML
    private void handleExport() {
        // TODO: Implementar exportação de logs
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Logs");
        alert.setHeaderText("Funcionalidade em Desenvolvimento");
        alert.setContentText("A exportação de logs será implementada em breve.");
        alert.showAndWait();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Classe para representar uma entrada de log.
     */
    public static class LogEntry {
        private String timestamp;
        private String level;
        private String logger;
        private String thread;
        private String message;
        private String exception;
        
        public LogEntry(String timestamp, String level, String logger, String thread, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.logger = logger;
            this.thread = thread;
            this.message = message;
        }
        
        // Getters e Setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public String getLogger() { return logger; }
        public void setLogger(String logger) { this.logger = logger; }
        
        public String getThread() { return thread; }
        public void setThread(String thread) { this.thread = thread; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getException() { return exception; }
        public void setException(String exception) { this.exception = exception; }
    }
}

