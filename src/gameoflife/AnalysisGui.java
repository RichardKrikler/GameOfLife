package gameoflife;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI for the Game Analysis
 *
 * @author Richard Krikler
 */
public class AnalysisGui {

    /**
     * analysisStage: top level JavaFX container for the analysis GUI
     */
    private static Stage analysisStage = null;


    /**
     * Series of the cells per generation for the chart
     */
    private static final XYChart.Series<String, Number> CELLS_PER_GEN_SERIES =
            new XYChart.Series<>();

    /**
     * Series of the cell changes per generation for the chart
     */
    private static final XYChart.Series<String, Number> CHANGE_PER_GEN_SERIES =
            new XYChart.Series<>();

    /**
     * Series of the percent cell changes per generation for the chart
     */
    private static final XYChart.Series<String, Number> CHANGE_PERCENT_PER_GEN_SERIES =
            new XYChart.Series<>();


    /**
     * BarChart for living cells per generation
     */
    private static BarChart<String, Number> cellsPerGenBC;

    /**
     * BarChart for percent cell changes per generation
     */
    private static BarChart<String, Number> changePercentPerGenBC;


    /**
     * Generation quantity (Label)
     */
    private static Label generationNumLabel;

    /**
     * Minimum living cells (Label)
     */
    private static Label minCellsNumLabel;

    /**
     * Maximum living cells (Label)
     */
    private static Label maxCellsNumLabel;

    /**
     * Average living cells (Label)
     */
    private static Label avgCellsNumLabel;

    /**
     * Average change per generation (Label)
     */
    private static Label avgChangeNumLabel;

    /**
     * Average percentage change per generation (Label)
     */
    private static Label avgChangePercentNumLabel;


    /**
     * DecimalFormat for rounding the double values to two decimals
     */
    private static final DecimalFormat DF2 = new DecimalFormat("#.##");


