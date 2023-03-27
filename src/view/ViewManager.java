package view;

import application.Main;
import com.sun.corba.se.pept.transport.InboundConnectionCache;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.SweeperButton;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Klasa obsługująca wyświetlanie menu głównego
public class ViewManager {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private AnchorPane mainPane;
    private Scene mainScene;
    private Stage mainStage;

    private final static int MENU_BUTTON_START_X = 417;
    private final static int MENU_BUTTON_START_Y = 300;

    List<SweeperButton> menuButtons;

    private static int BOARD_SIZE_X = 8;
    private static int BOARD_SIZE_Y = 8;
    private static int MINES = 10;
    private int difficulty = 0;

    private ImageView logoView;

    // Konstruktor klasy ViewManager
    public ViewManager() {
        menuButtons = new ArrayList<>();
        mainPane = new AnchorPane();
        mainScene = new Scene(mainPane, WIDTH, HEIGHT);
        mainStage = new Stage();
        mainStage.setTitle("Saper");
        mainStage.setScene(mainScene);

        mainStage.setResizable(false);
        mainStage.setWidth(WIDTH);
        mainStage.setHeight(HEIGHT);

        //createBackground();
        createLogo();
        createButtons();
    }

    public Stage getMainStage() {
        return mainStage;
    }

    // Metoda ustawiająca tło programu, nie jest w użytu z powodu brakującego pliku tła
    private void createBackground() {
        Image backgroundImage = new Image("view/resources/gameBackground.jpg", WIDTH, HEIGHT,
                false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, null);
        mainPane.setBackground(new Background(background));
    }

    // Metoda ustawiająca logo programu, wyświetlające się w górnej części okna
    private void createLogo() {
        logoView = new ImageView("view/resources/logo.png");
        logoView.setLayoutX(115);
        logoView.setLayoutY(0);

        logoView.setVisible(false);
        mainPane.getChildren().add(logoView);
    }

    // Ogólna metoda dodająca kolejny guzik do menu głównego, zawierający się w argumencie funkcji
    private void addMenuButton(SweeperButton button) {
        button.setLayoutX(MENU_BUTTON_START_X - 700);
        button.setLayoutY(MENU_BUTTON_START_Y + menuButtons.size() * 100);
        menuButtons.add(button);
        mainPane.getChildren().add(button);
    }

    // Metoda wywołująca wszystkie metody związane z tworzeniem guzików
    private void createButtons() {
        createStartButton();
        createOptionButton();
        createRankingButton();
        createExitButton();
    }

    //Metoda tworząca guzik "Start"
    private void createStartButton() {
        SweeperButton startButton = new SweeperButton("Start");
        addMenuButton(startButton);

        startButton.setOnAction(event -> {
            if (MINES >= (BOARD_SIZE_X * BOARD_SIZE_Y) - 8) {
                final Stage errorDialog = new Stage();
                errorDialog.initModality(Modality.APPLICATION_MODAL);
                errorDialog.initOwner(mainStage);

                VBox errorVBox = new VBox();
                errorVBox.setSpacing(20);
                errorVBox.setAlignment(Pos.CENTER);

                Scene errorScene = new Scene(errorVBox, 300, 100);
                errorDialog.setResizable(false);

                Label errorLabel = new Label("Za duża ilośc min, rozgrywka niemożliwa!");
                errorVBox.getChildren().add(errorLabel);
                Button confirmButton = new Button("Ok");

                confirmButton.setOnAction(event1 -> errorDialog.close());
                errorVBox.getChildren().add(confirmButton);
                errorDialog.setScene(errorScene);
                errorDialog.show();
                return;
            }
            GameViewManager gameManager = new GameViewManager(difficulty, this);
            gameManager.createNewGame(mainStage, BOARD_SIZE_X, BOARD_SIZE_Y, MINES);
        });
    }

