package gameoflife;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.concurrent.ScheduledExecutorService;


/**
 * GUI for the Game Of Life
 *
 * @author Richard Krikler
 */
public class Gui extends Application {
    /**
     * Width of the main GUI
     */
    static final int GUI_WIDTH = 1000;

    /**
     * Height of the main GUI
     */
    static final int GUI_HEIGHT = 550;

    /**
     * Stage: top level JavaFX container for the main GUI
     */
    static Stage stage;

    /**
     * Default size per cell of the play field
     */
    static final int DEFAULT_SIZE_PER_CELL = 16;

    /**
     * Variable size per cell of the play field
     */
    static int sizePerCell = DEFAULT_SIZE_PER_CELL;

    /**
     * Canvas displaying the play field
     */
    static Canvas gameCanvas;

    /**
     * GraphicsContext for drawings on the canvas
     */
    static GraphicsContext gc;

    /**
     * ExecutorService for periodically getting the play field to the next generation
     */
    static ScheduledExecutorService executor;

    /**
     * Boolean value, which is true if the game should stop
     * when the window is minimized into the taskbar
     *
     * The value is equals to the checkbox value (stopIfMinimizedCB)
     */
    static boolean stopIfMinimized;

    /**
     * Store the path to the folder, which contains the presets
     */
    static final String PRESET_PATH = "resources/PlayFieldPresets";

    /**
     * Store the play field inside the PlayField Object
     */
    static PlayField playField;

    /**
     * Store the preset manager (logic for the use of presets) inside the PresetManager Object
     */
    static PresetManager presetManager;


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
        Gui.stage = stage;

        // ------------------ PlayField Object ------------------
        playField = new PlayField(15, 15);
        playField.setGameSpeed(1);
        playField.setReanimateRule(3);
        playField.setKeepLifeRule(2, 3);
        stopIfMinimized = true;

        // ------------------ PresetManager ------------------
        presetManager = new PresetManager(stage, PRESET_PATH);

        // ------------------ GUI Layout ------------------
        // Initialising a horizontal SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);


