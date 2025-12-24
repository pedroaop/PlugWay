package com.plugway.etl.ui;

import com.plugway.etl.dao.DatabaseConnectionFactory;
import com.plugway.etl.dao.ExtractService;
import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.model.DatabaseType;
import com.plugway.etl.service.ConnectionConfigService;
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
 * Controller para gerenciamento de conexões de banco de dados.
 */
public class ConnectionManagerController implements Initializable {
    
    private static final Logger logger = LoggerUtil.getLogger(ConnectionManagerController.class);
    
    @FXML
    private TableView<DatabaseConfig> connectionsTable;
    @FXML
    private TableColumn<DatabaseConfig, String> nameColumn;
    @FXML
    private TableColumn<DatabaseConfig, String> typeColumn;
    @FXML
    private TableColumn<DatabaseConfig, String> hostColumn;
    
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<DatabaseType> typeComboBox;
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField databaseField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button newConnectionButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button testButton;
    
    private ObservableList<DatabaseConfig> connections;
    private DatabaseConfig selectedConnection;
    private Stage stage;
    private ConnectionConfigService configService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando ConnectionManagerController");
        
        // Inicializa serviço de persistência
        configService = ConnectionConfigService.getInstance();
        
        // Carrega conexões salvas do arquivo
        List<DatabaseConfig> savedConnections = configService.loadConnections();
        connections = FXCollections.observableArrayList(savedConnections);
        connectionsTable.setItems(connections);
        
        logger.info("Carregadas {} conexões do arquivo", connections.size());
        
