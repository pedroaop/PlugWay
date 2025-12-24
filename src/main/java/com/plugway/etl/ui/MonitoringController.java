package com.plugway.etl.ui;

import com.plugway.etl.model.JobStatus;
import com.plugway.etl.util.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.slf4j.Logger;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller para monitoramento de execuções de jobs.
 */
public class MonitoringController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(MonitoringController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @FXML
    private TableView<ExecutionRecord> executionsTable;
    @FXML
    private TableColumn<ExecutionRecord, String> jobIdColumn;
    @FXML
    private TableColumn<ExecutionRecord, String> statusColumn;
    @FXML
    private TableColumn<ExecutionRecord, String> startTimeColumn;
    @FXML
    private TableColumn<ExecutionRecord, String> durationColumn;
    @FXML
    private TableColumn<ExecutionRecord, Integer> recordsColumn;
    
    @FXML
    private TextArea detailsArea;
    
    @FXML
    private Label totalExecutionsLabel;
    @FXML
    private Label successCountLabel;
    @FXML
    private Label failedCountLabel;
    @FXML
    private Label runningCountLabel;
    
    @FXML
    private Button refreshButton;
    @FXML
    private Button clearButton;
    
    private ObservableList<ExecutionRecord> executions;
    private Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando MonitoringController");
        
        // Inicializa lista de execuções
        executions = FXCollections.observableArrayList();
        executionsTable.setItems(executions);
        
        // Configura colunas da tabela
        jobIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJobId()));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null ? 
                cellData.getValue().getStatus().toString() : ""));
        startTimeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedStartTime()));
        durationColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedDuration()));
        recordsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getRecordsProcessed()).asObject());
        
        // Formatação de status com cores
        statusColumn.setCellFactory(column -> new TableCell<ExecutionRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    ExecutionRecord record = getTableView().getItems().get(getIndex());
                    if (record != null && record.getStatus() != null) {
                        switch (record.getStatus()) {
                            case SUCCESS:
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                break;
                            case FAILED:
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            case RUNNING:
                                setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            }
        });
        
        // Seleção na tabela
        executionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showExecutionDetails(newValue);
                } else {
                    detailsArea.clear();
                }
            });
        
        // Adiciona ícones aos botões
        if (refreshButton != null) {
            IconHelper.setIconWithText(refreshButton, FontAwesomeSolid.SYNC_ALT, "Atualizar");
        }
        if (clearButton != null) {
            IconHelper.setIconWithText(clearButton, FontAwesomeSolid.BROOM, "Limpar");
        }
        
        updateStatistics();
    }
    
    /**
     * Adiciona um registro de execução.
     */
    public void addExecution(ExecutionRecord record) {
        executions.add(0, record); // Adiciona no início
        updateStatistics();
    }
    
    /**
     * Atualiza um registro de execução existente.
     */
    public void updateExecution(ExecutionRecord record) {
        int index = executions.indexOf(record);
        if (index >= 0) {
            executions.set(index, record);
            updateStatistics();
        }
    }
    
    private void showExecutionDetails(ExecutionRecord record) {
        StringBuilder details = new StringBuilder();
        details.append("Job ID: ").append(record.getJobId()).append("\n");
        details.append("Status: ").append(record.getStatus()).append("\n");
        details.append("Início: ").append(record.getFormattedStartTime()).append("\n");
        
        if (record.getEndTime() != null) {
            details.append("Fim: ").append(record.getFormattedEndTime()).append("\n");
        }
        
        details.append("Duração: ").append(record.getFormattedDuration()).append("\n");
        details.append("Registros processados: ").append(record.getRecordsProcessed()).append("\n");
        
        if (record.getErrorMessage() != null && !record.getErrorMessage().isEmpty()) {
            details.append("\nErro:\n").append(record.getErrorMessage());
        }
        
        detailsArea.setText(details.toString());
    }
    
    private void updateStatistics() {
        int total = executions.size();
        long success = executions.stream()
            .filter(e -> e.getStatus() == JobStatus.SUCCESS)
            .count();
        long failed = executions.stream()
            .filter(e -> e.getStatus() == JobStatus.FAILED)
            .count();
        long running = executions.stream()
            .filter(e -> e.getStatus() == JobStatus.RUNNING)
            .count();
        
        totalExecutionsLabel.setText(String.valueOf(total));
        successCountLabel.setText(String.valueOf(success));
        failedCountLabel.setText(String.valueOf(failed));
        runningCountLabel.setText(String.valueOf(running));
    }
    
    @FXML
    private void handleRefresh() {
        updateStatistics();
    }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Limpeza");
        alert.setHeaderText("Limpar Histórico");
        alert.setContentText("Deseja realmente limpar todo o histórico de execuções?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                executions.clear();
                detailsArea.clear();
                updateStatistics();
            }
        });
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Classe para representar um registro de execução.
     */
    public static class ExecutionRecord {
        private String jobId;
        private JobStatus status;
        private java.time.Instant startTime;
        private java.time.Instant endTime;
        private long durationMillis;
        private int recordsProcessed;
        private String errorMessage;
        
        public ExecutionRecord(String jobId) {
            this.jobId = jobId;
            this.status = JobStatus.PENDING;
            this.startTime = java.time.Instant.now();
            this.recordsProcessed = 0;
        }
        
        public String getFormattedStartTime() {
            return startTime != null ? 
                DATE_TIME_FORMATTER.format(java.time.LocalDateTime.ofInstant(startTime, java.time.ZoneId.systemDefault())) : 
                "N/A";
        }
        
        public String getFormattedEndTime() {
            return endTime != null ? 
                DATE_TIME_FORMATTER.format(java.time.LocalDateTime.ofInstant(endTime, java.time.ZoneId.systemDefault())) : 
                "N/A";
        }
        
        public String getFormattedDuration() {
            if (durationMillis < 1000) {
                return durationMillis + "ms";
            } else if (durationMillis < 60000) {
                return (durationMillis / 1000) + "s";
            } else {
                long minutes = durationMillis / 60000;
                long seconds = (durationMillis % 60000) / 1000;
                return minutes + "m " + seconds + "s";
            }
        }
        
        // Getters e Setters
        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        
        public java.time.Instant getStartTime() { return startTime; }
        public void setStartTime(java.time.Instant startTime) { this.startTime = startTime; }
        
        public java.time.Instant getEndTime() { return endTime; }
        public void setEndTime(java.time.Instant endTime) { this.endTime = endTime; }
        
        public long getDurationMillis() { return durationMillis; }
        public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }
        
        public int getRecordsProcessed() { return recordsProcessed; }
        public void setRecordsProcessed(int recordsProcessed) { this.recordsProcessed = recordsProcessed; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ExecutionRecord that = (ExecutionRecord) obj;
            return jobId != null ? jobId.equals(that.jobId) : that.jobId == null;
        }
        
        @Override
        public int hashCode() {
            return jobId != null ? jobId.hashCode() : 0;
        }
    }
}

