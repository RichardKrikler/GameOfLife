package gameoflife;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * PlayField of the Game Of Life
 *
 * @author Richard Krikler
 */
public class PlayField {
    /**
     * Stores the play field.
     * 2D int Array
     */
    private int[][] playField;

    /**
     * Stores the current number of generation
     */
    private int generationCount = 0;

    /**
     * Stores the Game Speed of the game
     */
    private float gameSpeed;

    /**
     * Stores the numbers of living cells needed to make a dead cell alive
     */
    private HashSet<Integer> reanimateRule = new HashSet<>();

    /**
     * Stores the numbers of living cells needed to keep a cell alive
     */
    private HashSet<Integer> keepLifeRule = new HashSet<>();


    /**
     * PlayField Constructor
     *
     * @param dimensionX x dimension of the play field
     * @param dimensionY y dimension of the play field
     */
    PlayField(int dimensionX, int dimensionY) {
        setSize(dimensionX, dimensionY);
    }


    /**
     * Override the playField array with a new int array, with new dimensions.
     *
     * @param dimensionX x dimension of the play field
     * @param dimensionY y dimension of the play field
     */
    void setSize(int dimensionX, int dimensionY) {
        this.playField = new int[dimensionY][dimensionX];
    }


    /**
     * Get the x dimension of the play field
     *
     * @return x dimension
     */
    int getDimensionX() {
        return playField.length > 0 ? playField[0].length : 0;
    }

    /**
     * Get the y dimension of the play field
     *
     * @return y dimension
     */
    int getDimensionY() {
        return playField.length;
    }


    /**
     * Get the stored value of a specific cell.
     *
     * @param posX x position of the cell
     * @param posY y position of the cell
     * @return cell value
     */
    int getCell(int posX, int posY) {
        return this.playField[posY][posX];
    }

    /**
     * Set value of a specific cell.
     *
     * @param posX x position of the cell
     * @param posY y position of the cell
     */
    void setCell(int posX, int posY, int value) {
        this.playField[posY][posX] = value;
    }


    /**
     * Get the current generation
     *
     * @return integer of the generation
     */
    int getGeneration() {
        return this.generationCount;
    }


    /**
     * Set the value of the Game Speed
     *
     * @param gameSpeed floating point value, which contains the game speed
     */
    void setGameSpeed(float gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    /**
     * Get the current Game Speed of the game
     *
     * @return floating point value of the game speed
     */
    float getGameSpeed() {
        return this.gameSpeed;
    }


    /**
     * Set the field, that stores the needed cells for reanimation
     *
     * @param cellsNeeded cells needed to reanimate a dead cell
     */
    void setReanimateRule(int... cellsNeeded) {
        this.reanimateRule.clear();
        for (int neededCell : cellsNeeded) {
            this.reanimateRule.add(neededCell);
        }
    }

    /**
     * Call the setReanimateRule method with an input String
     *
     * @param cellsNeeded String of the needed cells, separated via ","
     */
    void setReanimateRule(String cellsNeeded) {
        setReanimateRule(stringArToIntAr(cellsNeeded.split(",")));
    }

    /**
     * Get the cells needed for reanimation.
     *
     * @return String of the Reanimation Rule
     */
    String getReanimateRule() {
        return this.reanimateRule.toString().replaceAll("[\\[\\]\\s]", "");
    }

    /**
     * Set the field, that stores the needed cells for keeping a cell
     *
     * @param cellsNeeded cells needed to keep a cell alive
     */
    void setKeepLifeRule(int... cellsNeeded) {
        this.keepLifeRule.clear();
        for (int neededCell : cellsNeeded) {
            this.keepLifeRule.add(neededCell);
        }
    }

    /**
     * Call the setKeepLifeRule method with an input String
     *
     * @param cellsNeeded String of the needed cells, separated via ","
     */
    void setKeepLifeRule(String cellsNeeded) {
        setKeepLifeRule(stringArToIntAr(cellsNeeded.split(",")));
    }

    /**
     * Get the cells needed to keep a cell alive.
     *
     * @return String of the Keep Alive Rule
     */
    String getKeepLifeRule() {
        return this.keepLifeRule.toString().replaceAll("[\\[\\]\\s]", "");
    }


    /**
     * Convert the play field to CSV format
     *
     * @return String containing the play field
     */
    String convertToCSV() {
        StringBuilder csv = new StringBuilder();

        for (int i = 0; i < this.getDimensionY(); i++) {
            for (int j = 0; j < this.getDimensionX(); j++) {
                if (j + 1 < this.getDimensionX()) {
                    csv.append(this.getCell(j, i)).append(",");
                } else {
                    csv.append(this.getCell(j, i));
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
    boolean loadFromCSV(List<String> playField) {
        if (playField.size() < 1) {
            return false;
        }

        Pattern validLine = Pattern.compile("^([10],)*[10]$");
        int[][] newPlayField = new int[playField.size()][];
        int dimensionX = playField.get(0).split(",").length;
        int dimensionY = playField.size();

        for (int i = 0; i < dimensionY; i++) {
            if (!validLine.matcher(playField.get(i)).matches()) {
                return false;
            }

            String[] oneLineStAr = playField.get(i).split(",");
            if (oneLineStAr.length != dimensionX) {
                return false;
            }

            int[] oneLineIntAr = stringArToIntAr(oneLineStAr);
            newPlayField[i] = oneLineIntAr;
        }

        this.playField = newPlayField;
        return true;
    }

    /**
     * Convert a String array to an int array
     *
     * @param input String array
     * @return int array
     */
    private int[] stringArToIntAr(String[] input) {
        int[] result = new int[input.length];
        for (int j = 0; j < input.length; j++) {
            result[j] = Integer.parseInt(input[j]);
        }
        return result;
    }


    /**
     * Get the amount of living cells in the play field
     *
     * @return integer value with the counted cells
     */
    int getLivingCells() {
        int[] livingCells = {0};

        for (int i = 0; i < getDimensionY(); i++) {
            for (int j = 0; j < getDimensionX(); j++) {
                if (getCell(j, i) == 1) {
                    livingCells[0]++;
                }
            }
        }

        return livingCells[0];
    }
}
