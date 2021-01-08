package gameoflife;

import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * GUI Logic for the Game Of Life
 * - General functions (drawing PlayField to GUI; Error Dialog; Pausing Game; ...)
 * - Functions for the event handlers
 *
 * @author Richard Krikler
 */
public class GuiLogic {
    /**
     * RegExp Pattern for the game rule input text field
     */
    private static final Pattern INTEGER_PAT = Pattern.compile("^\\d+$");

    /**
     * RegExp Pattern for the game speed input text field
     */
    private static final Pattern GAME_SPEED_PAT = Pattern.compile("^([1-9]\\d*(\\.\\d*)?)|(\\d*\\.\\d{0,2}[1-9]0*)$");

    /**
     * RegExp Pattern for the game rule input text field
     */
    private static final Pattern GAME_RULE_PAT = Pattern.compile("^([0-8],)*[0-8]$");


    /**
     * Draw the current Play Field to the Canvas of the Gui
     */
    static void drawPlayField() {
        Gui.gameCanvas.setWidth(Gui.playField.getDimensionX() * Gui.sizePerCell);
        Gui.gameCanvas.setHeight(Gui.playField.getDimensionY() * Gui.sizePerCell);

        double canvasW = Gui.gameCanvas.getWidth();
        double canvasH = Gui.gameCanvas.getHeight();

        Gui.gc.clearRect(0, 0, canvasW, canvasH);

        // Draw Cells
        for (int y = 0; y < Gui.playField.getDimensionY(); y++) {
            for (int x = 0; x < Gui.playField.getDimensionX(); x++) {
                if (Gui.playField.getCell(x, y) == 1) {
                    Gui.gc.setFill(Color.web("98E35B"));
                    Gui.gc.fillRect(x * Gui.sizePerCell, y * Gui.sizePerCell, Gui.sizePerCell, Gui.sizePerCell);
                }
            }
        }

        // Draw Grid
        for (int x = 0; x <= Gui.playField.getDimensionX(); x++) {
            Gui.gc.strokeLine(x * Gui.sizePerCell, 0, x * Gui.sizePerCell, canvasH);
        }

        for (int y = 0; y <= Gui.playField.getDimensionY(); y++) {
            Gui.gc.strokeLine(0, y * Gui.sizePerCell, canvasW, y * Gui.sizePerCell);
        }
    }

    /**
     * Display variable Error Dialog.
     *
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
    static void pauseGame(ScheduledExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Update the play field to a new array
     *
     * @param newPlayField      int[][] array which contains the new play field
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void updatePlayField(int[][] newPlayField, TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel) {
        if (newPlayField != null) {
            pauseGame(Gui.executor);
            Gui.playField.setPlayField(newPlayField);
            Gui.playField.resetGeneration();
            drawPlayField();

            xDimTf.setText(Integer.toString(Gui.playField.getDimensionX()));
            yDimTf.setText(Integer.toString(Gui.playField.getDimensionY()));
            curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
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

    // ------------------------------------


    /**
     * Change the size of the play field to the values of the text fields
     *
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void setDimensions(TextField xDimTf, TextField yDimTf, Label curLivingNumLabel) {
        String xDim = xDimTf.getText();
        String yDim = yDimTf.getText();

        boolean validXDim = INTEGER_PAT.matcher(xDim).matches();
        boolean validYDim = INTEGER_PAT.matcher(yDim).matches();

        // If X dimension is invalid -> Display Error Message
        if (!validXDim) {
            errorDialog(Gui.stage, "Input Error", "The X dimension (\"" + xDim + "\") is not valid!", "Only integers are allowed.");
            xDimTf.setText(Integer.toString(Gui.playField.getDimensionX()));
        }

        // If Y dimension is invalid -> Display Error Message
        if (!validYDim) {
            errorDialog(Gui.stage, "Input Error", "The Y dimension (\"" + yDim + "\") is not valid!", "Only integers are allowed.");
            yDimTf.setText(Integer.toString(Gui.playField.getDimensionY()));
        }

        // If both inputs are valid -> set the dimensions of the playground and the size of the canvas
        if (validXDim && validYDim) {
            pauseGame(Gui.executor);
            Gui.playField.setSize(Integer.parseInt(xDim), Integer.parseInt(yDim));
            drawPlayField();
            curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
        }
    }

    /**
     * Start the game -> initialise ScheduledExecutorService
     *
     * @param runGame Runnable, which is periodically called from the ScheduledExecutorService,
     *                to get the play field to the next generation
     */
    static void playGame(Runnable runGame) {
        Gui.executor = Executors.newScheduledThreadPool(1);
        Gui.executor.scheduleAtFixedRate(runGame, 0, (long) (Gui.playField.getGameSpeed() * 1000), TimeUnit.MILLISECONDS);
    }

