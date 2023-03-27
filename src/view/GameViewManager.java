package view;

import application.Main;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import model.MineField;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

//Klasa obsługująca wyświetlania gry po wciśnięciu przycisku "Start"
public class GameViewManager {

    private int time;
    private int flags;
    private int mines;
    private int difficulty;
    private boolean fieldComplete;
    private boolean timerActive;

    ViewManager menuManager;

    private HBox gamePane;
    private AnchorPane scorePane;
    private AnchorPane timePane;
    private Scene gameScene;

    private Stage gameStage;
    private Stage menuStage;

    private static final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    private static final int GAME_WIDTH = (int) (primaryScreenBounds.getWidth() / 1.5);
    private static final int GAME_HEIGHT = (int) (primaryScreenBounds.getHeight() / 1.25);
    private static final  String FONT_PATH = "model/resources/Audiowide-Regular.ttf";

    private Image flagImage;
    private Image mineImage;
    private Image mineExpImage;
    private Image mineFlagImage;

    private GridPane boardPane;
    private MineField[][] mineField;
    private HBox labelBox;
    private HBox timeBox;

    // Gettery i settery - początek
    public Image getFlagImage() {
        return flagImage;
    }

    public Image getMineImage() {
        return mineImage;
    }

    public Image getMineExpImage() {
        return mineExpImage;
    }

    public Image getMineFlagImage() {
        return mineFlagImage;
    }

    public boolean getFieldComplete() {
        return fieldComplete;
    }

    public void setFieldComplete(boolean flag) {
        fieldComplete = flag;
    }

    public HBox getLabelBox() {
        return labelBox;
    }

    public MineField[][] getMineField() {
        return mineField;
    }