    /**
     * Show the window for the game analysis
     */
    static void show() {
        analysisStage = new Stage();

        // ------------------ Analysis window header ------------------
        Label analysisLabel = new Label("Game Of Life - Analysis");
        analysisLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));

        StackPane analysisHeaderStackPane = new StackPane();
        analysisHeaderStackPane.getChildren().add(analysisLabel);

        // ------------------ Analysis Chart ------------------
        // ------------------ Living Cells per Generation - Chart
        CategoryAxis cellsPerGenX = new CategoryAxis();
        NumberAxis cellsPerGenY = new NumberAxis();

        cellsPerGenX.setLabel("Generation");
        cellsPerGenY.setLabel("Living Cells");

        cellsPerGenBC = new BarChart<>(cellsPerGenX, cellsPerGenY);
        cellsPerGenBC.setAnimated(false);
        cellsPerGenBC.setPrefHeight(230);
        cellsPerGenBC.setTitle("Living Cells per Generation");

        CELLS_PER_GEN_SERIES.setName("Living Cells");
        cellsPerGenBC.getData().add(CELLS_PER_GEN_SERIES);

        CHANGE_PER_GEN_SERIES.setName("Living Cell Changes");


        // ------------------ Living Cell Percent Change per Generation - Chart
        CategoryAxis changePercentPerGenX = new CategoryAxis();
        NumberAxis changePercentPerGenY = new NumberAxis();

        changePercentPerGenX.setLabel("Generation");
        changePercentPerGenY.setLabel("Percent Change");

        changePercentPerGenBC = new BarChart<>(changePercentPerGenX, changePercentPerGenY);
        changePercentPerGenBC.setAnimated(false);
        changePercentPerGenBC.setPrefHeight(230);
        changePercentPerGenBC.setTitle("Living Cell Percent Change per Generation");

        // Set the bar chart to a complete invisible state
        changePercentPerGenBC.setVisible(false);
        changePercentPerGenBC.managedProperty().bind(changePercentPerGenBC.visibleProperty());

        CHANGE_PERCENT_PER_GEN_SERIES.setName("Living Cell Percent Change");
        changePercentPerGenBC.getData().add(CHANGE_PERCENT_PER_GEN_SERIES);

        // ------------------ Analysis Data Grid ------------------
        GridPane analysisGrid = new GridPane();
        analysisGrid.setAlignment(Pos.CENTER);
        analysisGrid.setHgap(10);
        analysisGrid.setVgap(10);
        analysisGrid.setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        analysisGrid.getColumnConstraints().addAll(col1, col2);


        // Current Generation
        Label generationLabel = new Label("Generation:");
        analysisGrid.add(generationLabel, 0, 0);

        generationNumLabel = new Label();
        analysisGrid.add(generationNumLabel, 1, 0);
        GridPane.setHalignment(generationNumLabel, HPos.CENTER);


        // Minimum amount of living cells
        Label minCellsLabel = new Label("Minimum Cells:");
        analysisGrid.add(minCellsLabel, 0, 1);

        minCellsNumLabel = new Label();
        analysisGrid.add(minCellsNumLabel, 1, 1);
        GridPane.setHalignment(minCellsNumLabel, HPos.CENTER);


        // Maximum amount of living cells
        Label maxCellsLabel = new Label("Maximum Cells:");
        analysisGrid.add(maxCellsLabel, 0, 2);

        maxCellsNumLabel = new Label();
        analysisGrid.add(maxCellsNumLabel, 1, 2);
        GridPane.setHalignment(maxCellsNumLabel, HPos.CENTER);


        // Average amount of living cells
        Label avgCellsLabel = new Label("Average Cells:");
        analysisGrid.add(avgCellsLabel, 0, 3);

        avgCellsNumLabel = new Label();
        analysisGrid.add(avgCellsNumLabel, 1, 3);
        GridPane.setHalignment(avgCellsNumLabel, HPos.CENTER);


        // Average amount of changes per generation
        Label avgChangeLabel = new Label("Average Change:");
        analysisGrid.add(avgChangeLabel, 0, 4);

        avgChangeNumLabel = new Label();
        analysisGrid.add(avgChangeNumLabel, 1, 4);
        GridPane.setHalignment(avgChangeNumLabel, HPos.CENTER);


        // Percent change per generation
        Label avgChangePercentLabel = new Label("Average Percent Change [%]:");
        analysisGrid.add(avgChangePercentLabel, 0, 5);

        avgChangePercentNumLabel = new Label();
        analysisGrid.add(avgChangePercentNumLabel, 1, 5);
        GridPane.setHalignment(avgChangePercentNumLabel, HPos.CENTER);


        // ------------------ Analysis Chart Select Grid ------------------
        GridPane chartSelectGrid = new GridPane();
        chartSelectGrid.setAlignment(Pos.CENTER);
        chartSelectGrid.setHgap(10);
        chartSelectGrid.setVgap(10);
        chartSelectGrid.setPadding(new Insets(10));

        chartSelectGrid.getColumnConstraints().addAll(col1, col2);


        // CheckBox for displaying the living cells per generation - series
        Label livingCellsChartLabel = new Label("Living Cells:");
        chartSelectGrid.add(livingCellsChartLabel, 0, 0);

        CheckBox livingCellsCB = new CheckBox();
        livingCellsCB.setSelected(true);
        GridPane.setHalignment(livingCellsCB, HPos.CENTER);
        chartSelectGrid.add(livingCellsCB, 1, 0);


        // CheckBox for displaying the average cell change per generation - series
        Label averageChangeChartLabel = new Label("Living Cell Change:");
        chartSelectGrid.add(averageChangeChartLabel, 0, 1);

        CheckBox cellChangesCB = new CheckBox();
        GridPane.setHalignment(cellChangesCB, HPos.CENTER);
        chartSelectGrid.add(cellChangesCB, 1, 1);


        // CheckBox for displaying the percent cell changes per generation - chart
        Label averagePercentChangeChartLabel = new Label("Living Cell Percent Change:");
        chartSelectGrid.add(averagePercentChangeChartLabel, 0, 2);

        CheckBox cellPercentChangeCB = new CheckBox();
        GridPane.setHalignment(cellPercentChangeCB, HPos.CENTER);
        chartSelectGrid.add(cellPercentChangeCB, 1, 2);

        // ------------------ Gui Layout; Stage Settings ------------------
        VBox mainVBox = new VBox();
        mainVBox.getChildren().addAll(analysisHeaderStackPane, new Separator(),
                cellsPerGenBC, new Separator(),
                changePercentPerGenBC, new Separator(),
                analysisGrid, new Separator(),
                chartSelectGrid);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainVBox);

        // Always display the vertical scroll bar
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Never display the horizontal scroll bar
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Disable overlapping of scroll bar onto vBox
        scrollPane.setFitToWidth(true);


        Scene scene = new Scene(scrollPane);
        analysisStage.setScene(scene);
        analysisStage.setTitle("Game Of Life - Analysis");
        analysisStage.setWidth(500);
        analysisStage.setHeight(650);
        analysisStage.getIcons().add(new Image("/GameOfLife.png"));
        analysisStage.show();

        // ------------------ Event Handlers ------------------
        livingCellsCB.selectedProperty().addListener(e -> {
            // If the livingCellsCB checkbox is checked display the CELLS_PER_GEN_SERIES
            if (livingCellsCB.selectedProperty().getValue()) {
                cellsPerGenBC.getData().add(CELLS_PER_GEN_SERIES);
            } else {
                cellsPerGenBC.getData().remove(CELLS_PER_GEN_SERIES);
            }
        });

        cellChangesCB.selectedProperty().addListener(e -> {
            // If the cellChangesCB checkbox is checked display the CHANGE_PER_GEN_SERIES
            if (cellChangesCB.selectedProperty().getValue()) {
                cellsPerGenBC.getData().add(CHANGE_PER_GEN_SERIES);
            } else {
                cellsPerGenBC.getData().remove(CHANGE_PER_GEN_SERIES);
            }
        });

        cellPercentChangeCB.selectedProperty().addListener(e -> {
            // If the cellPercentChangeCB checkbox is checked display the changePercentPerGenBC
            if (cellPercentChangeCB.selectedProperty().getValue()) {
                changePercentPerGenBC.setVisible(true);
                changePercentPerGenBC.managedProperty().unbind();
            } else {
                changePercentPerGenBC.setVisible(false);
                changePercentPerGenBC.managedProperty().bind(
                        changePercentPerGenBC.visibleProperty());
            }
        });
    }


    /**
     * Check if the window for the game analysis is showing
     *
     * @return true if the window is showing
     */
    static boolean isShowing() {
        if (analysisStage != null) {
            return analysisStage.isShowing();
        } else {
            return false;
        }
    }


    /**
     * Close the window for the game analysis
     */
    static void close() {
        if (analysisStage != null) {
            analysisStage.close();
        }
    }


    /**
     * Update the chart and all other information on the analysis window
     *
     * @param cellsPerGen HashMap with the information about the living cells per generation
     */
    static void update(HashMap<Integer, Double[]> cellsPerGen) {
        Platform.runLater(() -> {
            // Update Analysis Data Grid
            generationNumLabel.setText(String.valueOf(cellsPerGen.size() - 1));
            minCellsNumLabel.setText(
                    DF2.format((cellsPerGen.values().stream()
                                    .mapToDouble(v -> v[0]).min())
                                    .orElse(-1)));
            maxCellsNumLabel.setText(
                    DF2.format((cellsPerGen.values().stream()
                                    .mapToDouble(v -> v[0]).max())
                                    .orElse(-1)));
            avgCellsNumLabel.setText(
                    DF2.format((cellsPerGen.values().stream()
                                    .mapToDouble(v -> v[0]).average())
                                    .orElse(-1)));
            avgChangeNumLabel.setText(
                    DF2.format((cellsPerGen.values().stream()
                            .mapToDouble(v -> Math.abs(v[1])).average())
                            .orElse(-1)));
            avgChangePercentNumLabel.setText(
                    DF2.format((cellsPerGen.values().stream()
                            .mapToDouble(v -> Math.abs(v[2])).average())
                            .orElse(-1)));

            // Clear the data in the 3 series
            CELLS_PER_GEN_SERIES.getData().clear();
            CHANGE_PER_GEN_SERIES.getData().clear();
            CHANGE_PERCENT_PER_GEN_SERIES.getData().clear();

            // Add the new data to the 3 series
            for (Map.Entry<Integer, Double[]> entry : cellsPerGen.entrySet()) {
                CELLS_PER_GEN_SERIES.getData().add(
                        new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()[0]));
                CHANGE_PER_GEN_SERIES.getData().add(
                        new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()[1]));
                CHANGE_PERCENT_PER_GEN_SERIES.getData().add(
                        new XYChart.Data<>(Integer.toString(entry.getKey()), entry.getValue()[2]));
            }

            // Change color of changePercentPerGenBC chart
            for (Node n : changePercentPerGenBC.lookupAll(".default-color0.chart-bar")) {
                n.setStyle("-fx-bar-fill: #4472c4;");
            }
        });
    }
}
