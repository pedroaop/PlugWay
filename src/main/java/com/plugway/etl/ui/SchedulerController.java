package com.plugway.etl.ui;

import com.plugway.etl.model.EtlJob;
import com.plugway.etl.model.ScheduleConfig;
import com.plugway.etl.service.scheduler.SchedulerService;
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
import java.util.ResourceBundle;

/**
 * Controller para gerenciamento de agendamentos de jobs.
 */
public class SchedulerController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(SchedulerController.class);
    
    @FXML
    private TableView<ScheduledJobInfo> scheduledJobsTable;
    @FXML
    private TableColumn<ScheduledJobInfo, String> jobIdColumn;
    @FXML
    private TableColumn<ScheduledJobInfo, String> scheduleColumn;
    @FXML
    private TableColumn<ScheduledJobInfo, String> nextExecutionColumn;
    @FXML
    private TableColumn<ScheduledJobInfo, String> statusColumn;
    
    @FXML
    private ComboBox<EtlJob> jobComboBox;
    @FXML
    private CheckBox scheduleEnabledCheckBox;
    @FXML
    private RadioButton cronRadioButton;
    @FXML
    private RadioButton intervalRadioButton;
    @FXML
    private TextField cronExpressionField;
    @FXML
    private TextField intervalSecondsField;
    @FXML
    private TextField timezoneField;
    
    @FXML
    private Button scheduleButton;
    @FXML
    private Button unscheduleButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    
    private ObservableList<ScheduledJobInfo> scheduledJobs;
    private ObservableList<EtlJob> availableJobs;
    private SchedulerService schedulerService;
    private com.plugway.etl.service.JobConfigService jobConfigService;
    private Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando SchedulerController");
        
        schedulerService = new SchedulerService();
        jobConfigService = com.plugway.etl.service.JobConfigService.getInstance();
        
        // Inicializa listas
        scheduledJobs = FXCollections.observableArrayList();
        availableJobs = FXCollections.observableArrayList();
        
        scheduledJobsTable.setItems(scheduledJobs);
        jobComboBox.setItems(availableJobs);
        
        // Configura como exibir o job no ComboBox
        jobComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<EtlJob>() {
            @Override
            protected void updateItem(EtlJob job, boolean empty) {
                super.updateItem(job, empty);
                if (empty || job == null) {
                    setText(null);
                } else {
                    setText(job.getName() + " (" + job.getId() + ")");
                }
            }
        });
        jobComboBox.setButtonCell(new javafx.scene.control.ListCell<EtlJob>() {
            @Override
            protected void updateItem(EtlJob job, boolean empty) {
                super.updateItem(job, empty);
                if (empty || job == null) {
                    setText(null);
                } else {
                    setText(job.getName() + " (" + job.getId() + ")");
                }
            }
        });
        
        // Configura colunas da tabela
        jobIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJobId()));
        scheduleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleInfo()));
        nextExecutionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNextExecution()));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Configura ToggleGroup para radio buttons
        ToggleGroup scheduleTypeGroup = new ToggleGroup();
        cronRadioButton.setToggleGroup(scheduleTypeGroup);
        intervalRadioButton.setToggleGroup(scheduleTypeGroup);
        cronRadioButton.setSelected(true);
        
        // Atualiza campos baseado no tipo selecionado
        cronRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            cronExpressionField.setDisable(!newValue);
            intervalSecondsField.setDisable(newValue);
        });
        intervalRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            cronExpressionField.setDisable(newValue);
            intervalSecondsField.setDisable(!newValue);
        });
        
        // Valores padrão
        timezoneField.setText("America/Sao_Paulo");
        intervalSecondsField.setText("3600"); // 1 hora
        
        // Seleção na tabela
        scheduledJobsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadScheduleToForm(newValue);
                    unscheduleButton.setDisable(false);
                    pauseButton.setDisable(false);
                    resumeButton.setDisable(false);
                } else {
                    clearForm();
                    unscheduleButton.setDisable(true);
                    pauseButton.setDisable(true);
                    resumeButton.setDisable(true);
                }
            });
        
        unscheduleButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        
        // Adiciona ícones aos botões
        IconHelper.setIconWithText(scheduleButton, FontAwesomeSolid.CLOCK, "Agendar");
        IconHelper.setIconWithText(unscheduleButton, FontAwesomeSolid.TIMES_CIRCLE, "Remover");
        IconHelper.setIconWithText(pauseButton, FontAwesomeSolid.PAUSE_CIRCLE, "Pausar");
        IconHelper.setIconWithText(resumeButton, FontAwesomeSolid.PLAY_CIRCLE, "Retomar");
        
        // Carrega jobs disponíveis
        loadAvailableJobs();
        
        // Recria agendamentos dos jobs que têm schedule habilitado
        restoreScheduledJobs();
        
        // Atualiza lista de jobs agendados
        refreshScheduledJobs();
        
        // Atualiza lista periodicamente
        startRefreshTimer();
    }
    
    /**
     * Restaura os agendamentos dos jobs que têm schedule configurado e habilitado.
     */
    private void restoreScheduledJobs() {
        try {
            if (jobConfigService == null) {
                jobConfigService = com.plugway.etl.service.JobConfigService.getInstance();
            }
            
            java.util.List<EtlJob> jobs = jobConfigService.loadJobs();
            logger.info("Verificando {} jobs para restaurar agendamentos", jobs.size());
            
            int restored = 0;
            
            for (EtlJob job : jobs) {
                logger.info("Verificando job: {} | Schedule: {}", job.getId(), 
                           job.getSchedule() != null ? "presente" : "ausente");
                
                if (job.getSchedule() != null) {
                    logger.info("Job {} - Schedule enabled: {} | Valid: {} | Cron: {} | Interval: {}", 
                               job.getId(), 
                               job.getSchedule().isEnabled(),
                               job.getSchedule().isValid(),
                               job.getSchedule().getCronExpression(),
                               job.getSchedule().getIntervalSeconds());
                    
                    if (job.getSchedule().isEnabled() && job.getSchedule().isValid()) {
                        logger.info("Tentando restaurar agendamento para job: {}", job.getId());
                        if (schedulerService.scheduleJob(job)) {
                            restored++;
                            logger.info("Agendamento restaurado para job: {}", job.getId());
                        } else {
                            logger.warn("Falha ao restaurar agendamento para job: {}", job.getId());
                        }
                    } else {
                        logger.info("Job {} não será restaurado - enabled: {} | valid: {}", 
                                   job.getId(), 
                                   job.getSchedule().isEnabled(),
                                   job.getSchedule().isValid());
                    }
                }
            }
            
            if (restored > 0) {
                logger.info("Restaurados {} agendamentos de jobs", restored);
            } else {
                logger.info("Nenhum agendamento para restaurar");
            }
        } catch (Exception e) {
            logger.error("Erro ao restaurar agendamentos", e);
        }
    }
    
    private void loadAvailableJobs() {
        try {
            if (jobConfigService == null) {
                jobConfigService = com.plugway.etl.service.JobConfigService.getInstance();
            }
            availableJobs.setAll(jobConfigService.loadJobs());
        } catch (Exception e) {
            logger.error("Erro ao carregar jobs disponíveis", e);
        }
    }
    
    @FXML
    private void handleSchedule() {
        try {
            EtlJob selectedJob = jobComboBox.getValue();
            if (selectedJob == null) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Selecione um job para agendar.");
                return;
            }
            
            ScheduleConfig scheduleConfig = createScheduleFromForm();
            
            if (!scheduleConfig.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, configure o agendamento corretamente.");
                return;
            }
            
            selectedJob.setSchedule(scheduleConfig);
            
            if (schedulerService.scheduleJob(selectedJob)) {
                // Salva o job com o schedule no arquivo JSON
                java.util.List<EtlJob> allJobs = jobConfigService.loadJobs();
                jobConfigService.saveJob(selectedJob, allJobs);
                
                // Atualiza a lista de jobs disponíveis
                loadAvailableJobs();
                refreshScheduledJobs();
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", 
                    "Job agendado com sucesso!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Erro ao agendar job. Verifique a configuração.");
            }
            
        } catch (Exception e) {
            logger.error("Erro ao agendar job", e);
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Erro ao agendar job: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUnschedule() {
        ScheduledJobInfo selected = scheduledJobsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Agendamento");
        alert.setContentText("Deseja realmente remover o agendamento do job \"" + 
            selected.getJobId() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (schedulerService.unscheduleJob(selected.getJobId())) {
                    // Remove o schedule do job e salva no arquivo JSON
                    java.util.List<EtlJob> allJobs = jobConfigService.loadJobs();
                    for (EtlJob job : allJobs) {
                        if (job.getId().equals(selected.getJobId())) {
                            job.setSchedule(null);
                            break;
                        }
                    }
                    jobConfigService.saveJobs(allJobs);
                    
                    // Atualiza a lista de jobs disponíveis
                    loadAvailableJobs();
                    refreshScheduledJobs();
                    clearForm();
                }
            }
        });
    }
    
    @FXML
    private void handlePause() {
        ScheduledJobInfo selected = scheduledJobsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            schedulerService.pauseJob(selected.getJobId());
            refreshScheduledJobs();
        }
    }
    
    @FXML
    private void handleResume() {
        ScheduledJobInfo selected = scheduledJobsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            schedulerService.resumeJob(selected.getJobId());
            refreshScheduledJobs();
        }
    }
    
    @FXML
    private void handleRefresh() {
        refreshScheduledJobs();
    }
    
    private ScheduleConfig createScheduleFromForm() {
        ScheduleConfig config = new ScheduleConfig();
        config.setEnabled(scheduleEnabledCheckBox.isSelected());
        config.setTimezone(timezoneField.getText());
        
        if (cronRadioButton.isSelected()) {
            config.setCronExpression(cronExpressionField.getText());
        } else {
            try {
                config.setIntervalSeconds(Integer.parseInt(intervalSecondsField.getText()));
            } catch (NumberFormatException e) {
                config.setIntervalSeconds(0);
            }
        }
        
        return config;
    }
    
    private void loadScheduleToForm(ScheduledJobInfo info) {
        // Carrega informações do job agendado
        // TODO: Implementar carregamento completo
    }
    
    private void clearForm() {
        jobComboBox.setValue(null);
        scheduleEnabledCheckBox.setSelected(true);
        cronRadioButton.setSelected(true);
        cronExpressionField.clear();
        intervalSecondsField.setText("3600");
        timezoneField.setText("America/Sao_Paulo");
        scheduledJobsTable.getSelectionModel().clearSelection();
    }
    
    public void refreshScheduledJobs() {
        scheduledJobs.clear();
        
        for (EtlJob job : schedulerService.getScheduledJobs().values()) {
            ScheduledJobInfo info = new ScheduledJobInfo();
            info.setJobId(job.getId());
            info.setScheduleInfo(getScheduleInfo(job));
            info.setNextExecution(schedulerService.getNextFireTime(job.getId()));
            info.setStatus(schedulerService.isScheduled(job.getId()) ? "Agendado" : "Pausado");
            scheduledJobs.add(info);
        }
    }
    
    private String getScheduleInfo(EtlJob job) {
        ScheduleConfig schedule = job.getSchedule();
        if (schedule == null) {
            return "N/A";
        }
        
        if (schedule.getCronExpression() != null && !schedule.getCronExpression().isEmpty()) {
            return "Cron: " + schedule.getCronExpression();
        } else if (schedule.getIntervalSeconds() > 0) {
            return "Intervalo: " + schedule.getIntervalSeconds() + "s";
        }
        
        return "N/A";
    }
    
    private void startRefreshTimer() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> refreshScheduledJobs())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setAvailableJobs(ObservableList<EtlJob> jobs) {
        this.availableJobs.setAll(jobs);
    }
    
    /**
     * Classe para informações de job agendado.
     */
    public static class ScheduledJobInfo {
        private String jobId;
        private String scheduleInfo;
        private String nextExecution;
        private String status;
        
        // Getters e Setters
        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        
        public String getScheduleInfo() { return scheduleInfo; }
        public void setScheduleInfo(String scheduleInfo) { this.scheduleInfo = scheduleInfo; }
        
        public String getNextExecution() { return nextExecution; }
        public void setNextExecution(String nextExecution) { this.nextExecution = nextExecution; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}

