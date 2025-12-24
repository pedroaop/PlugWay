package com.plugway.etl;

import com.plugway.etl.config.ConfigManager;
import com.plugway.etl.eip.ControlBus;
import com.plugway.etl.ui.MainController;
import com.plugway.etl.util.LoggerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Classe principal da aplicação ETL Java Desktop.
 * Ponto de entrada da aplicação.
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerUtil.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Iniciando aplicação ETL Java Desktop...");
        
        // Inicializar configurações
        try {
            ConfigManager.getInstance().loadConfiguration();
            logger.info("Configurações carregadas com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao carregar configurações", e);
            System.exit(1);
        }
        
        // Lançar aplicação JavaFX
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Iniciando interface gráfica...");
        
        try {
            // Carrega o FXML da tela principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // Obtém o controller
            MainController controller = loader.getController();
            controller.setStage(primaryStage);
            
            // Configura a cena
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            // Configura a janela principal
            primaryStage.setTitle("PlugWay ETL - Java Desktop");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Mostra a janela
            primaryStage.show();
            
            logger.info("Interface gráfica iniciada com sucesso");
            
        } catch (IOException e) {
            logger.error("Erro ao carregar interface gráfica", e);
            // Fallback: mostra uma janela simples se o FXML não existir
            showFallbackWindow(primaryStage);
        } catch (Exception e) {
            logger.error("Erro ao iniciar interface gráfica", e);
            showFallbackWindow(primaryStage);
        }
    }
    
    /**
     * Mostra uma janela simples caso o FXML não esteja disponível.
     */
    private void showFallbackWindow(Stage primaryStage) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(
            "PlugWay ETL Java Desktop\n\n" +
            "Interface gráfica em desenvolvimento.\n" +
            "Verifique os logs para mais informações."
        );
        label.setStyle("-fx-font-size: 14px; -fx-padding: 20px;");
        
        Scene scene = new Scene(new javafx.scene.layout.StackPane(label), 400, 200);
        primaryStage.setTitle("PlugWay ETL");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        logger.info("Encerrando aplicação...");
        
        // Encerra o Control Bus
        try {
            ControlBus.getInstance().shutdown();
        } catch (Exception e) {
            logger.error("Erro ao encerrar Control Bus", e);
        }
        
        // Encerra o JobScheduler
        try {
            com.plugway.etl.service.scheduler.JobScheduler.getInstance().shutdown();
        } catch (Exception e) {
            logger.error("Erro ao encerrar JobScheduler", e);
        }
        
        // Limpar outros recursos se necessário
    }
}

