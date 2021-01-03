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
    private static final Pattern integerPat = Pattern.compile("^\\d+$");

    /**
     * RegExp Pattern for the game speed input text field
     */
    private static final Pattern gameSpeedPat = Pattern.compile("^([1-9]\\d*(\\.\\d*)?)|(\\d*\\.\\d{0,2}[1-9]0*)$");

    /**
     * RegExp Pattern for the game rule input text field
     */
    private static final Pattern gameRulePat = Pattern.compile("^([0-8],)*[0-8]$");


    /**
     * Draw the current Play Field to the Canvas of the Gui
     *
     * @param gui object of the main class
     */
    static void drawPlayField(Gui gui) {
        gui.gameCanvas.setWidth(gui.playField.getDimensionX() * gui.sizePerCell);
        gui.gameCanvas.setHeight(gui.playField.getDimensionY() * gui.sizePerCell);

        double canvasW = gui.gameCanvas.getWidth();
        double canvasH = gui.gameCanvas.getHeight();

        gui.gc.clearRect(0, 0, canvasW, canvasH);

        // Draw Cells
        for (int y = 0; y < gui.playField.getDimensionY(); y++) {
            for (int x = 0; x < gui.playField.getDimensionX(); x++) {
                if (gui.playField.getCell(x, y) == 1) {
                    gui.gc.setFill(Color.web("98E35B"));
                    gui.gc.fillRect(x * gui.sizePerCell, y * gui.sizePerCell, gui.sizePerCell, gui.sizePerCell);
                }
            }
        }

        // Draw Grid
        for (int x = 0; x <= gui.playField.getDimensionX(); x++) {
            gui.gc.strokeLine(x * gui.sizePerCell, 0, x * gui.sizePerCell, canvasH);
        }

        for (int y = 0; y <= gui.playField.getDimensionY(); y++) {
            gui.gc.strokeLine(0, y * gui.sizePerCell, canvasW, y * gui.sizePerCell);
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
     * @param gui               object of the main class
     * @param newPlayField      int[][] array which contains the new play field
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void updatePlayField(Gui gui, int[][] newPlayField, TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel) {
        if (newPlayField != null) {
            pauseGame(gui.executor);
            gui.playField.setPlayField(newPlayField);
            gui.playField.setOriginalPlayField(newPlayField);
            gui.playField.resetGeneration();
            drawPlayField(gui);

            xDimTf.setText(Integer.toString(gui.playField.getDimensionX()));
            yDimTf.setText(Integer.toString(gui.playField.getDimensionY()));
            curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
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
     * @param gui               object of the main class
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void setDimensions(Gui gui, TextField xDimTf, TextField yDimTf, Label curLivingNumLabel) {
        String xDim = xDimTf.getText();
        String yDim = yDimTf.getText();

        boolean validXDim = integerPat.matcher(xDim).matches();
        boolean validYDim = integerPat.matcher(yDim).matches();

        // If X dimension is invalid -> Display Error Message
        if (!validXDim) {
            errorDialog(gui.stage, "Input Error", "The X dimension (\"" + xDim + "\") is not valid!", "Only integers are allowed.");
            xDimTf.setText(Integer.toString(gui.playField.getDimensionX()));
        }

        // If Y dimension is invalid -> Display Error Message
        if (!validYDim) {
            errorDialog(gui.stage, "Input Error", "The Y dimension (\"" + yDim + "\") is not valid!", "Only integers are allowed.");
            yDimTf.setText(Integer.toString(gui.playField.getDimensionY()));
        }

        // If both inputs are valid -> set the dimensions of the playground and the size of the canvas
        if (validXDim && validYDim) {
            pauseGame(gui.executor);
            gui.playField.setSize(Integer.parseInt(xDim), Integer.parseInt(yDim));
            drawPlayField(gui);
            curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
        }
    }

    /**
     * Start the game -> initialise ScheduledExecutorService
     *
     * @param gui     object of the main class
     * @param runGame Runnable, which is periodically called from the ScheduledExecutorService,
     *                to get the play field to the next generation
     */
    static void playGame(Gui gui, Runnable runGame) {
        gui.executor = Executors.newScheduledThreadPool(1);
        gui.executor.scheduleAtFixedRate(runGame, 0, (long) (gui.playField.getGameSpeed() * 1000), TimeUnit.MILLISECONDS);
    }

    /**
     * Get the play field to the last generation
     *
     * @param gui               object of the main class
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void stepBack(Gui gui, Label curGenNumLabel, Label curLivingNumLabel) {
        if (gui.playField.stepTo(gui.playField.getGeneration() - 1)) {
            drawPlayField(gui);
            curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
            gui.playField.updateAnalysisGui();
        }
    }

    /**
     * Get the play field to the next generation
     *
     * @param gui               object of the main class
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void stepForward(Gui gui, Label curGenNumLabel, Label curLivingNumLabel) {
        if (gui.playField.stepForward()) {
            GuiLogic.drawPlayField(gui);
            curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
        }
    }

    /**
     * Go the a specific Generation
     *
     * @param gui               object of the main class
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     * @param goToTf            text field for the wished generation
     */
    static void goToGen(Gui gui, Label curGenNumLabel, Label curLivingNumLabel, TextField goToTf) {
        String gen = goToTf.getText();
        boolean validGen = integerPat.matcher(gen).matches();

        if (validGen) {
            // If generation is valid -> go to the generation x
            if (gui.playField.stepTo(Integer.parseInt(gen))) {
                GuiLogic.drawPlayField(gui);
                curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
                curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
                goToTf.setText(Integer.toString(gui.playField.getGeneration()));
            }
        } else {
            // If generation is invalid -> Display Error Message
            errorDialog(gui.stage, "Input Error", "The Generation (\"" + gen + "\") is not valid!", "Only integers are allowed.");
            goToTf.setText("");
        }
    }

    /**
     * Reset the complete game and display the changes
     *
     * @param gui               object of the main class
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void reset(Gui gui, Label curGenNumLabel, Label curLivingNumLabel) {
        pauseGame(gui.executor);
        gui.playField.setSize(gui.playField.getDimensionX(), gui.playField.getDimensionY());
        gui.playField.resetGeneration();

        GuiLogic.drawPlayField(gui);
        curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
        curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
    }

    /**
     * Reset the play field to the state of generation zero
     *
     * @param gui               object of the main class
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void resetToStart(Gui gui, Label curGenNumLabel, Label curLivingNumLabel) {
        pauseGame(gui.executor);
        if (gui.playField.stepTo(0)) {
            GuiLogic.drawPlayField(gui);
            curGenNumLabel.setText(Integer.toString(gui.playField.getGeneration()));
            curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
        }
    }

    /**
     * Change the game speed of the play field to the value of the text field
     *
     * @param gui     object of the main class
     * @param speedTf text field for the speed input
     */
    static void setSpeed(Gui gui, TextField speedTf) {
        String gameSpeed = speedTf.getText();

        // If Game Speed is valid -> pause the game and set the game speed
        // Otherwise -> Display Error Message
        if (gameSpeedPat.matcher(gameSpeed).matches()) {
            pauseGame(gui.executor);
            gui.playField.setGameSpeed(Float.parseFloat(gameSpeed));
        } else {
            errorDialog(gui.stage, "Input Error", "The Game Speed (\"" + gameSpeed + "\") is not valid!", "Only integers or floating point values are allowed. " +
                    "There is a maximum of 3 decimal points. The values are interpreted in seconds. Minimum value = 0.001");
            speedTf.setText(Float.toString(gui.playField.getGameSpeed()));
        }
    }

    /**
     * Change the game rules of the play field to the values of the text fields
     *
     * @param gui             object of the main class
     * @param reanimateRuleTf text field for the reanimate rule input
     * @param keepLifeRuleTf  text field for the keep life rule input
     */
    static void setGameRules(Gui gui, TextField reanimateRuleTf, TextField keepLifeRuleTf) {
        String reanimateRule = reanimateRuleTf.getText();
        String keepLifeRule = keepLifeRuleTf.getText();

        boolean validReanimateRule = gameRulePat.matcher(reanimateRule).matches();
        boolean validKeepLifeRule = gameRulePat.matcher(keepLifeRule).matches();

        String errorExplanation = "The rule is valid if it contains one or more integers (from 0 to 8), separated with a comma (\",\").";

        // If Reanimate Rule is invalid -> Display Error Message
        if (!validReanimateRule) {
            errorDialog(gui.stage, "Input Error", "The Reanimate Rule (\"" + reanimateRule + "\") is not valid!", errorExplanation);
            reanimateRuleTf.setText(gui.playField.getReanimateRule());
        }

        // If Keep Alive Rule is invalid -> Display Error Message
        if (!validKeepLifeRule) {
            errorDialog(gui.stage, "Input Error", "The Keep Alive Rule (\"" + keepLifeRule + "\") is not valid!", errorExplanation);
            keepLifeRuleTf.setText(gui.playField.getKeepLifeRule());
        }

        // If both inputs are valid -> set the game rules to the input values
        if (validReanimateRule & validKeepLifeRule) {
            gui.playField.setReanimateRule(reanimateRule);
            gui.playField.setKeepLifeRule(keepLifeRule);
        }
    }

    /**
     * Load the selected preset (file) from the dropdown menu of the presetBox
     *
     * @param gui               object of the main class
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     * @param presetBox         combo box for the presets in the preset folder
     */
    static void presetBox(Gui gui, TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel, ComboBox<String> presetBox) {
        if (presetBox.getValue() != null && !presetBox.getValue().equals("")) {
            String selectedItem = presetBox.getSelectionModel().getSelectedItem();
            presetBox.getSelectionModel().select(0);

            int[][] newPlayField = gui.presetManager.loadPreset(selectedItem);
            updatePlayField(gui, newPlayField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
        }
    }

    /**
     * Load a preset into the play field
     *
     * @param gui               object of the main class
     * @param xDimTf            text field for the X dimension input
     * @param yDimTf            text field for the Y dimension input
     * @param curGenNumLabel    label for displaying the current generation
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void loadPreset(Gui gui, TextField xDimTf, TextField yDimTf, Label curGenNumLabel, Label curLivingNumLabel) {
        int[][] newPlayField = gui.presetManager.loadPreset();
        GuiLogic.updatePlayField(gui, newPlayField, xDimTf, yDimTf, curGenNumLabel, curLivingNumLabel);
    }

    /**
     * Save the current play field to a CSV file
     *
     * @param gui       object of the main class
     * @param presetBox combo box for the presets in the preset folder
     */
    static void savePreset(Gui gui, ComboBox<String> presetBox) {
        if (gui.presetManager.savePreset(gui.playField)) {
            presetBox.setItems(gui.presetManager.getObservableList());
        }
    }

    /**
     * Zoom Slider for zooming into the game Canvas
     *
     * @param gui        object of the main class
     * @param zoomSlider slider for changing the zoom of the game canvas
     */
    static void changeZoom(Gui gui, Slider zoomSlider) {
        // Change the variable size per cell according to the value of the zoom slider
        gui.sizePerCell = (int) (gui.DEFAULT_SIZE_PER_CELL * zoomSlider.getValue());
        GuiLogic.drawPlayField(gui);
    }

    /**
     * Change the value of a cell to living or dead
     *
     * @param gui               object of the main class
     * @param e                 mouse event, which triggers when the game canvas has been clicked
     * @param curLivingNumLabel label for displaying the current amount of living cells
     */
    static void changeCellState(Gui gui, MouseEvent e, Label curLivingNumLabel) {
        // Get the expected position in the array from the mouse position
        int posX = (int) (e.getX() / gui.sizePerCell);
        int posY = (int) (e.getY() / gui.sizePerCell);

        // If the positions are within the size of the play field
        if (posX < gui.playField.getDimensionX() && posY < gui.playField.getDimensionY()) {
            // If it is a living cell -> dead
            if (gui.playField.getCell(posX, posY) == 1) {
                gui.playField.setCell(posX, posY, 0);
            } else {
                gui.playField.setCell(posX, posY, 1);
            }
        }

        drawPlayField(gui);
        curLivingNumLabel.setText(Integer.toString(gui.playField.getLivingCells()));
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