    // Ogólna metoda tworząca wszystkie suwaki używane w aplikacji, używane w menu opcji
    private HBox createSliderHBox(int option ,String text, int val1, int val2, int val3) {
        final Slider slider = new Slider(val1, val2, val3);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        final Label label = new Label(text);
        //final Label valueLabel = new Label(Integer.toString(val3));
        final TextField valueField = new TextField(Integer.toString(val3));
        switch(option) {
            case 0:
                slider.setValue(BOARD_SIZE_X);
                //valueLabel.setText(Integer.toString(BOARD_SIZE_X));
                valueField.setText(Integer.toString(BOARD_SIZE_X));
                slider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        int intValue = newValue.intValue();
                        BOARD_SIZE_X = intValue;
                        valueField.setText(Integer.toString(intValue));
                    }
                });
                valueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (!newValue.matches("\\d*")) {
                            valueField.setText(newValue.replaceAll("[^\\d]", ""));
                        }
                        if (!valueField.getText().equals("")) {
                            int intValue = Integer.parseInt(valueField.getText());
                            if (intValue >= val1 && intValue <= val2) {
                                BOARD_SIZE_X = intValue;
                                slider.setValue(intValue);
                            }
                        }
                    }
                });
                break;
            case 1:
                slider.setValue(BOARD_SIZE_Y);
                //valueLabel.setText(Integer.toString(BOARD_SIZE_Y));
                valueField.setText(Integer.toString(BOARD_SIZE_Y));
                slider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        int intValue = newValue.intValue();
                        BOARD_SIZE_Y = intValue;
                        valueField.setText(Integer.toString(intValue));
                    }
                });
                valueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (!newValue.matches("\\d*")) {
                            valueField.setText(newValue.replaceAll("[^\\d]", ""));
                        }
                        if (!valueField.getText().equals("")) {
                            int intValue = Integer.parseInt(valueField.getText());
                            if (intValue >= val1 && intValue <= val2)
                            {
                                BOARD_SIZE_Y = intValue;
                                slider.setValue(intValue);
                            }
                        }
                    }
                });
                break;
            case 2:
                slider.setValue(MINES);
                //valueLabel.setText(Integer.toString(MINES));
                valueField.setText(Integer.toString(MINES));
                slider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        int intValue = newValue.intValue();
                        MINES = intValue;
                        valueField.setText(Integer.toString(intValue));
                    }
                });
                valueField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (!newValue.matches("\\d*")) {
                            valueField.setText(newValue.replaceAll("[^\\d]", ""));
                        }
                        if (!valueField.getText().equals("")) {
                            final int intValue = Integer.parseInt(valueField.getText());
                            if (intValue >= val1 && intValue <= val2) {
                                MINES = intValue;
                                slider.setValue(intValue);
                            }

                        }
                    }
                });
                break;
        }
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPrefWidth(350);
        //valueLabel.setPrefWidth(30);
       // valueLabel.setAlignment(Pos.CENTER);
        valueField.setPrefWidth(45);
        valueField.setAlignment(Pos.CENTER);
        label.setPrefWidth(125);
        hBox.getChildren().add(label);
        hBox.getChildren().add(slider);
        //hBox.getChildren().add(valueLabel);
        hBox.getChildren().add(valueField);

        return hBox;
    }

    // Metoda używana do tworzenia pola wyboru poziomu trudności
    private HBox createComboBox(HBox fieldXBox, HBox fieldYBox, HBox mineBox) {
        Label nameLabel = new Label("Poziom trudności:");
        nameLabel.setAlignment(Pos.CENTER);

        ComboBox<String> comboBox = new ComboBox<String>();
        comboBox.getItems().add("Łatwy");
        comboBox.getItems().add("Średni");
        comboBox.getItems().add("Trudny");
        comboBox.getItems().add("Niestandardowy");

        comboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                String value = (String) newValue;
                Slider fieldX = (Slider) fieldXBox.getChildren().get(1);
                Slider fieldY = (Slider) fieldYBox.getChildren().get(1);
                Slider mine = (Slider) mineBox.getChildren().get(1);

                TextField fieldXField = (TextField) fieldXBox.getChildren().get(2);
                TextField fieldYField = (TextField) fieldYBox.getChildren().get(2);
                TextField mineField = (TextField) mineBox.getChildren().get(2);

                fieldX.setDisable(true);
                fieldY.setDisable(true);
                mine.setDisable(true);
                fieldXField.setDisable(true);
                fieldYField.setDisable(true);
                mineField.setDisable(true);

                switch (value) {
                    case "Łatwy":
                        fieldX.setValue(8);
                        fieldY.setValue(8);
                        mine.setValue(10);
                        difficulty = 0;
                        break;
                    case "Średni":
                        fieldX.setValue(16);
                        fieldY.setValue(16);
                        mine.setValue(40);
                        difficulty = 1;
                        break;
                    case "Trudny":
                        fieldX.setValue(30);
                        fieldY.setValue(20);
                        mine.setValue(120);
                        difficulty = 2;
                        break;
                    case "Niestandardowy":
                        fieldX.setDisable(false);
                        fieldY.setDisable(false);
                        mine.setDisable(false);
                        fieldXField.setDisable(false);
                        fieldYField.setDisable(false);
                        mineField.setDisable(false);
                        difficulty = 3;
                }
            }
        });
        comboBox.getSelectionModel().select(difficulty);
        HBox hBox = new HBox(20);
        hBox.setPrefWidth(350);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(nameLabel);
        hBox.getChildren().add(comboBox);
        return hBox;
    }

    // Metoda tworząca okno ustawień gry, pozwalające modyfikować poziom trudności lub manualnie dobrać właściwości pola
    private void initOptionDialog() {
        final Stage optionDialog = new Stage();
        optionDialog.initModality(Modality.APPLICATION_MODAL);
        optionDialog.initOwner(mainStage);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(0,0,0,5));

        HBox fieldXBox = createSliderHBox(0,"Szerokość pola gry:", 5, 30, 8);
        HBox fieldYBox = createSliderHBox(1,"Długość pola gry:", 5, 30, 8);
        HBox mineBox = createSliderHBox(2,"Ilość min:", 1, 200, 10);

        HBox comboBox = createComboBox(fieldXBox, fieldYBox, mineBox);

        Label optionsBasic = new Label("Ustawienia podstawowe:");
        optionsBasic.setPadding(new Insets(5, 0, 0, 0));
        Label optionsAdvanced = new Label("Ustawienia zaawansowane:");
        optionsAdvanced.setPadding(new Insets(5, 0, 0, 0));

        dialogVBox.getChildren().addAll(optionsBasic ,comboBox, optionsAdvanced ,fieldXBox, fieldYBox, mineBox);

        Scene dialogScene = new Scene(dialogVBox, 390, 300);
        optionDialog.setResizable(false);

        Button confirmButton = new Button("Potwierdź");
        confirmButton.setAlignment(Pos.CENTER);

        confirmButton.setOnAction(event -> optionDialog.close());
        dialogVBox.getChildren().add(confirmButton);

        optionDialog.setScene(dialogScene);
        optionDialog.show();
    }

    // Ogólna metoda do wczytywania plików tekstowych, zwraca jej zawartość jako ArrayList
    public String[] readFile(String filePath) throws IOException {
        File f = new File(filePath);
        if(f.exists() && !f.isDirectory())
        {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null && lines.size() < 10)
                lines.add(line);
            bufferedReader.close();
            return lines.toArray(new String[lines.size()]);
        }
        else
        {
            f.createNewFile();
            FileWriter fw = new FileWriter(f, false);
            fw.write(" ");
        }


        String[] returnString = {};
        return returnString;
    }

    // Metoda obsługująca wyświetlanie rankingu, zależnie od wybranego poziomu trudności
    private HBox createDifficultyComboBox(VBox scoreBox) {
        Label nameLabel = new Label("Poziom trudności:");
        nameLabel.setAlignment(Pos.CENTER);

        ComboBox<String> comboBox = new ComboBox<String>();
        comboBox.getItems().add("Łatwy");
        comboBox.getItems().add("Średni");
        comboBox.getItems().add("Trudny");

        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                scoreBox.getChildren().clear();
                String filePath;

                switch (newValue) {
                    case "Łatwy":
                        filePath = "rankingEasy.txt";
                        break;
                    case "Średni":
                        filePath = "rankingMedium.txt";
                        break;
                    case "Trudny":
                        filePath = "rankingHard.txt";
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + newValue);
                }

                String[] ranking = new String[0];
                try {
                    ranking = readFile(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to read file: " + filePath);
                }
                for (int i = 0; i < ranking.length; i++) {
                    if (ranking[i].equals(" "))
                        break;
                    Label rank = new Label(Integer.toString(i+1) + ".");
                    rank.setPrefWidth(40);
                    String[] newRanking = ranking[i].split(" ");
                    Label name = new Label(newRanking[0]);
                    name.setPrefWidth(160);
                    Label score = new Label(newRanking[1]);
                    score.setPrefWidth(60);
                    HBox setBox = new HBox();
                    setBox.getChildren().addAll(rank, name, score);
                    scoreBox.getChildren().add(setBox);
                }
            }
        });

        comboBox.getSelectionModel().select(0);
        HBox hBox = new HBox(20);
        hBox.setPrefWidth(350);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(5,0,0,0));
        hBox.getChildren().add(nameLabel);
        hBox.getChildren().add(comboBox);
        return hBox;
    }

    // Metoda obsługująca wyświetlanie okna zawierającego ranking
    public void initRankingDialog(Stage owner) {
        final Stage rankingDialog = new Stage();
        rankingDialog.initModality(Modality.APPLICATION_MODAL);
        rankingDialog.initOwner(owner);

        Label titleLabel = new Label("Imię:");
        titleLabel.setPrefWidth(200);
        Label titleLabel2 = new Label("Czas:");
        titleLabel2.setPrefWidth(60);
        titleLabel.setPadding(new Insets(0,0,5,40));

        HBox titleBox = new HBox();
        titleBox.getChildren().addAll(titleLabel, titleLabel2);

        VBox rankingBox = new VBox(10);
        VBox scoreBox = new VBox(5);
        HBox difficultyBox = createDifficultyComboBox(scoreBox);
        rankingBox.getChildren().addAll(difficultyBox,titleBox, scoreBox);
        Scene rankingScene = new Scene(rankingBox, 300, 325);
        rankingDialog.setResizable(false);
        rankingDialog.setScene(rankingScene);
        rankingDialog.show();
    }

    // Metoda tworząca guzik opcji
    private void createOptionButton() {
        SweeperButton optionButton = new SweeperButton("Opcje");
        addMenuButton(optionButton);

        optionButton.setOnAction(event -> initOptionDialog());
    }

    // Metoda tworząca guzik rankingu
    private void createRankingButton() {
        SweeperButton rankingButton = new SweeperButton("Ranking");
        addMenuButton(rankingButton);

        rankingButton.setOnAction(event -> initRankingDialog(mainStage));
    }

    // Metoda tworząca guzik wyjścia
    private void createExitButton() {
        SweeperButton exitButton = new SweeperButton("Wyjdź");
        addMenuButton(exitButton);

        exitButton.setOnAction(event -> mainStage.close());
    }

    // Metoda obsłgująca animacje startowe: pojawiające się logo oraz wsuwające guziki
    public void initStartupAnimations() {
        FadeTransition ft = new FadeTransition(Duration.millis(1500), logoView);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        logoView.setVisible(true);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        setButtonAnimationQueue(timeline, 0);
    }

    // Metoda obsłgująca kolejkę animacji guzików
    private void setButtonAnimationQueue(Timeline timeline, int count) {
        KeyValue kv1 = new KeyValue(menuButtons.get(count).layoutXProperty(), MENU_BUTTON_START_X);
        KeyFrame kf1 = new KeyFrame(Duration.millis(400), kv1);
        timeline.getKeyFrames().add(kf1);
        timeline.play();
        if (count >= menuButtons.size()-1)
            return;
        timeline.setOnFinished(event -> {
            Timeline newTimeline = new Timeline();
            newTimeline.setCycleCount(1);
            setButtonAnimationQueue(newTimeline, count+1);
        });
    }
}
