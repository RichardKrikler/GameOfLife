package gameoflife;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * GUI for the Game Of Life
 *
 * @author Richard Krikler
 */
public class GameGui extends Application {
    /**
     * Width of the main GUI
     */
    final int GUI_WIDTH = 1000;

    /**
     * Height of the main GUI
     */
    final int GUI_HEIGHT = 550;

    /**
     * Default size per cell of the play field
     */
    final int DEFAULT_SIZE_PER_CELL = 16;

    /**
     * Variable size per cell of the play field
     */
    int sizePerCell = DEFAULT_SIZE_PER_CELL;

    /**
     * Canvas displaying the play field
     */
    Canvas gameCanvas;

    /**
     * GraphicsContext for drawings on the canvas
     */
    GraphicsContext gc;

    /**
     * ExecutorService for periodically getting the play field to the next generation
     */
    ScheduledExecutorService executor;

    /**
     * Boolean value, which is true if the game should stop when the window is minimized into the taskbar
     * The value is equals to the checkbox value (stopIfMinimizedCB)
     */
    boolean stopIfMinimized;

    /**
     * Store the path to the folder, which contains the presets
     */
    final String PRESET_PATH = "resources/PlayFieldPresets";


    /**
     * Main method launching the main GUI.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Start the main GUI.
     *
     * @param stage top level JavaFX container for the main GUI
     */
    @Override
    public void start(Stage stage) {
        // ------------------ PlayField Object ------------------
        PlayField playField = new PlayField(15, 15);
        playField.setGameSpeed(1);
        playField.setReanimateRule(3);
        playField.setKeepLifeRule(2, 3);
        stopIfMinimized = true;

        // ------------------ PresetManager ------------------
        PresetManager presetManager = new PresetManager(stage, PRESET_PATH);

        // ------------------ GameGui Layout ------------------
        // Initialising a horizontal SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);


        BorderPane borderPaneLeft = new BorderPane();
        ScrollPane scrollPaneRight = new ScrollPane();
        // Only show the scroll bars if they are needed
        scrollPaneRight.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPaneRight.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        // ------------------ Game Canvas ------------------

        ScrollPane scrollPaneLeft = new ScrollPane();
        // Always show the scroll bars
        scrollPaneLeft.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPaneLeft.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        gameCanvas = new Canvas(playField.getDimensionX() * sizePerCell, playField.getDimensionY() * sizePerCell);
        // Canvas inside of a StackPane
        StackPane stackPane = new StackPane(gameCanvas);

        // StackPane inside of a ScrollPane
        scrollPaneLeft.setContent(stackPane);
        scrollPaneLeft.setFitToWidth(true);
        scrollPaneLeft.setFitToHeight(true);


        gc = gameCanvas.getGraphicsContext2D();
        drawPlayField(playField);

        // ------------------


        // Zoom Slider
        Slider zoomSlider = new Slider();
        zoomSlider.setMin(0.1);
        zoomSlider.setMax(2);
        zoomSlider.setValue(1);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.1);
        zoomSlider.setMinorTickCount(1);
        zoomSlider.setBlockIncrement(0.5);

        // BorderPane positioning
        borderPaneLeft.setCenter(scrollPaneLeft);
        borderPaneLeft.setBottom(zoomSlider);
        BorderPane.setMargin(zoomSlider, new Insets(10));


        // ------------------ Game Settings ------------------

        GridPane settingsGrid = new GridPane();
        // settingsGrid.setGridLinesVisible(true);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33);
        settingsGrid.getColumnConstraints().addAll(col1, col2, col3);


        // "Settings" Header Label
        Label settingsLabel = new Label("Settings");
        settingsLabel.setStyle("-fx-font-weight: bold");
        GridPane.setHalignment(settingsLabel, HPos.CENTER);
        settingsGrid.add(settingsLabel, 1, 0);

        // Current Generation
        Label curGenLabel = new Label("Current Generation:");
        settingsGrid.add(curGenLabel, 0, 1);
        GridPane.setColumnSpan(curGenLabel, 2);

