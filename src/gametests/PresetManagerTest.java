package gametests;

import gameoflife.PlayField;
import gameoflife.PresetManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PresetManager Class
 *
 * @author Richard Krikler
 */
class PresetManagerTest {

    /**
     * Store the play field inside the PlayField Object
     * <p>
     * PlayField with:
     * size of X: 15; Y: 16
     */
    private final PlayField playField = new PlayField(3, 3);

    /**
     * Store the preset manager (logic for the use of presets) inside the PresetManager Object
     * <p>
     * PresetManager with:
     * stage value of null
     * standard preset path
     */
    private final PresetManager presetManager = new PresetManager(null, "resources/JUnitTests/PresetManagerTest");

    /**
     * Content of Field0_0.csv in PresetManagerTest
     */
    private final int[][] field0_0 = new int[][]{{1, 0, 0, 1}, {0, 1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}};


    @Test
    void loadPresetsToMap() {
        presetManager.loadPresetsToMap();
        HashMap<String, Path> presets = new HashMap<>();
        presets.put("", null);
        presets.put("Field0_0", Path.of("resources/JUnitTests/PresetManagerTest/Field0_0.csv"));
        assertEquals(presets, presetManager.getPRESETS());
    }

    @Test
    void getObservableList() {
        ObservableList<String> observableList = FXCollections.observableArrayList("", "Field0_0");
        assertEquals(observableList, presetManager.getObservableList());
    }

    @Test
    void loadPresetFromPath() {
        assertTrue(Arrays.deepEquals(field0_0, presetManager.loadPreset(Path.of("resources/JUnitTests/PresetManagerTest/Field0_0.csv"))));
    }

    @Test
    void loadPresetFromName() {
        assertTrue(Arrays.deepEquals(field0_0, presetManager.loadPreset("Field0_0")));

        assertThrows(NullPointerException.class, () -> presetManager.loadPreset("xyz"));
    }

    @Test
    void savePresetWithPath() throws IOException {
        playField.setCell(0, 0, 1);

        Path saveTest = Path.of("resources/JUnitTests/PresetManagerTest/saveTest.csv");
        List<String> saveFileContent = List.of("1,0,0", "0,0,0", "0,0,0");

        presetManager.savePreset(saveTest, playField);
        assertEquals(saveFileContent, Files.readAllLines(saveTest));
        Files.delete(saveTest);
    }

    @Test
    void convertToCSV() {
        playField.setCell(0, 0, 1);
        String lS = System.lineSeparator();
        String csvContent = "1,0,0" + lS + "0,0,0" + lS + "0,0,0" + lS;

        assertEquals(csvContent, presetManager.convertToCSV(playField));
    }

    @Test
    void loadFromCSV() {
        int[][] field = new int[][]{{1, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        List<String> playField = List.of("1,0,0", "0,0,0", "0,0,0");
        assertTrue(Arrays.deepEquals(field, presetManager.loadFromCSV(playField)));

        List<String> playFieldInvalid0 = List.of();
        List<String> playFieldInvalid1 = List.of("1,0,0,0", "0,0,0", "0,0,0");
        List<String> playFieldInvalid2 = List.of("0,0,0", "0,5,0", "0,0,1");
        List<String> playFieldInvalid3 = List.of("0,0,0,", "0,0,0", "0,0,0");
        assertTrue(Arrays.deepEquals(null, presetManager.loadFromCSV(playFieldInvalid0)));
        assertTrue(Arrays.deepEquals(null, presetManager.loadFromCSV(playFieldInvalid1)));
        assertTrue(Arrays.deepEquals(null, presetManager.loadFromCSV(playFieldInvalid2)));
        assertTrue(Arrays.deepEquals(null, presetManager.loadFromCSV(playFieldInvalid3)));
    }
}