    /**
     * Get the play field to the last generation
     *
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void stepBack(Label curGenNumLabel, Label curLivingNumLabel) {
        if (Gui.playField.stepTo(Gui.playField.getGeneration() - 1)) {
            drawPlayField();
            curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
            Gui.playField.updateAnalysisGui();
        }
    }

    /**
     * Get the play field to the next generation
     *
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void stepForward(Label curGenNumLabel, Label curLivingNumLabel) {
        if (Gui.playField.stepForward()) {
            GuiLogic.drawPlayField();
            curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
        }
    }

    /**
     * Go the a specific Generation
     *
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     * @param goToTf            text field for the wished generation
     */
    static void goToGen(Label curGenNumLabel, Label curLivingNumLabel, TextField goToTf) {
        String gen = goToTf.getText();
        boolean validGen = INTEGER_PAT.matcher(gen).matches();

        if (validGen) {
            // If generation is valid -> go to the generation x
            if (Gui.playField.stepTo(Integer.parseInt(gen))) {
                GuiLogic.drawPlayField();
                curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
                curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
                goToTf.setText(Integer.toString(Gui.playField.getGeneration()));
            }
        } else {
            // If generation is invalid -> Display Error Message
            errorDialog(Gui.stage, "Input Error", "The Generation (\"" + gen + "\") is not valid!", "Only integers are allowed.");
            goToTf.setText("");
        }
    }

    /**
     * Reset the complete game and display the changes
     *
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void reset(Label curGenNumLabel, Label curLivingNumLabel) {
        pauseGame(Gui.executor);
        Gui.playField.setSize(Gui.playField.getDimensionX(), Gui.playField.getDimensionY());
        Gui.playField.resetGeneration();

        GuiLogic.drawPlayField();
        curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
        curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
    }

    /**
     * Reset the play field to the state of generation zero
     *
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void resetToStart(Label curGenNumLabel, Label curLivingNumLabel) {
        pauseGame(Gui.executor);
        if (Gui.playField.stepTo(0)) {
            GuiLogic.drawPlayField();
            curGenNumLabel.setText(Integer.toString(Gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
        }
    }

    /**
     * Change the game speed of the play field to the value of the text field
     *
     * @param speedTf text field for the speed input
     */
    static void setSpeed(TextField speedTf) {
        String gameSpeed = speedTf.getText();

        // If Game Speed is valid -> pause the game and set the game speed
        // Otherwise -> Display Error Message
        if (GAME_SPEED_PAT.matcher(gameSpeed).matches()) {
            pauseGame(Gui.executor);
            Gui.playField.setGameSpeed(Float.parseFloat(gameSpeed));
        } else {
            errorDialog(Gui.stage, "Input Error", "The Game Speed (\"" + gameSpeed + "\") is not valid!",
                    "Only integers or floating point values are allowed. "
                            + "There is a maximum of 3 decimal points. The values are interpreted in seconds. Minimum value = 0.001");
            speedTf.setText(Float.toString(Gui.playField.getGameSpeed()));
        }
    }