        Label curGenNumLabel = new Label(Integer.toString(playField.getGeneration()));
        GridPane.setHalignment(curGenNumLabel, HPos.CENTER);
        settingsGrid.add(curGenNumLabel, 2, 1);

        // Currently Living cells
        Label curLivingLabel = new Label("Living Cells:");
        settingsGrid.add(curLivingLabel, 0, 2);
        GridPane.setColumnSpan(curLivingLabel, 2);

        Label curLivingNumLabel = new Label(Integer.toString(playField.getLivingCells()));
        GridPane.setHalignment(curLivingNumLabel, HPos.CENTER);
        settingsGrid.add(curLivingNumLabel, 2, 2);


        // PlayField Dimensions
        Label playFieldLabel = new Label("Set Play Field Dimensions:");
        settingsGrid.add(playFieldLabel, 0, 3);
        GridPane.setColumnSpan(playFieldLabel, 4);

        TextField xDimTf = new TextField(Integer.toString(playField.getDimensionX()));
        xDimTf.setPromptText("X-Dim");
        xDimTf.setMaxWidth(55);
        settingsGrid.add(xDimTf, 0, 4);

        TextField yDimTf = new TextField(Integer.toString(playField.getDimensionY()));
        yDimTf.setPromptText("Y-Dim");
        yDimTf.setMaxWidth(55);
        settingsGrid.add(yDimTf, 1, 4);

        Button setDimensionBt = new Button("Set");
        setDimensionBt.setTooltip(new Tooltip("Set Dimension"));
        GridPane.setHalignment(setDimensionBt, HPos.CENTER);
        settingsGrid.add(setDimensionBt, 2, 4);


        // Game Controls
        Label gameControlLabel = new Label("Game Controls:");
        settingsGrid.add(gameControlLabel, 0, 5);
        GridPane.setColumnSpan(gameControlLabel, 4);

        Button playBt = new Button("▶");
        playBt.setTooltip(new Tooltip("Play"));
        GridPane.setHalignment(playBt, HPos.CENTER);
        settingsGrid.add(playBt, 0, 6);

        Button pauseBt = new Button("⏸");
        pauseBt.setTooltip(new Tooltip("Pause"));
        GridPane.setHalignment(pauseBt, HPos.CENTER);
        settingsGrid.add(pauseBt, 1, 6);

        Button stepBackBt = new Button("⏮");
        stepBackBt.setTooltip(new Tooltip("1 Step Back"));
        GridPane.setHalignment(stepBackBt, HPos.CENTER);
        settingsGrid.add(stepBackBt, 2, 6);


        Button resetBt = new Button("⏹");
        resetBt.setTooltip(new Tooltip("Reset"));
        GridPane.setHalignment(resetBt, HPos.CENTER);
        settingsGrid.add(resetBt, 0, 7);

        Button resetToStartBt = new Button("⭯");
        resetToStartBt.setTooltip(new Tooltip("Reset to Start"));
        GridPane.setHalignment(resetToStartBt, HPos.CENTER);
        settingsGrid.add(resetToStartBt, 1, 7);

        Button stepForwardBt = new Button("⏭");
        stepForwardBt.setTooltip(new Tooltip("1 Step Forward"));
        GridPane.setHalignment(stepForwardBt, HPos.CENTER);
        settingsGrid.add(stepForwardBt, 2, 7);


        // Got to step x
        Label goToLabel = new Label("Go to:");
        settingsGrid.add(goToLabel, 0, 8);

        TextField goToTf = new TextField();
        goToTf.setPromptText("Gen");
        goToTf.setMaxWidth(55);
        settingsGrid.add(goToTf, 1, 8);

        Button goToBt = new Button("X");
        goToBt.setTooltip(new Tooltip("Got to Generation"));
        GridPane.setHalignment(goToBt, HPos.CENTER);
        settingsGrid.add(goToBt, 2, 8);