        BorderPane borderPaneLeft = new BorderPane();
        ScrollPane scrollPaneRight = new ScrollPane();
        // Only show the scroll bars if they are needed
        scrollPaneRight.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPaneRight.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);


        // ------------------ Game Header Label ------------------
        Label gameLabel = new Label("Game Of Life");
        gameLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));

        StackPane gameHeaderStackPane = new StackPane();
        gameHeaderStackPane.getChildren().add(gameLabel);

        borderPaneLeft.setTop(gameHeaderStackPane);

        // ------------------ Game Canvas ------------------
        ScrollPane scrollPaneLeft = new ScrollPane();
        // Always show the scroll bars
        scrollPaneLeft.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPaneLeft.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        gameCanvas = new Canvas(
                playField.getDimensionX() * sizePerCell,
                playField.getDimensionY() * sizePerCell);

        // Canvas inside of a StackPane
        StackPane stackPane = new StackPane(gameCanvas);

        // StackPane inside of a ScrollPane
        scrollPaneLeft.setContent(stackPane);
        scrollPaneLeft.setFitToWidth(true);
        scrollPaneLeft.setFitToHeight(true);


        gc = gameCanvas.getGraphicsContext2D();
        GuiLogic.drawPlayField();

        // ------------------ Zoom Slider ------------------

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

        Button randomPlacementBt = new Button("Random Placement");
        randomPlacementBt.setTooltip(new Tooltip("Place living cells randomly"));
        settingsGrid.add(randomPlacementBt, 0, 17);
        GridPane.setColumnSpan(randomPlacementBt, 3);


        // Game Analysis
        Label analysisLabel = new Label("Analysis:");
        settingsGrid.add(analysisLabel, 0, 18);
        GridPane.setColumnSpan(analysisLabel, 3);

        Button analysisBt = new Button("Show Analysis");
        analysisBt.setTooltip(new Tooltip("Show Game Analysis"));
        GridPane.setColumnSpan(analysisBt, 2);
        settingsGrid.add(analysisBt, 0, 19);


        // Stop game if the main window is minimized into the taskbar
        Label stopIfMinimizedLabel = new Label("Stop game if minimized:");
        stopIfMinimizedLabel.setTooltip(new Tooltip("Stop the game if the window is minimized into the taskbar."));
        settingsGrid.add(stopIfMinimizedLabel, 0, 20);
        GridPane.setColumnSpan(stopIfMinimizedLabel, 3);

        CheckBox stopIfMinimizedCB = new CheckBox();
        stopIfMinimizedCB.setSelected(stopIfMinimized);
        GridPane.setHalignment(stopIfMinimizedCB, HPos.CENTER);
        settingsGrid.add(stopIfMinimizedCB, 2, 20);


        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        settingsGrid.setPadding(new Insets(10));
        scrollPaneRight.setContent(settingsGrid);

        // ------------------ GUI Layout; Stage Settings ------------------


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
        stage.getIcons().add(new Image("/GameOfLife.png"));
        stage.setResizable(true);

        stage.setWidth(GUI_WIDTH);
        stage.setHeight(GUI_HEIGHT);

        stage.show();

        // ------------------ Event Handlers ------------------

        // Change the size of the play field to the values of the text fields
        setDimensionBt.setOnAction(e -> GuiLogic.setDimensions(xDimTf, yDimTf, curLivingNumLabel));


        // Runnable, which is periodically called from the ScheduledExecutorService,
        // to get the play field to the next generation
        Runnable runGame = () -> {
            if (playField.stepForward()) {
                // Updating GUI elements can only be done in the Application thread
                Platform.runLater(() -> {
                    GuiLogic.drawPlayField();
                    curGenNumLabel.setText(Integer.toString(playField.getGeneration()));
                    curLivingNumLabel.setText(Integer.toString(playField.getLivingCells()));
                });
            } else {
                GuiLogic.pauseGame(executor);
            }
        };

        // Start the game -> initialise ScheduledExecutorService
        playBt.setOnAction(e -> GuiLogic.playGame(runGame));

        // Pause the game -> Stop the ScheduledExecutorService
        pauseBt.setOnAction(e -> GuiLogic.pauseGame(executor));


        // Get the play field to the last generation
        stepBackBt.setOnAction(e -> GuiLogic.stepBack(curGenNumLabel, curLivingNumLabel));

        // Get the play field to the next generation
        stepForwardBt.setOnAction(e -> GuiLogic.stepForward(curGenNumLabel, curLivingNumLabel));


        // Go the a specific Generation
        goToBt.setOnAction(e -> GuiLogic.goToGen(curGenNumLabel, curLivingNumLabel, goToTf));


        // Reset the complete game and display the changes
        resetBt.setOnAction(e -> GuiLogic.reset(curGenNumLabel, curLivingNumLabel));

        // Reset the play field to the state of generation zero
        resetToStartBt.setOnAction(e -> GuiLogic.resetToStart(curGenNumLabel, curLivingNumLabel));


        // Change the game speed of the play field to the value of the text field
        setSpeedBt.setOnAction(e -> GuiLogic.setSpeed(speedTf));

        // Change the game rules of the play field to the values of the text fields
        gameRulesBt.setOnAction(e -> GuiLogic.setGameRules(reanimateRuleTf, keepLifeRuleTf));


        // Load the selected preset (file) from the dropdown menu of the presetBox
        presetBox.setOnAction(e -> GuiLogic.presetBox(xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel, presetBox));

        // Load a preset into the play field
        loadPresetBt.setOnAction(e -> GuiLogic.loadPreset(xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel));

        // Save the current play field to a CSV file
        savePresetBt.setOnAction(e -> GuiLogic.savePreset(presetBox));

        // Place living cells randomly on the play field
        randomPlacementBt.setOnAction(e -> GuiLogic.placeRandomly(curLivingNumLabel));


        // Open the analysis window and update the GUI
        analysisBt.setOnAction(e -> {
            AnalysisGui.show();
            playField.updateAnalysisGui();
        });


        // Zoom Slider for zooming into the game Canvas
        zoomSlider.valueProperty().addListener(e -> GuiLogic.changeZoom(zoomSlider));


        // Change the value of a cell to living or dead
        gameCanvas.setOnMouseClicked(e -> GuiLogic.changeCellState(e, curLivingNumLabel));


        // Change the value of stopIfMinimized to the value of the according CheckBox (stopIfMinimizedCB)
        stopIfMinimizedCB.selectedProperty().addListener(
                e -> stopIfMinimized = stopIfMinimizedCB.selectedProperty().getValue());

        // Detect if the main window has been minimized into the taskbar
        // If it has and the stopIfMinimized is true -> pause the game
        stage.iconifiedProperty().addListener(
                (ov, t, t1) -> GuiLogic.stopGameIfMinimized(t1, stopIfMinimized, executor));


        // Close the Analysis window when the Main window is closed
        stage.setOnCloseRequest(e -> AnalysisGui.close());
    }
}