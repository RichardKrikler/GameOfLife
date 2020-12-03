package gameoflife;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        Label curGenNumLabel = new Label("0");
        GridPane.setHalignment(curGenNumLabel, HPos.CENTER);
        settingsGrid.add(curGenNumLabel, 2, 1);

        // Currently Living cells
        Label curLivingLabel = new Label("Living Cells:");
        settingsGrid.add(curLivingLabel, 0, 2);
        GridPane.setColumnSpan(curLivingLabel, 2);

        Label curLivingNumLabel = new Label("0");
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


        // Game Speed Control
        Label gameSpeedLabel = new Label("Game Speed (in s):");
        settingsGrid.add(gameSpeedLabel, 0, 8);
        GridPane.setColumnSpan(gameSpeedLabel, 3);

        TextField speedTf = new TextField();
        speedTf.setPromptText("Speed");
        speedTf.setMaxWidth(55);
        settingsGrid.add(speedTf, 0, 9);

        Button setSpeedBt = new Button("X");
        setSpeedBt.setTooltip(new Tooltip("Set Game Speed"));
        GridPane.setHalignment(setSpeedBt, HPos.CENTER);
        settingsGrid.add(setSpeedBt, 1, 9);


        // Change Game Rules
        Label gameRulesLabel = new Label("Game Rules:");
        settingsGrid.add(gameRulesLabel, 0, 10);
        GridPane.setColumnSpan(gameRulesLabel, 2);

        Button gameRulesBt = new Button("X");
        gameRulesBt.setTooltip(new Tooltip("Set Game Rules"));
        GridPane.setHalignment(gameRulesBt, HPos.CENTER);
        settingsGrid.add(gameRulesBt, 2, 10);

        Label deadRuleLabel = new Label("Dead becomes alive");
        settingsGrid.add(deadRuleLabel, 0, 11);
        GridPane.setColumnSpan(deadRuleLabel, 2);

        TextField deadRuleTf = new TextField();
        deadRuleTf.setPromptText("x,x");
        deadRuleTf.setMaxWidth(55);
        settingsGrid.add(deadRuleTf, 2, 11);

        Label lifeRuleLabel = new Label("Life lives on");
        settingsGrid.add(lifeRuleLabel, 0, 12);
        GridPane.setColumnSpan(lifeRuleLabel, 2);

        TextField lifeRuleTf = new TextField();
        lifeRuleTf.setPromptText("x,x");
        lifeRuleTf.setMaxWidth(55);
        settingsGrid.add(lifeRuleTf, 2, 12);


        // Presets
        Label presetsLabel = new Label("Presets:");
        settingsGrid.add(presetsLabel, 0, 13);
        GridPane.setColumnSpan(presetsLabel, 3);

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Option 1",
                        "Option 2",
                        "Option 3"
                );
        ComboBox<String> presetBox = new ComboBox<>(options);
        presetBox.setMinWidth(150);
        settingsGrid.add(presetBox, 0, 14);
        GridPane.setColumnSpan(presetBox, 3);
        presetBox.prefWidth(settingsGrid.getWidth());

        Button loadPresetBt = new Button("Load");
        loadPresetBt.setTooltip(new Tooltip("Load a Preset"));
        settingsGrid.add(loadPresetBt, 0, 15);

        Button savePresetBt = new Button("Save");
        savePresetBt.setTooltip(new Tooltip("Load a Preset"));
        settingsGrid.add(savePresetBt, 1, 15);


        // Game Analysis
        Label analysisLabel = new Label("Analysis:");
        settingsGrid.add(analysisLabel, 0, 16);
        GridPane.setColumnSpan(analysisLabel, 3);

        Button analysisBt = new Button("Show Analysis");
        analysisBt.setTooltip(new Tooltip("Show Game Analysis"));
        GridPane.setColumnSpan(analysisBt, 2);
        settingsGrid.add(analysisBt, 0, 17);


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
        Pattern pattern = Pattern.compile("^\\d+$");
        setDimensionBt.setOnAction(e -> {
            boolean validXDim = pattern.matcher(xDimTf.getText()).matches();
            boolean validYDim = pattern.matcher(yDimTf.getText()).matches();

            // If X dimension is invalid -> Display Error Message
            if (!validXDim) {
                errorDialog(stage, "Input Error", "The X dimension (\"" + xDimTf.getText() + "\") is not valid!", "Only integers are allowed.");
                xDimTf.setText("");
            }

            // If Y dimension is invalid -> Display Error Message
            if (!validYDim) {
                errorDialog(stage, "Input Error", "The Y dimension (\"" + yDimTf.getText() + "\") is not valid!", "Only integers are allowed.");
                yDimTf.setText("");
            }

            // If both inputs are valid -> set the dimensions of the playground and the size of the canvas
            if (validXDim && validYDim) {
                playField.setSize(Integer.parseInt(xDimTf.getText()), Integer.parseInt(yDimTf.getText()));
                drawPlayField(playField);
            }
        });

        playBt.setOnAction(e -> System.out.println("Start Game"));
        pauseBt.setOnAction(e -> System.out.println("Pause Game"));
        stepBackBt.setOnAction(e -> System.out.println("1-Step Back"));
        stepForwardBt.setOnAction(e -> System.out.println("1-Step Forward"));
        resetBt.setOnAction(e -> System.out.println("Reset Game"));
        resetToStartBt.setOnAction(e -> System.out.println("Reset Game to Start"));
        setSpeedBt.setOnAction(e -> System.out.println("Set Game Speed"));
        gameRulesBt.setOnAction(e -> System.out.println("Set Game Rules"));
        presetBox.setOnAction(e -> System.out.println("Preset: " + presetBox.getSelectionModel().getSelectedItem()));


        FileChooser fileChooser = new FileChooser();

        // File Extension = CSV
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        // Initial / Default Directory = "resources/PlayFieldPresets"
        fileChooser.setInitialDirectory(new File("resources/PlayFieldPresets"));

        // Load a preset into the play field
        loadPresetBt.setOnAction(e -> {
            // Show the FileChooser open Dialog
            File srcFile = fileChooser.showOpenDialog(stage);

            // If the Dialog is cancelled or closed the value of destFile will be null
            // If the selection is confirmed -> read the chosen file
            if (srcFile != null) {
                try {
                    loadPreset(stage, playField, srcFile, xDimTf, yDimTf);
                } catch (IOException ioException) {
                    errorDialog(stage, "IOException", "Could not read the file!", String.valueOf(ioException.getCause()));
                }
            }
        });

        // Save the current play field to a CSV file
        savePresetBt.setOnAction(e -> {
            // Show the FileChooser save Dialog
            File destFile = fileChooser.showSaveDialog(stage);

            // If the Dialog is cancelled or closed the value of destFile will be null
            // If the selection is confirmed -> write to the chosen file
            if (destFile != null) {
                try {
                    Files.writeString(Paths.get(destFile.getPath()), playField.convertToCSV());
                } catch (IOException ioException) {
                    errorDialog(stage, "IOException", "Could not write to file!", String.valueOf(ioException.getCause()));
                }
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

            // If it is a living cell -> dead
            if (playField.getCell(posX, posY) == 1) {
                playField.setCell(posX, posY, 0);
            } else {
                playField.setCell(posX, posY, 1);
            }

            drawPlayField(playField);
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
        for (int i = 0; i < playField.getDimensionY(); i++) {
            for (int j = 0; j < playField.getDimensionX(); j++) {
                if (playField.getCell(j, i) == 1) {
                    gc.setFill(Color.web("98E35B"));
                    gc.fillRect(j * sizePerCell, i * sizePerCell, sizePerCell, sizePerCell);
                }
            }
        }

        // Draw Grid
        for (int i = 0; i <= playField.getDimensionX(); i++) {
            gc.strokeLine(i * sizePerCell, 0, i * sizePerCell, canvasH);
        }

        for (int i = 0; i <= playField.getDimensionY(); i++) {
            gc.strokeLine(0, i * sizePerCell, canvasW, i * sizePerCell);
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
     * Load a Preset from a File into the play field
     *
     * @param stage     top level JavaFX container for the main GUI
     * @param playField PlayField Object containing the play field array
     * @param srcFile   Source file of the Preset
     * @param xDimTf    TextField for the Dimension input
     * @param yDimTf    TextField for the Dimension input
     * @throws IOException If the File could not be loaded properly
     */
    void loadPreset(Stage stage, PlayField playField, File srcFile, TextField xDimTf, TextField yDimTf) throws IOException {
        // Load the file -> if not possible -> show Error Dialog
        if (playField.loadFromCSV(Files.readAllLines(Paths.get(srcFile.getPath())))) {
            drawPlayField(playField);
            xDimTf.setText(Integer.toString(playField.getDimensionX()));
            yDimTf.setText(Integer.toString(playField.getDimensionY()));
        } else {
            errorDialog(stage, "Loading File", "Could not load the file to the play field!", "Please check the file or try another one.");
        }
    }
}
