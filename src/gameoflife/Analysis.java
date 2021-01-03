package gameoflife;

import java.util.HashMap;

/**
 * Analysis for analysing the cell states during a game cycle
 *
 * @author Richard Krikler
 */
public class Analysis {

    /**
     * Store the amount of cells per generation
     */
    private final HashMap<Integer, Double[]> cellsPerGen = new HashMap<>();


    /**
     * Constructor: set the first element of the cellsPerGen Map
     */
    public Analysis() {
        cellsPerGen.put(0, new Double[]{0.0, 0.0, 0.0});
    }


    /**
     * Getter for the cells per generation HashMap
     * @return HashMap<Integer, Double[]>
     */
    public HashMap<Integer, Double[]> getCellsPerGen() {
        return cellsPerGen;
    }


    /**
     * Add the amount of living cells for the current generation
     *
     * @param livingCells amount of currently living cells
     */
    public void addCellCount(int generation, int livingCells) {
        // If the amount of saved generations is higher than the generation that has to be add
        // -> remove every generation after the added generation
        if (cellsPerGen.size() > generation + 1) {
            int cellsPerGenMapSize = cellsPerGen.size();
            for (int i = generation + 1; i < cellsPerGenMapSize; i++) {
                cellsPerGen.remove(i);
            }
        }

        double valueChange;
        double percentValueChange;
        if (generation == 0) {
            valueChange = livingCells;

            // If nothing has changed from the previous field
            // -> percent change = 0
            percentValueChange = valueChange == 0 ? 0 : 1;
        } else {
            double lastValue = cellsPerGen.get(generation - 1)[0];
            valueChange = livingCells - lastValue;

            if (lastValue == 0) {
                // check if the current living cells is 0 -> percent change = 0; otherwise 1
                percentValueChange = livingCells == 0 ? 0 : 1;
            } else {
                percentValueChange = valueChange / lastValue;
            }
        }
        // Multiply by 100 to get a more human readable value
        percentValueChange *= 100;

        Double[] newValue = new Double[]{(double) livingCells, valueChange, percentValueChange};

        // If the generation to add to the map is already in the map
        // -> replace the entry with the new values
        if (cellsPerGen.containsKey(generation)) {
            cellsPerGen.replace(generation, newValue);
        } else {
            cellsPerGen.put(generation, newValue);
        }
    }


    /**
     * Update the analysis GUI
     * if the analysis window is showing
     */
    public void updateAnalysisGui() {
        if (AnalysisGui.isShowing()) {
            AnalysisGui.update(cellsPerGen);
        }
    }
}