        // Configura colunas da tabela
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType() != null ? cellData.getValue().getType().getDisplayName() : ""));
        hostColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHost()));
        
        // Configura ComboBox de tipos
        typeComboBox.setItems(FXCollections.observableArrayList(DatabaseType.values()));
        typeComboBox.setCellFactory(param -> new ListCell<DatabaseType>() {
            @Override
            protected void updateItem(DatabaseType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // Seleção na tabela
        connectionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedConnection = newValue;
                if (newValue != null) {
                    loadConnectionToForm(newValue);
                    enableFormFields(true); // Garante que campos estejam habilitados
                    deleteButton.setDisable(false);
                    testButton.setDisable(false);
                } else {
                    clearForm();
                    enableFormFields(true); // Garante que campos estejam habilitados mesmo sem seleção
                    deleteButton.setDisable(true);
                    testButton.setDisable(true);
                }
            });
        
        // Define porta padrão quando tipo muda
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && (portField.getText().isEmpty() || oldValue != null)) {
                switch (newValue) {
                    case FIREBIRD:
                        portField.setText("3050");
                        break;
                    case MYSQL:
                        portField.setText("3306");
                        break;
                    case POSTGRESQL:
                        portField.setText("5432");
                        break;
                    case SQLSERVER:
                        portField.setText("1433");
                        break;
                }
            }
        });
        
        // Desabilita botões inicialmente
        deleteButton.setDisable(true);
        testButton.setDisable(true);
        
        // Adiciona ícones aos botões
        if (newConnectionButton != null) {
            IconHelper.setIconWithText(newConnectionButton, FontAwesomeSolid.PLUS_CIRCLE, "Nova Conexão");
        }
        IconHelper.setIconWithText(saveButton, FontAwesomeSolid.SAVE, "Salvar");
        IconHelper.setIconWithText(deleteButton, FontAwesomeSolid.TRASH_ALT, "Excluir");
        IconHelper.setIconWithText(testButton, FontAwesomeSolid.CHECK_CIRCLE, "Testar Conexão");
        
        // Garante que os campos estão editáveis e habilitados (chamado por último)
        // Usa Platform.runLater para garantir que seja executado após a inicialização completa
        javafx.application.Platform.runLater(() -> {
            enableFormFields(true);
            logger.info("Campos do formulário habilitados após inicialização");
        });
    }
    
    @FXML
    private void handleSave() {
        try {
            DatabaseConfig config = createConfigFromForm();
            
            if (!config.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, preencha todos os campos obrigatórios.");
                return;
            }
            
            // Verifica se já existe (atualização) ou é novo
            int index = connections.indexOf(selectedConnection);
            if (index >= 0) {
                connections.set(index, config);
            } else {
                connections.add(config);
            }
            
            // Salva no arquivo JSON
            boolean saved = configService.saveConnection(config, new ArrayList<>(connections));
            if (!saved) {
                logger.warn("Conexão salva na memória, mas houve erro ao salvar no arquivo");
                showAlert(Alert.AlertType.WARNING, "Aviso", 
                    "Conexão salva, mas houve problema ao salvar no arquivo. Verifique os logs.");
                return;
            }
            
            clearForm();
            connectionsTable.getSelectionModel().clearSelection();
            
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", 
                "Conexão salva com sucesso!");
            
        } catch (Exception e) {
            logger.error("Erro ao salvar conexão", e);
            showAlert(Alert.AlertType.ERROR, "Erro", 
                "Erro ao salvar conexão: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedConnection == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Conexão");
        alert.setContentText("Deseja realmente excluir a conexão \"" + 
            selectedConnection.getName() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String connectionName = selectedConnection.getName();
                connections.remove(selectedConnection);
                
                // Salva no arquivo JSON
                boolean saved = configService.deleteConnection(connectionName, new ArrayList<>(connections));
                if (!saved) {
                    logger.warn("Conexão removida da memória, mas houve erro ao salvar no arquivo");
                }
                
                clearForm();
                connectionsTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    @FXML
    private void handleTest() {
        try {
            DatabaseConfig config = createConfigFromForm();
            
            if (!config.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Erro", 
                    "Por favor, preencha todos os campos obrigatórios.");
                return;
            }
            
            testButton.setDisable(true);
            testButton.setText("Testando...");
            
            // Testa conexão em thread separada para não travar a UI
            new Thread(() -> {
                try {
                    ExtractService extractService = new ExtractService();
                    boolean success = extractService.testConnection(config);
                    
                    javafx.application.Platform.runLater(() -> {
                        testButton.setDisable(false);
                        testButton.setText("Testar Conexão");
                        
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Teste de Conexão", 
                                "Conexão bem-sucedida!");
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
        connectionsTable.getSelectionModel().clearSelection();
        
        // Garante que os campos estão habilitados
        enableFormFields(true);
        
        // Foca no campo Nome para facilitar a entrada
        nameField.requestFocus();
    }
    
    /**
     * Habilita ou desabilita os campos do formulário.
     */
    private void enableFormFields(boolean enable) {
        logger.debug("Habilitando campos do formulário: {}", enable);
        
        if (nameField != null) {
            nameField.setEditable(enable);
            nameField.setDisable(!enable);
            logger.debug("Campo 'nameField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'nameField' é null!");
        }
        
        if (typeComboBox != null) {
            typeComboBox.setDisable(!enable);
            // ComboBox não precisa de setEditable, mas precisa estar habilitado
            logger.debug("Campo 'typeComboBox' - disabled: {}", !enable);
        } else {
            logger.warn("Campo 'typeComboBox' é null!");
        }
        
        if (hostField != null) {
            hostField.setEditable(enable);
            hostField.setDisable(!enable);
            logger.debug("Campo 'hostField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'hostField' é null!");
        }
        
        if (portField != null) {
            portField.setEditable(enable);
            portField.setDisable(!enable);
            logger.debug("Campo 'portField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'portField' é null!");
        }
        
        if (databaseField != null) {
            databaseField.setEditable(enable);
            databaseField.setDisable(!enable);
            logger.debug("Campo 'databaseField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'databaseField' é null!");
        }
        
        if (usernameField != null) {
            usernameField.setEditable(enable);
            usernameField.setDisable(!enable);
            logger.debug("Campo 'usernameField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'usernameField' é null!");
        }
        
        if (passwordField != null) {
            passwordField.setEditable(enable);
            passwordField.setDisable(!enable);
            logger.debug("Campo 'passwordField' - editable: {}, disabled: {}", enable, !enable);
        } else {
            logger.warn("Campo 'passwordField' é null!");
        }
    }
    
    private DatabaseConfig createConfigFromForm() {
        DatabaseConfig config = new DatabaseConfig();
        config.setName(nameField.getText());
        config.setType(typeComboBox.getValue());
        config.setHost(hostField.getText());
        
        try {
            config.setPort(Integer.parseInt(portField.getText()));
        } catch (NumberFormatException e) {
            config.setPort(0);
        }
        
        config.setDatabase(databaseField.getText());
        config.setUsername(usernameField.getText());
        config.setPassword(passwordField.getText());
        
        return config;
    }
    
    private void loadConnectionToForm(DatabaseConfig config) {
        // Garante que os campos estejam habilitados antes de carregar
        enableFormFields(true);
        
        nameField.setText(config.getName() != null ? config.getName() : "");
        typeComboBox.setValue(config.getType());
        hostField.setText(config.getHost() != null ? config.getHost() : "");
        portField.setText(config.getPort() > 0 ? String.valueOf(config.getPort()) : "");
        databaseField.setText(config.getDatabase() != null ? config.getDatabase() : "");
        usernameField.setText(config.getUsername() != null ? config.getUsername() : "");
        passwordField.setText(config.getPassword() != null ? config.getPassword() : "");
    }
    
    private void clearForm() {
        nameField.clear();
        typeComboBox.setValue(null);
        hostField.clear();
        portField.clear();
        databaseField.clear();
        usernameField.clear();
        passwordField.clear();
        selectedConnection = null;
        
        // Garante que os campos permaneçam habilitados após limpar
        enableFormFields(true);
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
    
    public ObservableList<DatabaseConfig> getConnections() {
        return connections;
    }
}

