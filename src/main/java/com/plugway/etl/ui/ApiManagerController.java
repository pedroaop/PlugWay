package com.plugway.etl.ui;

import com.plugway.etl.model.ApiConfig;
import com.plugway.etl.model.AuthType;
import com.plugway.etl.service.ApiConfigService;
import com.plugway.etl.service.load.LoadService;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller para gerenciamento de configurações de API REST.
 */
public class ApiManagerController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(ApiManagerController.class);
    
    @FXML
    private TableView<ApiConfig> apisTable;
    @FXML
    private TableColumn<ApiConfig, String> nameColumn;
    @FXML
    private TableColumn<ApiConfig, String> urlColumn;
    @FXML
    private TableColumn<ApiConfig, String> methodColumn;
    
    @FXML
    private TextField nameField;
    @FXML
    private TextField baseUrlField;
    @FXML
    private TextField endpointField;
    @FXML
    private ComboBox<String> methodComboBox;
    @FXML
    private ComboBox<AuthType> authTypeComboBox;
    
    @FXML
    private TextField authTokenField;
    @FXML
    private TextField apiKeyField;
    @FXML
    private TextField apiKeyHeaderField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextField timeoutField;
    @FXML
    private TextField maxRetriesField;
    
    @FXML
    private Button newApiButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button testButton;
    
    private ObservableList<ApiConfig> apis;
    private ApiConfig selectedApi;
    private Stage stage;
    private ApiConfigService configService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando ApiManagerController");
        
        // Inicializa serviço de persistência
        configService = ApiConfigService.getInstance();
        
        // Carrega APIs salvas do arquivo
        List<ApiConfig> savedApis = configService.loadApis();
        apis = FXCollections.observableArrayList(savedApis);
        apisTable.setItems(apis);
        
        logger.info("Carregadas {} APIs do arquivo", apis.size());
        
        // Configura colunas da tabela
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        urlColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().buildFullUrl()));
        methodColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMethod()));
        
        // Configura ComboBoxes
        methodComboBox.setItems(FXCollections.observableArrayList("POST", "PUT", "PATCH"));
        methodComboBox.setValue("POST");
        
        authTypeComboBox.setItems(FXCollections.observableArrayList(AuthType.values()));
        authTypeComboBox.setCellFactory(param -> new ListCell<AuthType>() {
            @Override
            protected void updateItem(AuthType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        authTypeComboBox.setValue(AuthType.NONE);
        
        // Atualiza campos de autenticação quando tipo muda
        authTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAuthFields(newValue);
        });
        
        // Seleção na tabela
        apisTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedApi = newValue;
                if (newValue != null) {
                    loadApiToForm(newValue);
                    deleteButton.setDisable(false);
                    testButton.setDisable(false);
                } else {
                    clearForm();
                    deleteButton.setDisable(true);
                    testButton.setDisable(true);
                }
            });
        
        // Valores padrão
        timeoutField.setText("30000");
        maxRetriesField.setText("3");
        
        // Desabilita botões inicialmente
        deleteButton.setDisable(true);
        testButton.setDisable(true);
        
        // Adiciona ícones aos botões
        if (newApiButton != null) {
            IconHelper.setIconWithText(newApiButton, FontAwesomeSolid.PLUS_CIRCLE, "Nova API");
        }
        IconHelper.setIconWithText(saveButton, FontAwesomeSolid.SAVE, "Salvar");
        IconHelper.setIconWithText(deleteButton, FontAwesomeSolid.TRASH_ALT, "Excluir");
        IconHelper.setIconWithText(testButton, FontAwesomeSolid.CHECK_CIRCLE, "Testar Conexão");
        
        updateAuthFields(AuthType.NONE);
    }
    
    @FXML
    private void handleSave() {
        try {
            ApiConfig config = createConfigFromForm();
            
            if (!config.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, preencha todos os campos obrigatórios.");
                return;
            }
            
            // Verifica se já existe (atualização) ou é novo
            int index = apis.indexOf(selectedApi);
            if (index >= 0) {
                apis.set(index, config);
            } else {
                apis.add(config);
            }
            
            // Salva no arquivo JSON
            boolean saved = configService.saveApi(config, new ArrayList<>(apis));
            if (!saved) {
                logger.warn("API salva na memória, mas houve erro ao salvar no arquivo");
                showAlert(Alert.AlertType.WARNING, "Aviso", 
                    "Configuração salva, mas houve problema ao salvar no arquivo. Verifique os logs.");
                return;
            }
            
            clearForm();
            apisTable.getSelectionModel().clearSelection();
            
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", 
                "Configuração de API salva com sucesso!");
            
        } catch (Exception e) {
            logger.error("Erro ao salvar configuração de API", e);
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Erro ao salvar configuração: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedApi == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Configuração de API");
        alert.setContentText("Deseja realmente excluir a configuração \"" + 
            selectedApi.getName() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String apiName = selectedApi.getName();
                apis.remove(selectedApi);
                
                // Salva no arquivo JSON
                boolean saved = configService.deleteApi(apiName, new ArrayList<>(apis));
                if (!saved) {
                    logger.warn("API removida da memória, mas houve erro ao salvar no arquivo");
                }
                
                clearForm();
                apisTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    @FXML
    private void handleTest() {
        try {
            ApiConfig config = createConfigFromForm();
            
            if (!config.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, preencha todos os campos obrigatórios.");
                return;
            }
            
            testButton.setDisable(true);
            testButton.setText("Testando...");
            
            // Testa conexão em thread separada
            new Thread(() -> {
                try {
                    LoadService loadService = new LoadService();
                    boolean success = loadService.testConnection(config);
                    
                    javafx.application.Platform.runLater(() -> {
                        testButton.setDisable(false);
                        testButton.setText("Testar Conexão");
                        
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Teste de Conexão", 
                                "Conexão com API bem-sucedida!");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Teste de Conexão", 
                                "Falha ao conectar. Verifique as configurações.");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        testButton.setDisable(false);
                        testButton.setText("Testar Conexão");
                        showAlert(Alert.AlertType.ERROR, "Erro", 
                            "Erro ao testar conexão: " + e.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception e) {
            logger.error("Erro ao testar conexão", e);
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Erro ao testar conexão: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNew() {
        clearForm();
        apisTable.getSelectionModel().clearSelection();
    }
    
    private ApiConfig createConfigFromForm() {
        ApiConfig config = new ApiConfig();
        config.setName(nameField.getText());
        config.setBaseUrl(baseUrlField.getText());
        config.setEndpoint(endpointField.getText());
        config.setMethod(methodComboBox.getValue());
        config.setAuthType(authTypeComboBox.getValue());
        
        // Configura autenticação baseada no tipo
        AuthType authType = authTypeComboBox.getValue();
        if (authType == AuthType.BEARER) {
            config.setAuthToken(authTokenField.getText());
        } else if (authType == AuthType.API_KEY) {
            config.setApiKey(apiKeyField.getText());
            config.setApiKeyHeader(apiKeyHeaderField.getText());
        } else if (authType == AuthType.BASIC) {
            config.setUsername(usernameField.getText());
            config.setPassword(passwordField.getText());
        }
        
        try {
            config.setTimeout(Integer.parseInt(timeoutField.getText()));
        } catch (NumberFormatException e) {
            config.setTimeout(30000);
        }
        
        try {
            config.setMaxRetries(Integer.parseInt(maxRetriesField.getText()));
        } catch (NumberFormatException e) {
            config.setMaxRetries(3);
        }
        
        return config;
    }
    
    private void loadApiToForm(ApiConfig config) {
        nameField.setText(config.getName());
        baseUrlField.setText(config.getBaseUrl());
        endpointField.setText(config.getEndpoint());
        methodComboBox.setValue(config.getMethod());
        authTypeComboBox.setValue(config.getAuthType());
        
        authTokenField.setText(config.getAuthToken());
        apiKeyField.setText(config.getApiKey());
        apiKeyHeaderField.setText(config.getApiKeyHeader());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
        
        timeoutField.setText(String.valueOf(config.getTimeout()));
        maxRetriesField.setText(String.valueOf(config.getMaxRetries()));
        
        updateAuthFields(config.getAuthType());
    }
    
    private void updateAuthFields(AuthType authType) {
        // Desabilita todos os campos primeiro
        authTokenField.setDisable(true);
        apiKeyField.setDisable(true);
        apiKeyHeaderField.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        
        // Habilita campos relevantes baseado no tipo
        if (authType == AuthType.BEARER) {
            authTokenField.setDisable(false);
        } else if (authType == AuthType.API_KEY) {
            apiKeyField.setDisable(false);
            apiKeyHeaderField.setDisable(false);
        } else if (authType == AuthType.BASIC) {
            usernameField.setDisable(false);
            passwordField.setDisable(false);
        }
    }
    
    private void clearForm() {
        nameField.clear();
        baseUrlField.clear();
        endpointField.clear();
        methodComboBox.setValue("POST");
        authTypeComboBox.setValue(AuthType.NONE);
        authTokenField.clear();
        apiKeyField.clear();
        apiKeyHeaderField.clear();
        usernameField.clear();
        passwordField.clear();
        timeoutField.setText("30000");
        maxRetriesField.setText("3");
        selectedApi = null;
        updateAuthFields(AuthType.NONE);
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
    
    public ObservableList<ApiConfig> getApis() {
        return apis;
    }
}

