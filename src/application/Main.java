package application;

import javafx.application.Application;
import javafx.stage.Stage;
import view.ViewManager;

public class Main extends Application {
    // Metoda uruchamiająca program, wywołuje ViewManager, który obsługuje menu główne aplikacji
    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            ViewManager manager = new ViewManager();
            primaryStage = manager.getMainStage();
            primaryStage.show();
            manager.initStartupAnimations();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
