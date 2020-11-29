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
import javafx.stage.Stage;


/**
 * GUI for the Game Of Life
 *
 * @author Richard Krikler
 */
public class GameGui extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final int guiWidth = 1000;
        final int guiHeight = 550;


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

        Canvas gameCanvas = new Canvas(200, 200);
        // Canvas inside of a StackPane
        StackPane stackPane = new StackPane(gameCanvas);

        // StackPane inside of a ScrollPane
        scrollPaneLeft.setContent(stackPane);
        scrollPaneLeft.setFitToWidth(true);
        scrollPaneLeft.setFitToHeight(true);


        // ------------------ Example Drawing
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        double canvasW = gameCanvas.getWidth();
        double canvasH = gameCanvas.getHeight();

        // line, from top left to bottom right
        gc.strokeLine(0, 0, canvasW, 0);
        gc.strokeLine(0, canvasH, canvasW, canvasH);
        gc.strokeLine(0, 0, 0, canvasH);
        gc.strokeLine(canvasW, 0, canvasW, canvasH);
        // ------------------


        // Zoom Slider
        Slider zoomSlider = new Slider();
        zoomSlider.setMin(0);
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
//        settingsGrid.setGridLinesVisible(true);

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


        // Currently Living cells
        Label curLivingLabel = new Label("Living Cells:");
        settingsGrid.add(curLivingLabel, 0, 1);
        GridPane.setColumnSpan(curLivingLabel, 2);

        Label curLivingNumLabel = new Label("0");
        GridPane.setHalignment(curLivingNumLabel, HPos.CENTER);
        settingsGrid.add(curLivingNumLabel, 2, 1);


        // PlayField Dimensions
        Label playFieldLabel = new Label("Set Play Field Dimensions:");
        settingsGrid.add(playFieldLabel, 0, 2);
        GridPane.setColumnSpan(playFieldLabel, 4);

        TextField xDimTf = new TextField();
        xDimTf.setPromptText("X-Dim");
        xDimTf.setMaxWidth(55);
        settingsGrid.add(xDimTf, 0, 3);

        TextField yDimTf = new TextField();
        yDimTf.setPromptText("Y-Dim");
        yDimTf.setMaxWidth(55);
        settingsGrid.add(yDimTf, 1, 3);

        Button setDimensionBt = new Button("Set");
        setDimensionBt.setTooltip(new Tooltip("Set Dimension"));
        GridPane.setHalignment(setDimensionBt, HPos.CENTER);
        settingsGrid.add(setDimensionBt, 2, 3);


        // Game Controls
        Label gameControlLabel = new Label("Game Controls:");
        settingsGrid.add(gameControlLabel, 0, 4);
        GridPane.setColumnSpan(gameControlLabel, 4);

        Button playBt = new Button("▶");
        playBt.setTooltip(new Tooltip("Play"));
        GridPane.setHalignment(playBt, HPos.CENTER);
        settingsGrid.add(playBt, 0, 5);

        Button pauseBt = new Button("⏸");
        pauseBt.setTooltip(new Tooltip("Pause"));
        GridPane.setHalignment(pauseBt, HPos.CENTER);
        settingsGrid.add(pauseBt, 1, 5);

        Button stepBackBt = new Button("⏮");
        stepBackBt.setTooltip(new Tooltip("1 Step Back"));
        GridPane.setHalignment(stepBackBt, HPos.CENTER);
        settingsGrid.add(stepBackBt, 2, 5);


        Button resetBt = new Button("⏹");
        resetBt.setTooltip(new Tooltip("Reset"));
        GridPane.setHalignment(resetBt, HPos.CENTER);
        settingsGrid.add(resetBt, 0, 6);

        Button resetToStartBt = new Button("⭯");
        resetToStartBt.setTooltip(new Tooltip("Reset to Start"));
        GridPane.setHalignment(resetToStartBt, HPos.CENTER);
        settingsGrid.add(resetToStartBt, 1, 6);

        Button stepForwardBt = new Button("⏭");
        stepForwardBt.setTooltip(new Tooltip("1 Step Forward"));
        GridPane.setHalignment(stepForwardBt, HPos.CENTER);
        settingsGrid.add(stepForwardBt, 2, 6);


        // Game Speed Control
        Label gameSpeedLabel = new Label("Game Speed (in s):");
        settingsGrid.add(gameSpeedLabel, 0, 7);
        GridPane.setColumnSpan(gameSpeedLabel, 3);

        TextField speedTf = new TextField();
        speedTf.setPromptText("Speed");
        speedTf.setMaxWidth(55);
        settingsGrid.add(speedTf, 0, 8);

        Button setSpeedBt = new Button("X");
        setSpeedBt.setTooltip(new Tooltip("Set Game Speed"));
        GridPane.setHalignment(setSpeedBt, HPos.CENTER);
        settingsGrid.add(setSpeedBt, 1, 8);


        // Change Game Rules
        Label gameRulesLabel = new Label("Game Rules:");
        settingsGrid.add(gameRulesLabel, 0, 9);
        GridPane.setColumnSpan(gameRulesLabel, 3);

        Label deadRuleLabel = new Label("Dead becomes alive");
        settingsGrid.add(deadRuleLabel, 0, 10);
        GridPane.setColumnSpan(deadRuleLabel, 2);

        TextField deadRuleTf = new TextField();
        deadRuleTf.setPromptText("x,x");
        deadRuleTf.setMaxWidth(55);
        settingsGrid.add(deadRuleTf, 2, 10);

        Label lifeRuleLabel = new Label("Life lives on");
        settingsGrid.add(lifeRuleLabel, 0, 11);
        GridPane.setColumnSpan(lifeRuleLabel, 2);

        TextField lifeRuleTf = new TextField();
        lifeRuleTf.setPromptText("x,x");
        lifeRuleTf.setMaxWidth(55);
        settingsGrid.add(lifeRuleTf, 2, 11);


        // Presets
        Label presetsLabel = new Label("Presets:");
        settingsGrid.add(presetsLabel, 0, 12);
        GridPane.setColumnSpan(presetsLabel, 3);

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Option 1",
                        "Option 2",
                        "Option 3"
                );
        ComboBox presetBox = new ComboBox(options);
        presetBox.setMinWidth(150);
        settingsGrid.add(presetBox, 0, 13);
        GridPane.setColumnSpan(presetBox, 3);
        presetBox.prefWidth(settingsGrid.getWidth());

        Button loadPresetBt = new Button("Load");
        loadPresetBt.setTooltip(new Tooltip("Load a Preset"));
        settingsGrid.add(loadPresetBt, 0, 14);

        Button savePresetBt = new Button("Save");
        savePresetBt.setTooltip(new Tooltip("Load a Preset"));
        settingsGrid.add(savePresetBt, 1, 14);


        // Game Analysis
        Label analysisLabel = new Label("Analysis:");
        settingsGrid.add(analysisLabel, 0, 15);
        GridPane.setColumnSpan(analysisLabel, 3);

        Button analysisBt = new Button("Show Analysis");
        analysisBt.setTooltip(new Tooltip("Show Game Analysis"));
        GridPane.setColumnSpan(analysisBt, 2);
        settingsGrid.add(analysisBt, 0, 16);


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
        splitPane.setPrefSize(guiWidth * 0.6, guiWidth * 0.4);
        splitPane.getDividers().get(0).setPosition(0.60);


        Scene scene = new Scene(splitPane);
        stage.setScene(scene);
        stage.setTitle("Game Of Life");
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/GameOfLife.png")));
        stage.setResizable(true);

        stage.setWidth(guiWidth);
        stage.setHeight(guiHeight);

        stage.show();
    }
}
