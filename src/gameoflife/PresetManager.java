package gameoflife;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Preset Manager for storing preset locations, reading / writing presets
 *
 * @author Richard Krikler
 */
public class PresetManager {
    /**
     * Store the path to the folder, which contains the presets
     */
    private final String presetPath;

    /**
     * Save the available presets in the resources/PlayFieldPresets in a Map
     * String = name of the preset
     * Path = location of the preset
     */
    private final HashMap<String, Path> presets = new HashMap<>();

    /**
     * FileChooser for the file selection dialogs
     */
    private final FileChooser fileChooser = new FileChooser();

    /**
     * stage: top level JavaFX container for the main GUI
     */
    private final Stage stage;

    /**
     * PresetManager Constructor
     *
     * @param stage      top level JavaFX container for the main GUI
     * @param presetPath path to the folder, which contains the presets
     */
    public PresetManager(Stage stage, String presetPath) {
        this.stage = stage;
        this.presetPath = presetPath;
        loadPresetsToMap();


        // File Extension = CSV
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        // Initial / Default Directory = preset path
        fileChooser.setInitialDirectory(new File(this.presetPath));
    }


    /**
     * Load the files from the presets folder into the presets Map
     */
    public void loadPresetsToMap() {
        presets.clear();
        presets.put("", null);
        File[] presetFiles = new File(presetPath).listFiles();

        // If there are any files in the preset folder ->
        // put the files and the filenames (without the ".csv" extension) into the presets Map
        if (presetFiles != null) {
            for (File presetFile : presetFiles) {
                if (presetFile.isFile()) {
                    presets.put(presetFile.getName().replaceAll(".csv$", ""), presetFile.toPath());
                }
            }
        }
    }

    /**
     * Get the Map with the presets of the preset folder
     * String = name of the preset
     * Path = location of the preset
     *
     * @return HashMap with the presets
     */
    public HashMap<String, Path> getPresets() {
        return presets;
    }


    /**
     * Get a list of the preset filenames
     *
     * @return observable list from the presets map keySet
     */
    public ObservableList<String> getObservableList() {
        return FXCollections.observableArrayList(presets.keySet());
    }


    /**
     * Load the preset from the preset file path
     *
     * @param srcPath source Path
     * @return int[][] array which contains the play field of the preset
     */
    public int[][] loadPreset(Path srcPath) {
        try {
            int[][] newPlayField = loadFromCSV(Files.readAllLines(srcPath));
            if (newPlayField == null) {
                GuiLogic.errorDialog(stage,
                        "Loading File",
                        "Could not load the file to the play field!",
                        "Please check the file or try another one.");
            } else {
                return newPlayField;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            GuiLogic.errorDialog(stage,
                    "IOException",
                    "Could not read the file!",
                    "Error Message: " + ioException.getCause());
        }
        return null;
    }

    /**
     * Load the preset from a file selected through the fileChooser
     *
     * @return int[][] array which contains the play field of the preset
     */
    public int[][] loadPreset() {
        // Show the FileChooser open Dialog
        File srcFile = fileChooser.showOpenDialog(stage);

        // If the Dialog is cancelled or closed the value of srcFile will be null
        // If the selection is confirmed -> read the chosen file
        if (srcFile != null) {
            return loadPreset(srcFile.toPath());
        } else {
            return null;
        }
    }

    /**
     * Load the preset from the preset name and its corresponding file path
     *
     * @param presetName name of the preset
     * @return int[][] array which contains the play field of the preset
     */
    public int[][] loadPreset(String presetName) {
        return loadPreset(presets.get(presetName));
    }


    /**
     * Save the preset to a given path
     *
     * @param destPath  destination Path
     * @param playField PlayField Object containing the current play field
     * @return true if saving the play field was successful
     */
    public boolean savePreset(Path destPath, PlayField playField) {
        try {
            Files.writeString(destPath, convertToCSV(playField));
            loadPresetsToMap();
            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            GuiLogic.errorDialog(stage,
                    "IOException",
                    "Could not write to file!",
                    String.valueOf(ioException.getCause()));
        }
        return false;
    }

    /**
     * Save the preset to the selected path
     *
     * @param playField PlayField Object containing the current play field
     * @return true if saving the play field was successful
     */
    public boolean savePreset(PlayField playField) {
        // Show the FileChooser save Dialog
        File destFile = fileChooser.showSaveDialog(stage);

        // If the Dialog is cancelled or closed the value of destFile will be null
        // If the selection is confirmed -> write to the chosen file
        if (destFile != null) {
            return savePreset(destFile.toPath(), playField);
        }
        return false;
    }


    /**
     * Convert the play field to CSV format
     *
     * @param playField PlayField Object containing the current play field
     * @return String containing the play field
     */
    public String convertToCSV(PlayField playField) {
        StringBuilder csv = new StringBuilder();

        for (int y = 0; y < playField.getDimensionY(); y++) {
            for (int x = 0; x < playField.getDimensionX(); x++) {
                if (x + 1 < playField.getDimensionX()) {
                    csv.append(playField.getCell(x, y)).append(",");
                } else {
                    csv.append(playField.getCell(x, y));
                }
            }
            csv.append(System.lineSeparator());
        }

        return csv.toString();
    }


    /**
     * Convert the CSV format from a file into the play field
     *
     * @param playField List of the Lines stored in the file
     * @return true if the conversion was successful
     */
    public int[][] loadFromCSV(List<String> playField) {
        if (playField.size() < 1) {
            return null;
        }

        Pattern validLine = Pattern.compile("^([10],)*[10]$");
        int[][] newPlayField = new int[playField.size()][];
        int dimensionX = playField.get(0).split(",").length;
        int dimensionY = playField.size();

        for (int y = 0; y < dimensionY; y++) {
            if (!validLine.matcher(playField.get(y)).matches()) {
                return null;
            }

            String[] oneLineStAr = playField.get(y).split(",");
            if (oneLineStAr.length != dimensionX) {
                return null;
            }

            int[] oneLineIntAr = GuiLogic.stringArToIntAr(oneLineStAr);
            newPlayField[y] = oneLineIntAr;
        }

        return newPlayField;
    }
}