        // Game Speed Control
        Label gameSpeedLabel = new Label("Game Speed (in s):");
        settingsGrid.add(gameSpeedLabel, 0, 9);
        GridPane.setColumnSpan(gameSpeedLabel, 3);

        TextField speedTf = new TextField(Float.toString(playField.getGameSpeed()));
        speedTf.setPromptText("Speed");
        speedTf.setMaxWidth(55);
        settingsGrid.add(speedTf, 0, 10);

        Button setSpeedBt = new Button("X");
        setSpeedBt.setTooltip(new Tooltip("Set Game Speed"));
        GridPane.setHalignment(setSpeedBt, HPos.CENTER);
        settingsGrid.add(setSpeedBt, 1, 10);


        // Change Game Rules
        Label gameRulesLabel = new Label("Game Rules:");
        settingsGrid.add(gameRulesLabel, 0, 11);
        GridPane.setColumnSpan(gameRulesLabel, 2);

        Button gameRulesBt = new Button("X");
        gameRulesBt.setTooltip(new Tooltip("Set Game Rules"));
        GridPane.setHalignment(gameRulesBt, HPos.CENTER);
        settingsGrid.add(gameRulesBt, 2, 11);

        Label reanimateRuleLabel = new Label("Reanimate Rule");
        settingsGrid.add(reanimateRuleLabel, 0, 12);
        GridPane.setColumnSpan(reanimateRuleLabel, 2);

        TextField reanimateRuleTf = new TextField(playField.getReanimateRule());
        reanimateRuleTf.setPromptText("x,x");
        reanimateRuleTf.setMaxWidth(55);
        settingsGrid.add(reanimateRuleTf, 2, 12);

        Label keepLifeRuleLabel = new Label("Keep Alive Rule");
        settingsGrid.add(keepLifeRuleLabel, 0, 13);
        GridPane.setColumnSpan(keepLifeRuleLabel, 2);

        TextField keepLifeRuleTf = new TextField(playField.getKeepLifeRule());
        keepLifeRuleTf.setPromptText("x,x");
        keepLifeRuleTf.setMaxWidth(55);
        settingsGrid.add(keepLifeRuleTf, 2, 13);


        // Presets
        Label presetsLabel = new Label("Presets:");
        settingsGrid.add(presetsLabel, 0, 14);
        GridPane.setColumnSpan(presetsLabel, 3);

        ComboBox<String> presetBox = new ComboBox<>();
        presetBox.setItems(presetManager.getObservableList());
        presetBox.setMinWidth(150);
        settingsGrid.add(presetBox, 0, 15);
        GridPane.setColumnSpan(presetBox, 3);
        presetBox.prefWidth(settingsGrid.getWidth());

        Button loadPresetBt = new Button("Load");
        loadPresetBt.setTooltip(new Tooltip("Load a Preset"));
        settingsGrid.add(loadPresetBt, 0, 16);

        Button savePresetBt = new Button("Save");
        savePresetBt.setTooltip(new Tooltip("Save a Preset"));
        settingsGrid.add(savePresetBt, 1, 16);


        // Game Analysis
        Label analysisLabel = new Label("Analysis:");
        settingsGrid.add(analysisLabel, 0, 17);
        GridPane.setColumnSpan(analysisLabel, 3);

        Button analysisBt = new Button("Show Analysis");
        analysisBt.setTooltip(new Tooltip("Show Game Analysis"));
        GridPane.setColumnSpan(analysisBt, 2);
        settingsGrid.add(analysisBt, 0, 18);


        // Stop game if the main window is minimized into the taskbar
        Label stopIfMinimizedLabel = new Label("Stop game if minimized:");
        stopIfMinimizedLabel.setTooltip(new Tooltip("Stop the game if the window is minimized into the taskbar."));
        settingsGrid.add(stopIfMinimizedLabel, 0, 19);
        GridPane.setColumnSpan(stopIfMinimizedLabel, 3);

