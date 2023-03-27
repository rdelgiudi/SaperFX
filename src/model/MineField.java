package model;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import view.GameViewManager;

import java.util.ArrayList;
import java.util.List;

// Klasa dziedzicząca po Button - obsługuje czynności związane z pojedynczym polem na planszy
public class MineField extends Button {

    final static String BUTTON_STYLE = "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: cornflowerblue;";
    final static String BUTTON_ACTIVATED = "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: gainsboro; fx-background-color: gray;";
    final static String MINE_STYLE =  "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: red;";
    final static String MINE_PATH = "model/resources/mine_micro.png";
    final static String MINE_EXPLODED_STYLE =  "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: red;";
    final static String MINE_FLAGGED_STYLE = "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: darkcyan;";
    final static String FLAG_STYLE = "-fx-faint-focus-color: transparent; -fx-focus-color: transparent;" +
            " -fx-border-color: black;";
    //-fx-background-image: url('model/resources/flag_mini.png'); fx-background-repeat: no-repeat;
    // fx-background-position: center" ;

    private boolean isMine;
    private boolean isFlag;
    private boolean isActivated;
    private int minesNearby;

    private final int posX, posY;
    private final GameViewManager gameViewManager;

    // Konstruktor klasy MineField
    public MineField(int x, int y, int size, GameViewManager manager) {
        posX = x;
        posY = y;
        gameViewManager = manager;
        setStyle(BUTTON_STYLE);
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        setPadding(Insets.EMPTY);
        isMine = false;
        isFlag = false;
        isActivated = false;
        minesNearby = 0;
        initListeners();
    }

    // Metoda sprawdzająca zawartość pola
    public void checkField() {
        if (isMine && isFlag)
        {
            String setString = MINE_FLAGGED_STYLE;
            setStyle(setString);
            setBackgroundIcon(gameViewManager.getMineFlagImage());
            return;
        }
        else if (isFlag)
            return;
        setMouseTransparent(true);
        isActivated = true;
        List<Color> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.ORANGE);
        colors.add(Color.DARKBLUE);
        colors.add(Color.DARKRED);
        colors.add(Color.PURPLE);
        colors.add(Color.OLIVE);
        colors.add(Color.DARKBLUE);

        if (isMine) {
            String setString = MINE_STYLE;
            setStyle(setString);
            setBackgroundIcon(gameViewManager.getMineImage());
        }
        else if (minesNearby == 0)
            setStyle(BUTTON_ACTIVATED);
        else if (minesNearby < 9) {
            setText(Integer.toString(minesNearby));
            setTextAlignment(TextAlignment.CENTER);
            setTextFill(colors.get(minesNearby-1));
            setStyle(BUTTON_ACTIVATED);
            setFont(Font.font("Verdana", FontWeight.BOLD, getWidth()*0.7));
        }
        else {
            Label debugLabel = (Label) gameViewManager.getLabelBox().getChildren().get(1);
            debugLabel.setText("!! minesNearby większe od 8 !!");
            debugLabel.setTextFill(Paint.valueOf("red"));
        }
    }

    //Klasa inicjalizująca listenery - obsługują różne kliknięcia myszą
    private void initListeners() {
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !isFlag) {
                setStyle(BUTTON_ACTIVATED);
                isActivated = true;
                setMouseTransparent(true);
                if (!gameViewManager.getFieldComplete()) {
                    gameViewManager.setFieldComplete(true);
                    gameViewManager.initBoard();
                    gameViewManager.startTimer();

                }
                if (isMine) {
                    String setString = MINE_EXPLODED_STYLE;
                    setStyle(setString);
                    setBackgroundIcon(gameViewManager.getMineExpImage());
                    gameViewManager.gameOver(false);
                }
                else {
                    checkField();
                    if (minesNearby == 0)
                        gameViewManager.activateNearbyFields(posX, posY);
                    int fieldsActive = 0;
                    for (int i = 0; i < gameViewManager.getMineField().length; i++) {
                        for (int j = 0; j < gameViewManager.getMineField()[i].length; j++) {
                            if (gameViewManager.getMineField()[i][j].getActivated())
                                fieldsActive++;
                        }
                    }
                    if (fieldsActive == (gameViewManager.getMineField().length
                            * gameViewManager.getMineField()[0].length) - gameViewManager.getMines())
                        gameViewManager.gameOver(true);
                }


            }
            if (event.getButton() == MouseButton.SECONDARY && gameViewManager.getFieldComplete()) {
                if (isFlag) {
                    setStyle(BUTTON_STYLE);
                    setBackgroundIcon(null);
                    isFlag = false;
                    gameViewManager.setFlags(gameViewManager.getFlags() + 1);
                    Label mineLabel = (Label) gameViewManager.getLabelBox().getChildren().get(1);
                    mineLabel.setText(Integer.toString(gameViewManager.getFlags()));
                }
                else if (gameViewManager.getFlags() > 0){
                    String setString = FLAG_STYLE;
                    setStyle(setString);
                    setBackgroundIcon(gameViewManager.getFlagImage());
                    isFlag = true;
                    gameViewManager.setFlags(gameViewManager.getFlags() - 1);
                    Label mineLabel = (Label) gameViewManager.getLabelBox().getChildren().get(1);
                    mineLabel.setText(Integer.toString(gameViewManager.getFlags()));
                    if (gameViewManager.getFlags() == 0)
                    {
                        int minesFound = 0;
                        for (int i = 0; i < gameViewManager.getMineField().length; i++) {
                            for (int j = 0; j < gameViewManager.getMineField()[i].length; j++) {
                                if (gameViewManager.getMineField()[i][j].getFlag()
                                        && gameViewManager.getMineField()[i][j].getMine())
                                    minesFound++;
                            }
                        }
                        if (minesFound == gameViewManager.getMines())
                            gameViewManager.gameOver(true);
                    }
                }
            }
        });
    }

    // Metoda obsługująca zmianę tła przycisku, aby zasymbolizować postawienie flagi lub obecność miny
    private void setBackgroundIcon(Image image) {
        if (image == null)
        {
            setGraphic(null);
            return;
        }
        ImageView view = new ImageView(image);
        view.setFitHeight(getHeight());
        view.setPreserveRatio(true);
        setGraphic(view);
    }
    // Gettery i settery - początek
    public boolean getMine() {
        return isMine;
    }
    public void setMine(boolean flag) {
        isMine = flag;
    }
    public boolean getFlag() {
        return isFlag;
    }
    public void setFlag(boolean flag) {
        isFlag = flag;
    }
    public boolean getActivated() {
        return isActivated;
    }
    public void setActivated(boolean flag) {
        isActivated = flag;
    }
    public int getMinesNearby() {
        return minesNearby;
    }
    public void setMinesNearby(int val) {
        minesNearby = val;
    }
    // Gettery i settery - koniec
}
