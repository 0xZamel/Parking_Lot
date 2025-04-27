package com.example.parking;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.util.*;

public class Main extends Application {
    Stage window;
    public static void main(String[] args) {
        SystemManager.initialize();
        launch(args);
        SystemManager.save_data_to_file();
    }
    // Admin
    @Override
    public void start(Stage primaryStage) {
        try {
            // Set the custom icon for the window
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/css/images/logo.png"))));
            primaryStage.setTitle("Parking-Lot");

            // Load the FXML file
            window = primaryStage;
            Pane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/parking/menuFXML.fxml")));

            // Create and set the scene
            Scene scene = new Scene(root);
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}