package com.plugway.etl.ui;

import com.plugway.etl.eip.ControlBus;
import com.plugway.etl.model.*;
import com.plugway.etl.service.ApiConfigService;
import com.plugway.etl.service.ConnectionConfigService;
import com.plugway.etl.service.JobConfigService;
import com.plugway.etl.service.orchestrator.MessagingGateway;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller para gerenciamento de jobs ETL.
 */
public class JobManagerController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(JobManagerController.class);
    
    @FXML
    private TableView<EtlJob> jobsTable;
    @FXML
    private TableColumn<EtlJob, String> nameColumn;
    @FXML
    private TableColumn<EtlJob, Boolean> enabledColumn;
    
    @FXML
    private TextField jobIdField;
    @FXML
    private TextField jobNameField;
    @FXML
    private TextArea jobDescriptionField;
    @FXML
    private CheckBox enabledCheckBox;
    
    @FXML
    private ComboBox<DatabaseConfig> sourceConnectionComboBox;
    @FXML
    private TextArea sqlQueryField;
    
    @FXML
    private ComboBox<ApiConfig> targetApiComboBox;
    
    @FXML
    private CheckBox normalizeDatesCheckBox;
    @FXML
    private CheckBox normalizeDecimalsCheckBox;
    @FXML
    private CheckBox normalizeColumnNamesCheckBox;
    @FXML
    private ComboBox<String> nullHandlingComboBox;
    @FXML
    private CheckBox prettyPrintCheckBox;
    
    @FXML
    private Button newJobButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button executeButton;
    
    private ObservableList<EtlJob> jobs;
    private ObservableList<DatabaseConfig> availableConnections;
    private ObservableList<ApiConfig> availableApis;
    private EtlJob selectedJob;
    private Stage stage;
    
    // Serviços de persistência
    private JobConfigService jobConfigService;
    private ConnectionConfigService connectionConfigService;
    private ApiConfigService apiConfigService;
    
    // Referências para dados compartilhados (serão injetados)
    private ObservableList<DatabaseConfig> sharedConnections;
    private ObservableList<ApiConfig> sharedApis;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando JobManagerController");
        
        // Inicializa serviços de persistência
        jobConfigService = JobConfigService.getInstance();
        connectionConfigService = ConnectionConfigService.getInstance();
        apiConfigService = ApiConfigService.getInstance();
        
        // Carrega conexões e APIs dos arquivos primeiro
        List<DatabaseConfig> savedConnections = connectionConfigService.loadConnections();
        availableConnections = FXCollections.observableArrayList(savedConnections);
        
        List<ApiConfig> savedApis = apiConfigService.loadApis();
        availableApis = FXCollections.observableArrayList(savedApis);
        
        // Carrega jobs salvos do arquivo
        List<EtlJob> savedJobs = jobConfigService.loadJobs();
        
        // Reconstrói referências de DatabaseConfig e ApiConfig nos jobs
        for (EtlJob job : savedJobs) {
            // Reconstrói referência de DatabaseConfig
            if (job.getSourceConfig() != null) {
                String sourceName = job.getSourceConfig().getName();
                DatabaseConfig matchedConnection = availableConnections.stream()
                    .filter(c -> c.getName().equals(sourceName))
                    .findFirst()
                    .orElse(null);
                if (matchedConnection != null) {
                    job.setSourceConfig(matchedConnection);
                } else {
                    logger.warn("Conexão '{}' referenciada no job '{}' não encontrada", 
                              sourceName, job.getId());
                }
            }
            
            // Reconstrói referência de ApiConfig
            if (job.getTargetConfig() != null) {
                String targetName = job.getTargetConfig().getName();
                ApiConfig matchedApi = availableApis.stream()
                    .filter(a -> a.getName().equals(targetName))
                    .findFirst()
                    .orElse(null);
                if (matchedApi != null) {
                    job.setTargetConfig(matchedApi);
                } else {
                    logger.warn("API '{}' referenciada no job '{}' não encontrada", 
                              targetName, job.getId());
                }
            }
        }
        
        jobs = FXCollections.observableArrayList(savedJobs);
        jobsTable.setItems(jobs);
        
        logger.info("Carregados {} jobs, {} conexões e {} APIs do arquivo", 
                   jobs.size(), availableConnections.size(), availableApis.size());
        
        // Garante que os campos estão editáveis
        if (jobIdField != null) {
            jobIdField.setEditable(true);
            jobIdField.setDisable(false);
        }
        if (jobNameField != null) {
            jobNameField.setEditable(true);
            jobNameField.setDisable(false);
        }
        if (jobDescriptionField != null) {
            jobDescriptionField.setEditable(true);
            jobDescriptionField.setDisable(false);
        }
        if (sqlQueryField != null) {
            sqlQueryField.setEditable(true);
            sqlQueryField.setDisable(false);
        }
        
        // Configura colunas da tabela
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        enabledColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isEnabled()));
        
        // Configura ComboBoxes
        sourceConnectionComboBox.setItems(availableConnections);
        sourceConnectionComboBox.setCellFactory(param -> new ListCell<DatabaseConfig>() {
            @Override
            protected void updateItem(DatabaseConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getType().getDisplayName() + ")");
                }
            }
        });
        
        targetApiComboBox.setItems(availableApis);
        targetApiComboBox.setCellFactory(param -> new ListCell<ApiConfig>() {
            @Override
            protected void updateItem(ApiConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getMethod() + ")");
                }
            }
        });
        
        nullHandlingComboBox.setItems(FXCollections.observableArrayList("keep", "exclude", "replace"));
        nullHandlingComboBox.setValue("keep");
        
        // Valores padrão
        normalizeDatesCheckBox.setSelected(true);
        normalizeDecimalsCheckBox.setSelected(true);
        normalizeColumnNamesCheckBox.setSelected(true);
        prettyPrintCheckBox.setSelected(false);
        
        // Seleção na tabela
        jobsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedJob = newValue;
                if (newValue != null) {
                    loadJobToForm(newValue);
                    deleteButton.setDisable(false);
                    executeButton.setDisable(false);
                } else {
                    clearForm();
                    deleteButton.setDisable(true);
                    executeButton.setDisable(true);
                }
            });
        
        deleteButton.setDisable(true);
        executeButton.setDisable(true);
        
        // Adiciona ícones aos botões
        if (newJobButton != null) {
            IconHelper.setIconWithText(newJobButton, FontAwesomeSolid.PLUS_CIRCLE, "Novo Job");
        }
        IconHelper.setIconWithText(saveButton, FontAwesomeSolid.SAVE, "Salvar");
        IconHelper.setIconWithText(deleteButton, FontAwesomeSolid.TRASH_ALT, "Excluir");
        IconHelper.setIconWithText(executeButton, FontAwesomeSolid.PLAY_CIRCLE, "Executar Job");
    }
    
    @FXML
    private void handleSave() {
        try {
            EtlJob job = createJobFromForm();
            
            if (!job.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, preencha todos os campos obrigatórios:\n" +
                    "- ID e Nome do Job\n" +
                    "- Conexão de origem\n" +
                    "- Query SQL\n" +
                    "- API de destino");
                return;
            }
            
            // Verifica se já existe (atualização) ou é novo
            int index = jobs.indexOf(selectedJob);
            if (index >= 0) {
                jobs.set(index, job);
            } else {
                jobs.add(job);
            }
            
            // Salva no arquivo JSON
            boolean saved = jobConfigService.saveJob(job, new ArrayList<>(jobs));
            if (!saved) {
                logger.warn("Job salvo na memória, mas houve erro ao salvar no arquivo");
                showAlert(Alert.AlertType.WARNING, "Aviso", 
                    "Job salvo, mas houve problema ao salvar no arquivo. Verifique os logs.");
                return;
            }
            
            clearForm();
            jobsTable.getSelectionModel().clearSelection();
            
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", 
                "Job ETL salvo com sucesso!");
            
        } catch (Exception e) {
            logger.error("Erro ao salvar job", e);
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Erro ao salvar job: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedJob == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Job ETL");
        alert.setContentText("Deseja realmente excluir o job \"" + 
            selectedJob.getName() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String jobId = selectedJob.getId();
                jobs.remove(selectedJob);
                
                // Salva no arquivo JSON
                boolean saved = jobConfigService.deleteJob(jobId, new ArrayList<>(jobs));
                if (!saved) {
                    logger.warn("Job removido da memória, mas houve erro ao salvar no arquivo");
                }
                
                clearForm();
                jobsTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    @FXML
    private void handleExecute() {
        if (selectedJob == null) {
            return;
        }
        
        if (!selectedJob.isValid()) {
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Job não está configurado corretamente. Verifique as configurações.");
            return;
        }
        
        executeButton.setDisable(true);
        executeButton.setText("Executando...");
        
        // Executa job em thread separada
        new Thread(() -> {
            try {
                MessagingGateway gateway = new MessagingGateway();
                MessagingGateway.ExecutionResult result = gateway.executeEtlJob(selectedJob);
                
                javafx.application.Platform.runLater(() -> {
                    executeButton.setDisable(false);
                    executeButton.setText("Executar Job");
                    
                    if (result.isSuccess()) {
                        showAlert(Alert.AlertType.INFORMATION, "Execução Concluída", 
                            "Job executado com sucesso!\n\n" +
                            "Registros processados: " + result.getRecordsProcessed() + "\n" +
                            "Duração: " + result.getFormattedDuration());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Execução Falhou", 
                            "Erro ao executar job:\n" + result.getErrorMessage());
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    executeButton.setDisable(false);
                    executeButton.setText("Executar Job");
                    showAlert(Alert.AlertType.ERROR, "Erro", 
                        "Erro ao executar job: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleNew() {
        clearForm();
        jobsTable.getSelectionModel().clearSelection();
        
        // Garante que os campos estão habilitados
        enableFormFields(true);
        
        // Gera ID automático
        jobIdField.setText("job-" + System.currentTimeMillis());
        
        // Foca no campo Nome para facilitar a entrada
        jobNameField.requestFocus();
    }
    
    /**
     * Habilita ou desabilita os campos do formulário.
     */
    private void enableFormFields(boolean enable) {
        if (jobIdField != null) {
            jobIdField.setEditable(enable);
            jobIdField.setDisable(!enable);
        }
        if (jobNameField != null) {
            jobNameField.setEditable(enable);
            jobNameField.setDisable(!enable);
        }
        if (jobDescriptionField != null) {
            jobDescriptionField.setEditable(enable);
            jobDescriptionField.setDisable(!enable);
        }
        if (sqlQueryField != null) {
            sqlQueryField.setEditable(enable);
            sqlQueryField.setDisable(!enable);
        }
        if (enabledCheckBox != null) {
            enabledCheckBox.setDisable(!enable);
        }
        if (sourceConnectionComboBox != null) {
            sourceConnectionComboBox.setDisable(!enable);
        }
        if (targetApiComboBox != null) {
            targetApiComboBox.setDisable(!enable);
        }
        if (normalizeDatesCheckBox != null) {
            normalizeDatesCheckBox.setDisable(!enable);
        }
        if (normalizeDecimalsCheckBox != null) {
            normalizeDecimalsCheckBox.setDisable(!enable);
        }
        if (normalizeColumnNamesCheckBox != null) {
            normalizeColumnNamesCheckBox.setDisable(!enable);
        }
        if (nullHandlingComboBox != null) {
            nullHandlingComboBox.setDisable(!enable);
        }
        if (prettyPrintCheckBox != null) {
            prettyPrintCheckBox.setDisable(!enable);
        }
    }
    
    private EtlJob createJobFromForm() {
        EtlJob job = new EtlJob();
        job.setId(jobIdField.getText());
        job.setName(jobNameField.getText());
        job.setDescription(jobDescriptionField.getText());
        job.setEnabled(enabledCheckBox.isSelected());
        
        job.setSourceConfig(sourceConnectionComboBox.getValue());
        job.setSqlQuery(sqlQueryField.getText());
        job.setTargetConfig(targetApiComboBox.getValue());
        
        // Configurações de transformação
        Map<String, Object> transformations = new HashMap<>();
        transformations.put("normalizeDates", normalizeDatesCheckBox.isSelected());
        transformations.put("normalizeDecimals", normalizeDecimalsCheckBox.isSelected());
        transformations.put("normalizeColumnNames", normalizeColumnNamesCheckBox.isSelected());
        transformations.put("nullHandling", nullHandlingComboBox.getValue());
        transformations.put("prettyPrint", prettyPrintCheckBox.isSelected());
        job.setTransformations(transformations);
        
        return job;
    }
    
    private void loadJobToForm(EtlJob job) {
        jobIdField.setText(job.getId());
        jobNameField.setText(job.getName());
        jobDescriptionField.setText(job.getDescription());
        enabledCheckBox.setSelected(job.isEnabled());
        
        sourceConnectionComboBox.setValue(job.getSourceConfig());
        sqlQueryField.setText(job.getSqlQuery());
        targetApiComboBox.setValue(job.getTargetConfig());
        
        // Carrega configurações de transformação
        Map<String, Object> transformations = job.getTransformations();
        if (transformations != null) {
            normalizeDatesCheckBox.setSelected(getBoolean(transformations, "normalizeDates", true));
            normalizeDecimalsCheckBox.setSelected(getBoolean(transformations, "normalizeDecimals", true));
            normalizeColumnNamesCheckBox.setSelected(getBoolean(transformations, "normalizeColumnNames", true));
            nullHandlingComboBox.setValue(getString(transformations, "nullHandling", "keep"));
            prettyPrintCheckBox.setSelected(getBoolean(transformations, "prettyPrint", false));
        }
    }
    
    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private void clearForm() {
        jobIdField.clear();
        jobNameField.clear();
        jobDescriptionField.clear();
        enabledCheckBox.setSelected(true);
        sourceConnectionComboBox.setValue(null);
        sqlQueryField.clear();
        targetApiComboBox.setValue(null);
        normalizeDatesCheckBox.setSelected(true);
        normalizeDecimalsCheckBox.setSelected(true);
        normalizeColumnNamesCheckBox.setSelected(true);
        nullHandlingComboBox.setValue("keep");
        prettyPrintCheckBox.setSelected(false);
        selectedJob = null;
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
    
    public void setSharedConnections(ObservableList<DatabaseConfig> connections) {
        this.sharedConnections = connections;
        availableConnections.setAll(connections);
    }
    
    public void setSharedApis(ObservableList<ApiConfig> apis) {
        this.sharedApis = apis;
        availableApis.setAll(apis);
    }
    
    public ObservableList<EtlJob> getJobs() {
        return jobs;
    }
}