        CheckBox stopIfMinimizedCB = new CheckBox();
        stopIfMinimizedCB.setSelected(stopIfMinimized);
        GridPane.setHalignment(stopIfMinimizedCB, HPos.CENTER);
        settingsGrid.add(stopIfMinimizedCB, 2, 19);


        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(10));
        scrollPaneRight.setContent(settingsGrid);

        // ------------------


        // Disable the automatic resizing of the right ScrollPane, when the window would be resized
        SplitPane.setResizableWithParent(scrollPaneRight, Boolean.FALSE);

        // Add the ScrollPanes as Items to the SplitPane
        splitPane.getItems().addAll(borderPaneLeft, scrollPaneRight);

        // Set the position of the horizontal Divider of the SplitPane
        splitPane.setPrefSize(GUI_WIDTH * 0.6, GUI_WIDTH * 0.4);
        splitPane.getDividers().get(0).setPosition(0.60);


        Scene scene = new Scene(splitPane);
        stage.setScene(scene);
        stage.setTitle("Game Of Life");
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/GameOfLife.png")));
        stage.setResizable(true);

        stage.setWidth(GUI_WIDTH);
        stage.setHeight(GUI_HEIGHT);

        stage.show();

        // ------------------ Event Handlers ------------------

        // Change the size of the play field to the values of the text fields
        Pattern integerPat = Pattern.compile("^\\d+$");
        setDimensionBt.setOnAction(e -> {
            String xDim = xDimTf.getText();
            String yDim = yDimTf.getText();

            boolean validXDim = integerPat.matcher(xDim).matches();
            boolean validYDim = integerPat.matcher(yDim).matches();

            // If X dimension is invalid -> Display Error Message
            if (!validXDim) {
                errorDialog(stage, "Input Error", "The X dimension (\"" + xDim + "\") is not valid!", "Only integers are allowed.");
                xDimTf.setText(Integer.toString(playField.getDimensionX()));
            }

            // If Y dimension is invalid -> Display Error Message
            if (!validYDim) {
                errorDialog(stage, "Input Error", "The Y dimension (\"" + yDim + "\") is not valid!", "Only integers are allowed.");
                yDimTf.setText(Integer.toString(playField.getDimensionY()));
            }

            // If both inputs are valid -> set the dimensions of the playground and the size of the canvas
            if (validXDim && validYDim) {
                pauseGame();
                playField.setSize(Integer.parseInt(xDim), Integer.parseInt(yDim));
                drawPlayField(playField);
                curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
            }
        });


        // Runnable, which is periodically called from the ScheduledExecutorService, to get the play field to the next generation
        Runnable runGame = () -> {
            if (playField.stepForward()) {
                // Updating GUI elements can only be done in the Application thread
                Platform.runLater(() -> {
                    drawPlayField(playField);
                    curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                    curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
                });
            } else {
                pauseGame();
            }
        };

        // Start the game -> initialise ScheduledExecutorService
        playBt.setOnAction(e -> {
            System.out.println("Start Game");
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(runGame, 0, (long) (playField.getGameSpeed() * 1000), TimeUnit.MILLISECONDS);
        });

        // Pause the game -> Stop the ScheduledExecutorService
        pauseBt.setOnAction(e -> pauseGame());


        // Get the play field to the last generation
        stepBackBt.setOnAction(e -> {
            if (playField.stepTo(playField.getGeneration() - 1)) {
                drawPlayField(playField);
                curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
            }
        });


        // Get the play field to the next generation
        stepForwardBt.setOnAction(e -> {
            if (playField.stepForward()) {
                drawPlayField(playField);
                curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
            }
        });


        // Go the a specific Generation
        goToBt.setOnAction(e -> {
            String gen = goToTf.getText();
            boolean validGen = integerPat.matcher(gen).matches();

            if (validGen) {
                // If generation is valid -> go to the generation x
                if (playField.stepTo(Integer.parseInt(gen))) {
                    drawPlayField(playField);
                    curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                    curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
                    goToTf.setText(Integer.toString(playField.getGeneration()));
                }
            } else {
                // If generation is invalid -> Display Error Message
                errorDialog(stage, "Input Error", "The Generation (\"" + gen + "\") is not valid!", "Only integers are allowed.");
                goToTf.setText("");
            }
        });


        // Reset the complete game and display the changes
        resetBt.setOnAction(e -> {
            pauseGame();
            playField.setSize(playField.getDimensionX(), playField.getDimensionY());
            playField.resetGeneration();

            drawPlayField(playField);
            curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
        });


        // Reset the play field to the state of generation zero
        resetToStartBt.setOnAction(e -> {
            pauseGame();
            if (playField.stepTo(0)) {
                drawPlayField(playField);
                curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
            }
        });


        // Change the game speed of the play field to the value of the text field
        Pattern gameSpeedPat = Pattern.compile("^([1-9]\\d*(\\.\\d*)?)|(\\d*\\.\\d{0,2}[1-9]0*)$");
        setSpeedBt.setOnAction(e -> {
            String gameSpeed = speedTf.getText();

            // If Game Speed is valid -> pause the game and set the game speed
            // Otherwise -> Display Error Message
            if (gameSpeedPat.matcher(gameSpeed).matches()) {
                pauseGame();
                playField.setGameSpeed(Float.parseFloat(gameSpeed));
            } else {
                errorDialog(stage, "Input Error", "The Game Speed (\"" + gameSpeed + "\") is not valid!", "Only integers or floating point values are allowed. " +
                        "There is a maximum of 3 decimal points. The values are interpreted in seconds. Minimum value = 0.001");
                speedTf.setText(Float.toString(playField.getGameSpeed()));
            }
        });


        // Change the game rules of the play field to the values of the text fields
        Pattern gameRulePat = Pattern.compile("^([0-8],)*[0-8]$");
        gameRulesBt.setOnAction(e -> {
            String reanimateRule = reanimateRuleTf.getText();
            String keepLifeRule = keepLifeRuleTf.getText();

            boolean validReanimateRule = gameRulePat.matcher(reanimateRule).matches();
            boolean validKeepLifeRule = gameRulePat.matcher(keepLifeRule).matches();

            String errorExplanation = "The rule is valid if it contains one or more integers (from 0 to 8), separated with a comma (\",\").";

            // If Reanimate Rule is invalid -> Display Error Message
            if (!validReanimateRule) {
                errorDialog(stage, "Input Error", "The Reanimate Rule (\"" + reanimateRule + "\") is not valid!", errorExplanation);
                reanimateRuleTf.setText(playField.getReanimateRule());
            }

            // If Keep Alive Rule is invalid -> Display Error Message
            if (!validKeepLifeRule) {
                errorDialog(stage, "Input Error", "The Keep Alive Rule (\"" + keepLifeRule + "\") is not valid!", errorExplanation);
                keepLifeRuleTf.setText(playField.getKeepLifeRule());
            }

            // If both inputs are valid -> set the game rules to the input values
            if (validReanimateRule & validKeepLifeRule) {
                playField.setReanimateRule(reanimateRule);
                playField.setKeepLifeRule(keepLifeRule);
            }
        });


        // Load the selected preset (file) from the dropdown menu of the presetBox
        presetBox.setOnAction(e -> {
            if (!presetBox.getValue().equals("")) {
                String selectedItem = presetBox.getSelectionModel().getSelectedItem();
                presetBox.getSelectionModel().select(0);

                int[][] newPlayField = presetManager.loadPreset(selectedItem);
                updatePlayField(newPlayField, playField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
            }
        });

        // Load a preset into the play field
        loadPresetBt.setOnAction(e -> {
            int[][] newPlayField = presetManager.loadPreset();
            updatePlayField(newPlayField, playField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
        });

        // Save the current play field to a CSV file
        savePresetBt.setOnAction(e -> {
            if (presetManager.savePreset(playField)) {
                presetBox.setItems(presetManager.getObservableList());
            }
        });


        analysisBt.setOnAction(e -> System.out.println("Show Analysis"));


        // Zoom Slider for zooming into the game Canvas
        zoomSlider.valueProperty().addListener(e -> {
            // Change the variable size per cell according to the value of the zoom slider
            sizePerCell = (int) (DEFAULT_SIZE_PER_CELL * zoomSlider.getValue());
            drawPlayField(playField);
        });


        // Change the value of a cell to living or dead
        gameCanvas.setOnMouseClicked(e -> {
            // Get the expected position in the array from the mouse position
            int posX = (int) ((e.getSceneX() - gameCanvas.getLayoutX()) / sizePerCell);
            int posY = (int) ((e.getSceneY() - gameCanvas.getLayoutY()) / sizePerCell);

            // If the positions are within the size of the play field
            if (posX < playField.getDimensionX() && posY < playField.getDimensionY()) {
                // If it is a living cell -> dead
                if (playField.getCell(posX, posY) == 1) {
                    playField.setCell(posX, posY, 0);
                } else {
                    playField.setCell(posX, posY, 1);
                }
            }

            drawPlayField(playField);
            curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
        });


        // Change the value of stopIfMinimized to the value of the according CheckBox (stopIfMinimizedCB)
        stopIfMinimizedCB.selectedProperty().addListener(e -> stopIfMinimized = stopIfMinimizedCB.selectedProperty().getValue());

        // Detect if the main window has been minimized into the taskbar
        // If it has and the stopIfMinimized is true -> pause the game
        stage.iconifiedProperty().addListener((ov, t, t1) -> {
            if (t1 && stopIfMinimized) {
                pauseGame();
            }
        });
    }


    /**
     * Draw the current Play Field to the Canvas of the Gui.
     *
     * @param playField PlayField Object containing the play field array
     */
    void drawPlayField(PlayField playField) {
        gameCanvas.setWidth(playField.getDimensionX() * sizePerCell);
        gameCanvas.setHeight(playField.getDimensionY() * sizePerCell);

        double canvasW = gameCanvas.getWidth();
        double canvasH = gameCanvas.getHeight();

        gc.clearRect(0, 0, canvasW, canvasH);

        // Draw Cells
        for (int y = 0; y < playField.getDimensionY(); y++) {
            for (int x = 0; x < playField.getDimensionX(); x++) {
                if (playField.getCell(x, y) == 1) {
                    gc.setFill(Color.web("98E35B"));
                    gc.fillRect(x * sizePerCell, y * sizePerCell, sizePerCell, sizePerCell);
                }
            }
        }

        // Draw Grid
        for (int x = 0; x <= playField.getDimensionX(); x++) {
            gc.strokeLine(x * sizePerCell, 0, x * sizePerCell, canvasH);
        }

        for (int y = 0; y <= playField.getDimensionY(); y++) {
            gc.strokeLine(0, y * sizePerCell, canvasW, y * sizePerCell);
        }
    }

    /**
     * Display variable Error Dialog.
     *
     * @param stage       top level JavaFX container for the main GUI
     * @param title       Title of the Error Dialog
     * @param headerText  Main Error Message
     * @param contentText Helpful Error Hint
     */
    static void errorDialog(Stage stage, String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    /**
     * Pause the game -> stop the ScheduledExecutorService
     */
    void pauseGame() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Update the play field to a new array
     *
     * @param newPlayField int[][] array which contains the new play field
     */
    void updatePlayField(int[][] newPlayField, PlayField playField, TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel) {
        if (newPlayField != null) {
            pauseGame();
            playField.setPlayField(newPlayField);
            playField.setOriginalPlayField(newPlayField);
            playField.resetGeneration();
            drawPlayField(playField);

            xDimTf.setText(Integer.toString(playField.getDimensionX()));
            yDimTf.setText(Integer.toString(playField.getDimensionY()));
            curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
        }
    }

    /**
     * Convert a String array to an int array
     *
     * @param input String array
     * @return int array
     */
    static int[] stringArToIntAr(String[] input) {
        int[] result = new int[input.length];
        for (int y = 0; y < input.length; y++) {
            result[y] = Integer.parseInt(input[y]);
        }
        return result;
    }
}