    /**
     * Change the game rules of the play field to the values of the text fields
     *
     * @param reanimateRuleTf text field for the reanimate rule input
     * @param keepLifeRuleTf  text field for the keep life rule input
     */
    static void setGameRules(TextField reanimateRuleTf, TextField keepLifeRuleTf) {
        String reanimateRule = reanimateRuleTf.getText();
        String keepLifeRule = keepLifeRuleTf.getText();

        boolean validReanimateRule = GAME_RULE_PAT.matcher(reanimateRule).matches();
        boolean validKeepLifeRule = GAME_RULE_PAT.matcher(keepLifeRule).matches();

        String errorExplanation = "The rule is valid if it contains one or more integers (from 0 to 8), separated with a comma (\",\").";

        // If Reanimate Rule is invalid -> Display Error Message
        if (!validReanimateRule) {
            errorDialog(Gui.stage, "Input Error", "The Reanimate Rule (\"" + reanimateRule + "\") is not valid!", errorExplanation);
            reanimateRuleTf.setText(Gui.playField.getReanimateRule());
        }

        // If Keep Alive Rule is invalid -> Display Error Message
        if (!validKeepLifeRule) {
            errorDialog(Gui.stage, "Input Error", "The Keep Alive Rule (\"" + keepLifeRule + "\") is not valid!", errorExplanation);
            keepLifeRuleTf.setText(Gui.playField.getKeepLifeRule());
        }

        // If both inputs are valid -> set the game rules to the input values
        if (validReanimateRule & validKeepLifeRule) {
            Gui.playField.setReanimateRule(reanimateRule);
            Gui.playField.setKeepLifeRule(keepLifeRule);
        }
    }

    /**
     * Load the selected preset (file) from the dropdown menu of the presetBox
     *
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     * @param presetBox         combo box for the presets in the preset folder
     */
    static void presetBox(TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel, ComboBox<String> presetBox) {
        if (presetBox.getValue() != null && !presetBox.getValue().equals("")) {
            String selectedItem = presetBox.getSelectionModel().getSelectedItem();
            presetBox.getSelectionModel().select(0);

            int[][] newPlayField = Gui.presetManager.loadPreset(selectedItem);
            updatePlayField(newPlayField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
        }
    }

    /**
     * Load a preset into the play field
     *
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void loadPreset(TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel) {
        int[][] newPlayField = Gui.presetManager.loadPreset();
        GuiLogic.updatePlayField(newPlayField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
    }

    /**
     * Save the current play field to a CSV file
     *
     * @param presetBox combo box for the presets in the preset folder
     */
    static void savePreset(ComboBox<String> presetBox) {
        if (Gui.presetManager.savePreset(Gui.playField)) {
            presetBox.setItems(Gui.presetManager.getObservableList());
        }
    }

    /**
     * Place living cells randomly on the play field
     *
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void placeRandomly(Label curLivingNumLabel) {
        Gui.playField.placeRandomly();
        drawPlayField();
        curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
    }

    /**
     * Zoom Slider for zooming into the game Canvas
     *
     * @param zoomSlider slider for changing the zoom of the game canvas
     */
    static void changeZoom(Slider zoomSlider) {
        // Change the variable size per cell according to the value of the zoom slider
        Gui.sizePerCell = (int) (Gui.DEFAULT_SIZE_PER_CELL * zoomSlider.getValue());
        GuiLogic.drawPlayField();
    }

    /**
     * Change the value of a cell to living or dead
     *
     * @param e                 mouse event, which triggers when the game canvas has been clicked
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void changeCellState(MouseEvent e, Label curLivingNumLabel) {
        // Get the expected position in the array from the mouse position
        int posX = (int) (e.getX() / Gui.sizePerCell);
        int posY = (int) (e.getY() / Gui.sizePerCell);

        // If the positions are within the size of the play field
        if (posX < Gui.playField.getDimensionX() && posY < Gui.playField.getDimensionY()) {
            // If it is a living cell -> dead
            if (Gui.playField.getCell(posX, posY) == 1) {
                Gui.playField.setCell(posX, posY, 0);
            } else {
                Gui.playField.setCell(posX, posY, 1);
            }
        }

        drawPlayField();
        curLivingNumLabel.setText(Integer.toString(Gui.playField.getLivingCells()));
    }

    /**
     * Pause the game if the window is minimized and if stopIfMinimized is true
     *
     * @param t1              boolean value, which is true if the window has been minimized into the taskbar
     * @param stopIfMinimized boolean value, which is true if the game should stop when the window is minimized into the taskbar
     * @param executor        scheduled executor service for periodically getting the play field to the next generation
     */
    static void stopGameIfMinimized(Boolean t1, boolean stopIfMinimized, ScheduledExecutorService executor) {
        if (t1 && stopIfMinimized) {
            pauseGame(executor);
        }
    }

}