    public int getMines() {
        return mines;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
    //Gettery i settery - koniec

    // Konstruktor klasy GameViewManager
    public GameViewManager(int difficulty, ViewManager menuManager) {
        initStage();
        //createBackground();
        fieldComplete = false;
        this.difficulty = difficulty;
        this.menuManager = menuManager;

    }

    // Metoda obsłgująca koniec gry - zarówno wygraną jak i przegraną
    public void gameOver(boolean win) {
        timerActive = false;

        for (MineField[] mineFields : mineField) {
            for (MineField field : mineFields) {
                if (!field.getActivated()) {
                    field.setActivated(true);
                    field.setMouseTransparent(true);
                    field.checkField();
                }
            }
        }

        final Stage endDialog = new Stage();
        endDialog.initModality(Modality.APPLICATION_MODAL);
        endDialog.initOwner(gameStage);

        VBox endVBox = new VBox();
        endVBox.setSpacing(10);
        endVBox.setAlignment(Pos.CENTER);

        Scene endScene = new Scene(endVBox, 300, 150);
        endDialog.setResizable(false);
        Label endLabel;
        String endString;
        TextField highScoreName = new TextField("Gracz");
        if (!win)
        {
            endString = String.format("Przegrana!");
            endLabel = new Label(endString);
            endVBox.getChildren().add(endLabel);
        }
        else if (difficulty == 3) {
            endString = String.format("Wygrana! Uzyskany czas: %d sek.", time);
            endLabel = new Label(endString);
            endVBox.getChildren().add(endLabel);
        }
        else
        {
            endString = String.format("Wygrana! Uzyskany czas: %d sek.", time);
            endLabel = new Label(endString);
            Label endLabel2 = new Label("Wpisz imię:");
            highScoreName.setMaxWidth(200);
            highScoreName.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String[] newVal = newValue.split(" ");
                    highScoreName.setText(newVal[0]);

                    if(newValue.length() > 20) {
                        String s = newValue.substring(0, 20);
                        highScoreName.setText(s);
                    }
                }
            });
            endVBox.getChildren().addAll(endLabel, endLabel2, highScoreName);
        }


        Button confirmButton = new Button("Ok");

        confirmButton.setOnAction(event -> {
            if (win && difficulty < 3)
                updateRanking(highScoreName.getText(), time);
            endDialog.close();
            gameStage.close();
            menuStage.show();
        });
        endVBox.getChildren().add(confirmButton);
        endDialog.setScene(endScene);
        endDialog.show();
    }

    // Metoda aktualizująca ranking, jeżeli został pokonany rekord
    private void updateRanking(String name, int score) {

        String filePath;

        switch (difficulty) {
            case 0:
                filePath = "rankingEasy.txt";
                break;
            case 1:
                filePath = "rankingMedium.txt";
                break;
            case 2:
                filePath = "rankingHard.txt";
                break;
      //      case 3:
      //          filePath = "rankingEasy.txt";
      //          break;
            default:
                throw new IllegalStateException("Unexpected value: " + difficulty);
        }

        String[] ranking = new String[0];

        try {
            ranking = menuManager.readFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Faliled to read file: " + filePath);
        }

        ArrayList<String> rankingArray = new ArrayList<String>();

        for (String line : ranking) {
            rankingArray.add(line);
        }


        boolean scoreUpdated = false;
        for (int i = 0; i < rankingArray.size(); i++) {
            if (rankingArray.size() == 0)
                break;
            if (rankingArray.get(i).equals(" ")) {
                String newScore = name + " " + Integer.toString(score);
                rankingArray.add(i, newScore);
                scoreUpdated = true;
                break;
            }
            String[] pos = rankingArray.get(i).split(" ");
            int time = Integer.parseInt(pos[1]);
            if (time >= score)
            {
                String newScore = name + " " + Integer.toString(score);
                rankingArray.add(i, newScore);
                scoreUpdated = true;
                break;
            }
        }

        if (rankingArray.size() == 0) {
            String newScore = name + " " + Integer.toString(score);
            rankingArray.add(newScore);
            scoreUpdated = true;
        }

        if (!scoreUpdated)
            return;



        if (rankingArray.size() > 10)
        {
            ArrayList<String> rankingArraySub = new ArrayList<String>();
            for (int i = 0; i < 10; i ++) {
                rankingArraySub.add(rankingArray.get(i));
            }
            rankingArray = rankingArraySub;
        }

        File file = new File(filePath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, false);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to create FileWriter");
        }
        assert fw != null;
        for (String line : rankingArray) {
            try {
                if (!line.equals(" ")) fw.write(line+ '\n');
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to write to file: " + filePath);
            }
        }
        try {
            fw.write(" ");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to write to file: " + filePath);
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to close FileWriter");
        }
    }

    // Ogólna metoda konfigurująca okno gry
    private void initStage() {
        gamePane = new HBox();
        gamePane.setAlignment(Pos.TOP_CENTER);
        gameScene = new Scene(gamePane, GAME_WIDTH, GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setTitle("Saper");
        gameStage.setScene(gameScene);

        scorePane = new AnchorPane();

        gameStage.setResizable(false);
        gameStage.setWidth(GAME_WIDTH);
        gameStage.setHeight(GAME_HEIGHT);

        gameStage.setOnCloseRequest(event -> {
            gameStage.close();
            menuStage.show();
            timerActive = false;
        });
    }
    // Metoda konfigurująca tekst informujący gracza o pozostałej ilości flag
    private void initFlagPane() {
        labelBox = new HBox();
        labelBox.setAlignment(Pos.CENTER_LEFT);
        labelBox.setSpacing(10);

        labelBox.setPrefWidth(200);
        labelBox.setPrefHeight(50);
        scorePane = new AnchorPane();
        scorePane.getChildren().add(labelBox);

        AnchorPane.setLeftAnchor(labelBox, 10.0);
        AnchorPane.setTopAnchor(labelBox, 10.0);

        ImageView flagIcon = new ImageView(new Image("model/resources/flag_new.png"));
        flagIcon.setFitWidth(50);
        flagIcon.setPreserveRatio(true);

        //Label flags = new Label("Flagi: ");
        //flags.setFont(Font.loadFont(Main.class.getClassLoader().getResourceAsStream(FONT_PATH), 20));
        //flags.setTextFill(Color.WHITE);

        flags = mines;

        Label flags2 = new Label(Integer.toString(this.flags));
        flags2.setFont(Font.loadFont(Main.class.getClassLoader().getResourceAsStream(FONT_PATH), 30));
        flags2.setTextFill(Color.BLACK);

        labelBox.getChildren().addAll(flagIcon, flags2);
        gamePane.getChildren().add(scorePane);
    }

    // Metoda inicjalizująca timer, pilnujący ilość czasu spędzoną podczas gry
    public void initTimer() {
        timeBox = new HBox();
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        timeBox.setSpacing(10);
        timeBox.setPrefWidth(200);
        timeBox.setPrefHeight(50);

        timePane = new AnchorPane();
        timePane.getChildren().add(timeBox);
        AnchorPane.setRightAnchor(timeBox, 10.0);
        AnchorPane.setTopAnchor(timeBox, 10.0);

        Label time = new Label("Czas:");
        time.setFont(Font.loadFont(Main.class.getClassLoader().getResourceAsStream(FONT_PATH), 20));
        time.setTextFill(Color.BLACK);

        Label time2 = new Label("0000");
        time2.setPrefWidth(120);
        time2.setFont(Font.loadFont(Main.class.getClassLoader().getResourceAsStream(FONT_PATH), 30));
        time2.setTextFill(Color.BLACK);

        timeBox.getChildren().addAll(time, time2);
        gamePane.getChildren().add(timePane);

    }

    // Metoda uruchamiająca i aktualizująca timer w oddzielnym wątku
    public void startTimer() {
        timerActive = true;
        long start = System.nanoTime();
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                while(timerActive) {
                    long stop = System.nanoTime();
                    int elapsedTime = (int) Math.floor((stop - start) / 1000000000.0);
                    if (elapsedTime == 9999)
                        timerActive = false;
                    time = elapsedTime;
                    Label timeLabel = (Label) timeBox.getChildren().get(1);
                    final String elapsedString;
                    elapsedString = String.format("%04d", elapsedTime);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            timeLabel.setText(elapsedString);
                        }
                    });
                    Thread.sleep(100);
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    // Metoda konfigurująca pole gry - rozlosowanie położenia min
    // Algorytm nie pozwala na położenie miny w pobliżu pierwszego ruchu gracza, aby ograniczyć ilość zgadywania
    // położenia min
    public void initBoard() {
        Random rand = new Random();
        for (int i = 0; i < mines; i++) {
            int randx =  rand.nextInt(mineField.length);
            int randy = rand.nextInt(mineField[randx].length);

            if (!mineField[randx][randy].getMine() && !mineField[randx][randy].getActivated() &&
                    !playerNearby(randx, randy))
            {
                mineField[randx][randy].setMine(true);
                if (randx > 0 && randy > 0)
                    mineField[randx-1][randy-1].setMinesNearby(mineField[randx-1][randy-1].getMinesNearby() + 1);
                if (randy > 0)
                    mineField[randx][randy-1].setMinesNearby(mineField[randx][randy-1].getMinesNearby() + 1);
                if (randx > 0)
                    mineField[randx-1][randy].setMinesNearby(mineField[randx-1][randy].getMinesNearby() + 1);
                if (randx < mineField.length-1 && randy < mineField[randx].length-1)
                    mineField[randx+1][randy+1].setMinesNearby(mineField[randx+1][randy+1].getMinesNearby() + 1);
                if (randx < mineField.length - 1)
                    mineField[randx+1][randy].setMinesNearby(mineField[randx+1][randy].getMinesNearby() + 1);
                if (randy < mineField[randx].length - 1)
                    mineField[randx][randy+1].setMinesNearby(mineField[randx][randy+1].getMinesNearby() + 1);
                if (randx < mineField.length - 1 && randy > 0)
                    mineField[randx+1][randy-1].setMinesNearby(mineField[randx+1][randy-1].getMinesNearby() + 1);
                if (randx > 0 && randy < mineField[randx].length - 1)
                    mineField[randx-1][randy+1].setMinesNearby(mineField[randx-1][randy+1].getMinesNearby() + 1);
            }
            else
                i--;

        }
    }

    // Metoda aktywująca polbiskie pola, wywoływana w przypadku uruchomienia pola, w którego pobliżu nie znajduje się
    // żadna mina
    // Algorytm działa rekurencyjnie, tzn. aktywuje się ponownie przy znalezieniu kolejnego pustego pola w trakcie
    // aktywacji pobliskich pól
    public void activateNearbyFields(int posx, int posy) {
        if (posx > 0 && posy > 0) {
            if (!mineField[posx - 1][posy - 1].getActivated()) {
                mineField[posx - 1][posy - 1].checkField();
                if (mineField[posx - 1][posy - 1].getMinesNearby() == 0)
                        activateNearbyFields(posx - 1, posy - 1);
            }
        }
        if (posy > 0) {
            if (!mineField[posx][posy - 1].getActivated()) {
                mineField[posx][posy - 1].checkField();
                if (mineField[posx][posy - 1].getMinesNearby() == 0)
                    activateNearbyFields(posx, posy - 1);
            }
        }
        if (posx > 0) {
            if (!mineField[posx - 1][posy].getActivated()) {
                mineField[posx - 1][posy].checkField();
                if (mineField[posx - 1][posy].getMinesNearby() == 0)
                    activateNearbyFields(posx - 1, posy);
            }
        }
        if (posx < mineField.length-1 && posy < mineField[posx].length-1) {
            if (!mineField[posx + 1][posy + 1].getActivated()) {
                mineField[posx + 1][posy + 1].checkField();
                if (mineField[posx + 1][posy + 1].getMinesNearby() == 0)
                    activateNearbyFields(posx + 1, posy + 1);
            }
        }
        if (posx < mineField.length - 1) {
            if (!mineField[posx + 1][posy].getActivated()) {
                mineField[posx + 1][posy].checkField();
                if (mineField[posx + 1][posy].getMinesNearby() == 0)
                    activateNearbyFields(posx + 1, posy);
            }
        }
        if (posy < mineField[posx].length - 1) {
            if (!mineField[posx][posy + 1].getActivated()) {
                mineField[posx][posy + 1].checkField();
                if (mineField[posx][posy + 1].getMinesNearby() == 0)
                    activateNearbyFields(posx, posy + 1);
            }
        }
        if (posx < mineField.length - 1 && posy > 0) {
            if (!mineField[posx + 1][posy - 1].getActivated()) {
                mineField[posx + 1][posy - 1].checkField();
                if (mineField[posx + 1][posy - 1].getMinesNearby() == 0)
                    activateNearbyFields(posx + 1, posy - 1);
            }
        }
        if (posx > 0 && posy < mineField[posx].length - 1) {
            if (!mineField[posx - 1][posy + 1].getActivated()) {
                mineField[posx - 1][posy + 1].checkField();
                if (mineField[posx - 1][posy + 1].getMinesNearby() == 0)
                    activateNearbyFields(posx - 1, posy + 1);
            }
        }
    }

    // Metoda analizująca czy gracz wykonał ruch w pobliżu wskazanych współrzędnych
    private boolean playerNearby(int posx, int posy) {
        if (posx > 0 && posy > 0 && posx < mineField.length-1 && posy < mineField[posx].length-1) {
            if (mineField[posx - 1][posy - 1].getActivated() || mineField[posx][posy - 1].getActivated()
                    || mineField[posx + 1][posy - 1].getActivated() || mineField[posx - 1][posy].getActivated()
                    || mineField[posx + 1][posy].getActivated() || mineField[posx - 1][posy + 1].getActivated()
                    || mineField[posx][posy + 1].getActivated() || mineField[posx + 1][posy + 1].getActivated())
                return true;
            else return false;
        }
        else {
            if (posx > 0 && posy > 0)
                if (mineField[posx - 1][posy - 1].getActivated())
                    return true;
            if (posy > 0)
                if (mineField[posx][posy - 1].getActivated())
                    return true;
            if (posx > 0)
                if (mineField[posx - 1][posy].getActivated())
                    return true;
            if (posx < mineField.length-1 && posy < mineField[posx].length-1)
                if (mineField[posx + 1][posy + 1].getActivated())
                    return true;
            if (posx < mineField.length - 1)
                if (mineField[posx + 1][posy].getActivated())
                    return true;
            if (posy < mineField[posx].length - 1)
                if (mineField[posx][posy + 1].getActivated())
                    return true;
            if (posx < mineField.length - 1 && posy > 0)
                if (mineField[posx + 1][posy - 1].getActivated())
                    return true;
            if (posx > 0 && posy < mineField[posx].length - 1)
                if (mineField[posx - 1][posy + 1].getActivated())
                    return true;
        }
        return false;
    }

    // Metoda konfigurująca nową grę, w tym: uruchamia elementy interfejsu graficznego, inicjalizuje pole gry, wczytuje
    // zasoby gry, oraz uruchamia animację startową pojawiania się pola
    public void createNewGame(Stage menuStage, int boardX, int boardY, int mines) {
        this.mines = mines;
        initFlagPane();
        boardPane = new GridPane();
        boardPane.setAlignment(Pos.CENTER);
        boardPane.setPrefSize(primaryScreenBounds.getWidth() / 1.75,
                primaryScreenBounds.getHeight() / 1.5);
        //boardPane.setPadding(new Insets(5,5,5,5));
        gamePane.getChildren().add(boardPane);
        initTimer();
        mineField = new MineField[boardX][boardY];

        int fieldSize;
        if (boardX > boardY)
            fieldSize = 750 / boardX;
        else
            fieldSize = 750 / boardY;

        flagImage = new Image("model/resources/flag_new.png", fieldSize, fieldSize, false,
                true, true);
        mineExpImage = new Image("model/resources/mine_exp_alt.png", fieldSize, fieldSize, false,
                true, true);
        mineImage = new Image("model/resources/mine_new.png", fieldSize, fieldSize, false,
                true, true);
        mineFlagImage = new Image("model/resources/mine_flagged_new.png", fieldSize, fieldSize, false,
                true, true);
        for (int i = 0; i < mineField.length; i++) {
            for (int j = 0; j < mineField[i].length; j++) {
                mineField[i][j] = new MineField(i, j, fieldSize, this);
                boardPane.add(mineField[i][j], i, j);
                mineField[i][j].setVisible(false);
            }
        }
        this.menuStage = menuStage;
        this.menuStage.hide();
        gameStage.show();
        startAnimation();
    }

    // Metoda obsługująca start animacji pojawiania się pola gry
    public void startAnimation() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                for (MineField[] mineFields : mineField) {
                    for (MineField field : mineFields) {
                        field.setMouseTransparent(true);
                        FadeTransition ft = new FadeTransition(Duration.millis(500), field);
                        ft.setFromValue(0.0);
                        ft.setToValue(1.0);
                        Platform.runLater(() -> {
                            field.setVisible(true);
                            ft.play();
                        });

                    }
                    Thread.sleep(50);
                }
                for (MineField[] mineFields : mineField) {
                    for (MineField field : mineFields) {
                            field.setMouseTransparent(false);
                        }
                    }
                return null;
            }
        };
        new Thread(task).start();
    }

    // Metoda tworząca tło - nieużywana z powodu braku pliku
    private void createBackground() {
        Image backgroundImage = new Image("view/resources/gameBackground.jpg", GAME_WIDTH, GAME_HEIGHT,
                false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, null);
        gamePane.setBackground(new Background(background));
    }
}
