package gametests;

import gameoflife.PlayField;
import gameoflife.PresetManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PlayField Class
 *
 * @author Richard Krikler
 */
class PlayFieldTest {

    /**
     * Store the play field inside the PlayField Object
     * <p>
     * PlayField with:
     * size of X: 15; Y: 16
     * game speed of 1 second per generation
     * standard rules for
     * - reanimate rule: 3
     * - keep life rule: 2, 3
     */
    private final PlayField playField = new PlayField(15, 16, 1, new int[]{3}, new int[]{2, 3});

    /**
     * Store the preset manager (logic for the use of presets) inside the PresetManager Object
     * <p>
     * PresetManager with:
     * stage value of null
     * standard preset path
     */
    private final PresetManager presetManager = new PresetManager(null, "resources/PlayFieldPresets");


    @Test
    void testCreatePlayField() {
        assertEquals(15, playField.getDimensionX());
        assertEquals(16, playField.getDimensionY());
        assertEquals(1, playField.getGameSpeed());
        assertEquals("3", playField.getReanimateRule());
        assertEquals("2,3", playField.getKeepLifeRule());
    }

    @Test
    void setSize() {
        playField.setSize(3, 4);
        assertEquals(3, playField.getPlayField()[0].length);
        assertEquals(4, playField.getPlayField().length);
    }

    @Test
    void getDimensionX() {
        assertEquals(15, playField.getDimensionX());
    }

    @Test
    void getDimensionY() {
        assertEquals(16, playField.getDimensionY());
    }

    @Test
    void getCell() {
        playField.setPlayField(new int[][]{{0, 0, 1, 0}, {0, 0, 0, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}});
        assertEquals(0, playField.getCell(0, 0));
        assertEquals(1, playField.getCell(2, 0));
        assertEquals(0, playField.getCell(2, 1));
        assertEquals(1, playField.getCell(0, 2));
        assertEquals(1, playField.getCell(1, 3));
    }

    @Test
    void setCell() {
        playField.setCell(2, 0, 1);
        playField.setCell(0, 2, 1);
        playField.setCell(1, 3, 1);
        playField.setCell(14, 8, 1);

        assertEquals(1, playField.getPlayField()[0][2]);
        assertEquals(1, playField.getPlayField()[2][0]);
        assertEquals(1, playField.getPlayField()[3][1]);
        assertEquals(1, playField.getPlayField()[8][14]);


        assertThrows(ArrayIndexOutOfBoundsException.class, () -> playField.setCell(15, 8, 1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> playField.setCell(8, 16, 1));
    }

    @Test
    void setReanimateRule() {
        playField.setReanimateRule();
        assertEquals("", playField.getReanimateRule());

        playField.setReanimateRule(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals("0,1,2,3,4,5,6,7,8,9", playField.getReanimateRule());

        playField.setReanimateRule(5, 3, 9, 1, 0);
        assertEquals("0,1,3,5,9", playField.getReanimateRule());
    }

    @Test
    void setKeepLifeRule() {
        playField.setKeepLifeRule();
        assertEquals("", playField.getKeepLifeRule());

        playField.setKeepLifeRule(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals("0,1,2,3,4,5,6,7,8,9", playField.getKeepLifeRule());

        playField.setKeepLifeRule(5, 3, 9, 1, 0);
        assertEquals("0,1,3,5,9", playField.getKeepLifeRule());
    }

    @Test
    void getLivingCells() {
        playField.setPlayField(new int[][]{});
        assertEquals(0, playField.getLivingCells());

        playField.setPlayField(new int[][]{{0, 0, 1, 0}, {0, 0, 0, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}});
        assertEquals(3, playField.getLivingCells());

        playField.setPlayField(new int[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}});
        assertEquals(16, playField.getLivingCells());
    }

    @Test
    void stepForward() {
        int[][] startingField = {{0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 1, 1, 0}};

        int[][] nextField = {{0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 1, 1, 0}};

        playField.setPlayField(startingField);
        playField.stepForward();
        assertTrue(Arrays.deepEquals(nextField, playField.getPlayField()));


        startingField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field0_0.csv"));
        nextField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field0_1_3,2-3.csv"));
        playField.setPlayField(startingField);
        playField.stepForward();
        assertTrue(Arrays.deepEquals(nextField, playField.getPlayField()));


        nextField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field0_1_2-3-7,2-3-6.csv"));
        playField.setPlayField(startingField);
        playField.setReanimateRule(2, 3, 7);
        playField.setKeepLifeRule(2, 3, 6);
        playField.stepForward();
        assertTrue(Arrays.deepEquals(nextField, playField.getPlayField()));
    }

    @Test
    void stepTo() {
        int[][] startingField = {{0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0},
                {0, 0, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}};

        int[][] resultField = {{0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0, 0},
                {0, 1, 0, 0, 1, 0, 0, 0},
                {0, 0, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}};

        playField.setPlayField(startingField);
        playField.stepTo(3);
        assertTrue(Arrays.deepEquals(resultField, playField.getPlayField()));


        playField.resetGeneration();
        startingField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field1_0.csv"));
        resultField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field1_17_3,2-3.csv"));
        playField.setPlayField(startingField);
        playField.stepTo(17);
        assertTrue(Arrays.deepEquals(resultField, playField.getPlayField()));


        playField.resetGeneration();
        startingField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field1_0.csv"));
        resultField = presetManager.loadPreset(Path.of("resources/JUnitTests/PlayFieldTest/Field1_30_2-6,4-5.csv"));
        playField.setPlayField(startingField);
        playField.setReanimateRule(2, 6);
        playField.setKeepLifeRule(4, 5);
        playField.stepTo(30);
        assertTrue(Arrays.deepEquals(resultField, playField.getPlayField()));
    }
}