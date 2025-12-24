package com.plugway.etl.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

/**
 * Classe utilitária para gerenciar ícones FontAwesome na interface gráfica.
 * Facilita a criação e aplicação de ícones em componentes JavaFX.
 * 
 * @author PlugWay ETL
 * @version 1.0
 */
public class IconHelper {
    
    // Tamanhos padrão
    public static final int SIZE_SMALL = 12;
    public static final int SIZE_MEDIUM = 16;
    public static final int SIZE_LARGE = 24;
    public static final int SIZE_XLARGE = 32;
    
    // Cores padrão
    public static final String COLOR_PRIMARY = "#4a90e2";
    public static final String COLOR_SUCCESS = "#27ae60";
    public static final String COLOR_DANGER = "#e74c3c";
    public static final String COLOR_WARNING = "#f39c12";
    public static final String COLOR_INFO = "#3498db";
    public static final String COLOR_DARK = "#333333";
    public static final String COLOR_LIGHT = "#ffffff";
    
    /**
     * Cria um ícone FontAwesome Solid com tamanho padrão médio.
     * 
     * @param icon O ícone FontAwesome a ser criado
     * @return FontIcon configurado
     */
    public static FontIcon createIcon(FontAwesomeSolid icon) {
        return createIcon(icon, SIZE_MEDIUM);
    }
    
    /**
     * Cria um ícone FontAwesome Solid com tamanho personalizado.
     * 
     * @param icon O ícone FontAwesome a ser criado
     * @param size O tamanho do ícone em pixels
     * @return FontIcon configurado
     */
    public static FontIcon createIcon(FontAwesomeSolid icon, int size) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(size);
        return fontIcon;
    }
    
    /**
     * Cria um ícone FontAwesome Solid com tamanho e cor personalizados.
     * 
     * @param icon O ícone FontAwesome a ser criado
     * @param size O tamanho do ícone em pixels
     * @param color A cor do ícone em formato hexadecimal (ex: "#4a90e2")
     * @return FontIcon configurado
     */
    public static FontIcon createIcon(FontAwesomeSolid icon, int size, String color) {
        FontIcon fontIcon = createIcon(icon, size);
        fontIcon.setIconColor(Color.web(color));
        return fontIcon;
    }
    
    /**
     * Adiciona ícone a um botão com tamanho padrão médio.
     * 
     * @param button O botão que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     */
    public static void setIconToButton(Button button, FontAwesomeSolid icon) {
        setIconToButton(button, icon, SIZE_MEDIUM);
    }
    
    /**
     * Adiciona ícone a um botão com tamanho personalizado.
     * 
     * @param button O botão que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     */
    public static void setIconToButton(Button button, FontAwesomeSolid icon, int size) {
        FontIcon fontIcon = createIcon(icon, size);
        button.setGraphic(fontIcon);
    }
    
    /**
     * Adiciona ícone a um botão com tamanho e cor personalizados.
     * 
     * @param button O botão que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     * @param color A cor do ícone em formato hexadecimal
     */
    public static void setIconToButton(Button button, FontAwesomeSolid icon, int size, String color) {
        FontIcon fontIcon = createIcon(icon, size, color);
        button.setGraphic(fontIcon);
    }
    
    /**
     * Adiciona ícone a um label com tamanho padrão médio.
     * 
     * @param label O label que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     */
    public static void setIconToLabel(Label label, FontAwesomeSolid icon) {
        setIconToLabel(label, icon, SIZE_MEDIUM);
    }
    
    /**
     * Adiciona ícone a um label com tamanho personalizado.
     * 
     * @param label O label que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     */
    public static void setIconToLabel(Label label, FontAwesomeSolid icon, int size) {
        FontIcon fontIcon = createIcon(icon, size);
        label.setGraphic(fontIcon);
    }
    
    /**
     * Adiciona ícone a um label com tamanho e cor personalizados.
     * 
     * @param label O label que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     * @param color A cor do ícone em formato hexadecimal
     */
    public static void setIconToLabel(Label label, FontAwesomeSolid icon, int size, String color) {
        FontIcon fontIcon = createIcon(icon, size, color);
        label.setGraphic(fontIcon);
    }
    
    /**
     * Cria um botão com ícone e texto (ícone + texto lado a lado).
     * 
     * @param button O botão que receberá o ícone e texto
     * @param icon O ícone FontAwesome a ser adicionado
     * @param text O texto a ser exibido
     */
    public static void setIconWithText(Button button, FontAwesomeSolid icon, String text) {
        setIconWithText(button, icon, text, SIZE_SMALL);
    }
    
    /**
     * Cria um botão com ícone e texto (ícone + texto lado a lado) com tamanho personalizado.
     * 
     * @param button O botão que receberá o ícone e texto
     * @param icon O ícone FontAwesome a ser adicionado
     * @param text O texto a ser exibido
     * @param iconSize O tamanho do ícone em pixels
     */
    public static void setIconWithText(Button button, FontAwesomeSolid icon, String text, int iconSize) {
        if (button == null) {
            return; // Retorna silenciosamente se o botão não existir
        }
        FontIcon fontIcon = createIcon(icon, iconSize);
        button.setGraphic(fontIcon);
        button.setText(" " + text); // Espaço entre ícone e texto
    }
    
    /**
     * Cria um ícone de status (sucesso, erro, aviso, info).
     * 
     * @param icon O ícone FontAwesome a ser criado
     * @param statusType O tipo de status ("success", "danger", "warning", "info")
     * @return FontIcon configurado com cor apropriada
     */
    public static FontIcon createStatusIcon(FontAwesomeSolid icon, String statusType) {
        String color;
        switch (statusType.toLowerCase()) {
            case "success":
                color = COLOR_SUCCESS;
                break;
            case "danger":
            case "error":
                color = COLOR_DANGER;
                break;
            case "warning":
                color = COLOR_WARNING;
                break;
            case "info":
                color = COLOR_INFO;
                break;
            default:
                color = COLOR_DARK;
        }
        return createIcon(icon, SIZE_MEDIUM, color);
    }
    
    /**
     * Adiciona ícone a um MenuItem com tamanho padrão pequeno.
     * 
     * @param menuItem O MenuItem que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     */
    public static void setIconToMenuItem(MenuItem menuItem, FontAwesomeSolid icon) {
        setIconToMenuItem(menuItem, icon, SIZE_SMALL);
    }
    
    /**
     * Adiciona ícone a um MenuItem com tamanho personalizado.
     * 
     * @param menuItem O MenuItem que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     */
    public static void setIconToMenuItem(MenuItem menuItem, FontAwesomeSolid icon, int size) {
        FontIcon fontIcon = createIcon(icon, size);
        menuItem.setGraphic(fontIcon);
    }
    
    /**
     * Adiciona ícone a um MenuItem com tamanho e cor personalizados.
     * 
     * @param menuItem O MenuItem que receberá o ícone
     * @param icon O ícone FontAwesome a ser adicionado
     * @param size O tamanho do ícone em pixels
     * @param color A cor do ícone em formato hexadecimal
     */
    public static void setIconToMenuItem(MenuItem menuItem, FontAwesomeSolid icon, int size, String color) {
        FontIcon fontIcon = createIcon(icon, size, color);
        menuItem.setGraphic(fontIcon);
    }
}

