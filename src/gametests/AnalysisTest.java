package gametests;

import gameoflife.Analysis;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for the Analysis Class
 *
 * @author Richard Krikler
 */
class AnalysisTest {

    /**
     * Store the amount of living cells per generation in the Analysis Object
     */
    private final Analysis analysis = new Analysis();


    /**
     * Compare two cellsPerGen HashMaps
     * Integer, Double[]
     *
     * @param cellsPerGen0 first Map
     * @param cellsPerGen1 second Map
     * @return true if the HashMaps are equal
     */
    public boolean equalsCellsPerGen(HashMap<Integer, Double[]> cellsPerGen0,
                                     HashMap<Integer, Double[]> cellsPerGen1) {
        if (!cellsPerGen0.keySet().equals(cellsPerGen1.keySet())) {
            return false;
        }

        for (HashMap.Entry<Integer, Double[]> entry : cellsPerGen0.entrySet()) {
            if (!Arrays.equals(entry.getValue(), cellsPerGen1.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }


    @Test
    void addCellCount() {
        HashMap<Integer, Double[]> cellsPerGen = new HashMap<>();

        // check initial map value at generation 0
        cellsPerGen.put(0, new Double[]{0.0, 0.0, 0.0});
        assertTrue(equalsCellsPerGen(cellsPerGen, analysis.getCellsPerGen()));

        // just adding a new value after last one
        cellsPerGen.put(1, new Double[]{5.0, 5.0, 100.0});
        analysis.addCellCount(1, 5);
        assertTrue(equalsCellsPerGen(cellsPerGen, analysis.getCellsPerGen()));

        // updating an existing value in the map
        cellsPerGen.put(1, new Double[]{8.0, 8.0, 100.0});
        analysis.addCellCount(1, 8);
        assertTrue(equalsCellsPerGen(cellsPerGen, analysis.getCellsPerGen()));

        // check what happens when a value at an already existing earlier generation changes
        analysis.addCellCount(2, 9);
        cellsPerGen.clear();
        cellsPerGen.put(0, new Double[]{12.0, 12.0, 100.0});
        analysis.addCellCount(0, 12);
        assertTrue(equalsCellsPerGen(cellsPerGen, analysis.getCellsPerGen()));

        // check if exception is thrown, when adding a value at an impossible generation
        assertThrows(NullPointerException.class, () -> analysis.addCellCount(10, 7));
    }
}