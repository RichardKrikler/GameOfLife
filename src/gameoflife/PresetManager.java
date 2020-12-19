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
    private final String PRESET_PATH;

    /**
     * Save the available presets in the resources/PlayFieldPresets in a Map
     * String = name of the preset
     * Path = location of the preset
     */
    private final HashMap<String, Path> PRESETS = new HashMap<>();

    /**
     * FileChooser for the file selection dialogs
     */
    private final FileChooser FILE_CHOOSER = new FileChooser();

    /**
     * stage: top level JavaFX container for the main GUI
     */
    private final Stage STAGE;

    /**
     * PresetManager Constructor
     *
     * @param PRESET_PATH path to the folder, which contains the presets
     */
    public PresetManager(Stage stage, String PRESET_PATH) {
        this.STAGE = stage;
        this.PRESET_PATH = PRESET_PATH;
        loadPresetsToMap();


        // File Extension = CSV
        FILE_CHOOSER.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        // Initial / Default Directory = preset path
        FILE_CHOOSER.setInitialDirectory(new File(this.PRESET_PATH));
    }


    /**
     * Load the files from the presets folder into the presets Map
     */
    public void loadPresetsToMap() {
        PRESETS.clear();
        PRESETS.put("", null);
        File[] presetFiles = new File(PRESET_PATH).listFiles();

        // If there are any files in the preset folder ->
        // put the files and the filenames (without the ".csv" extension) into the presets Map
        if (presetFiles != null) {
            for (File presetFile : presetFiles) {
                if (presetFile.isFile()) {
                    PRESETS.put(presetFile.getName().replaceAll(".csv$", ""), presetFile.toPath());
                }
            }
        }
    }


    /**
     * Get an observable list of the preset filenames
     */
    public ObservableList<String> getObservableList() {
        return FXCollections.observableArrayList(PRESETS.keySet());
    }


    /**
     * Load the preset from the preset file path
     *
     * @return int[][] array which contains the play field of the preset
     */
    public int[][] loadPreset(Path srcPath) {
        try {
            int[][] newPlayField = loadFromCSV(Files.readAllLines(srcPath));
            if (newPlayField == null) {
                GuiLogic.errorDialog(STAGE, "Loading File", "Could not load the file to the play field!", "Please check the file or try another one.");
            } else {
                return newPlayField;
            }
        } catch (IOException ioException) {
            GuiLogic.errorDialog(STAGE, "IOException", "Could not read the file!", "Error Message: " + String.valueOf(ioException.getCause()));
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
        File srcFile = FILE_CHOOSER.showOpenDialog(STAGE);

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
        return loadPreset(PRESETS.get(presetName));
    }


    /**
     * Save the preset to the selected path
     */
    public boolean savePreset(Path destPath, PlayField playField) {
        try {
            Files.writeString(destPath, convertToCSV(playField));
            loadPresetsToMap();
            return true;
        } catch (IOException ioException) {
            GuiLogic.errorDialog(STAGE, "IOException", "Could not write to file!", String.valueOf(ioException.getCause()));
        }
        return false;
    }

    public boolean savePreset(PlayField playField) {
        // Show the FileChooser save Dialog
        File destFile = FILE_CHOOSER.showSaveDialog(STAGE);

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
