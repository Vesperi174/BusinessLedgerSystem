package com.businessledger;

import com.businessledger.config.SpringContextHolder;
import com.businessledger.ui.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFxLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainView.fxml")
        );
        loader.setControllerFactory(SpringContextHolder::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/theme.css").toExternalForm()
        );
        scene.getStylesheets().add(
                getClass().getResource("/css/main.css").toExternalForm()
        );

        primaryStage.setTitle("桌游店业务台账系统");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(800);
        primaryStage.setScene(scene);
        primaryStage.show();

        MainController controller = loader.getController();
        controller.setStage(primaryStage);
    }
}