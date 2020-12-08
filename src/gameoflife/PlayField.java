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

        for (int y = 0; y < this.getDimensionY(); y++) {
            for (int x = 0; x < this.getDimensionX(); x++) {
                if (x + 1 < this.getDimensionX()) {
                    csv.append(this.getCell(x, y)).append(",");
                } else {
                    csv.append(this.getCell(x, y));
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

        for (int y = 0; y < dimensionY; y++) {
            if (!validLine.matcher(playField.get(y)).matches()) {
                return false;
            }

            String[] oneLineStAr = playField.get(y).split(",");
            if (oneLineStAr.length != dimensionX) {
                return false;
            }

            int[] oneLineIntAr = stringArToIntAr(oneLineStAr);
            newPlayField[y] = oneLineIntAr;
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
        for (int y = 0; y < input.length; y++) {
            result[y] = Integer.parseInt(input[y]);
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

        for (int y = 0; y < getDimensionY(); y++) {
            for (int x = 0; x < getDimensionX(); x++) {
                if (getCell(x, y) == 1) {
                    livingCells[0]++;
                }
            }
        }

        return livingCells[0];
    }

    /**
     * Get the play field to the next generation
     *
     * @return true if it was possible to go to the next generation
     */
    boolean stepForward() {
        int[][] newPlayField = new int[getDimensionY()][getDimensionX()];

        for (int y = 0; y < getDimensionY(); y++) {
            for (int x = 0; x < getDimensionX(); x++) {
                int surroundedLivingCells = 0;

                // Go through the surrounding cells of the cell at (x|y)
                for (int yRad = y - 1; yRad <= y + 1; yRad++) {
                    for (int xRad = x - 1; xRad <= x + 1; xRad++) {
                        // Make sure that the surrounded cell is not out of bounds or at the same place as the original cell
                        if ((!(yRad == y && xRad == x)) &&
                                (yRad >= 0 && yRad < getDimensionY()) &&
                                (xRad >= 0 && xRad < getDimensionX())) {
                            // Increase the surroundedLivingCells variable with the value of the cell
                            surroundedLivingCells += getCell(xRad, yRad);
                        }
                    }
                }

                // Go through the rules and change the value of the cell if necessary
                if (reanimateRule.contains(surroundedLivingCells) && getCell(x, y) == 0) {
                    newPlayField[y][x] = 1;
                } else if (keepLifeRule.contains(surroundedLivingCells) && getCell(x, y) == 1) {
                    newPlayField[y][x] = 1;
                } else {
                    newPlayField[y][x] = 0;
                }
            }
        }

        // If the current play field has not changed -> return false
        if (Arrays.deepEquals(playField, newPlayField)) {
            return false;
        } else {
            playField = newPlayField;
            generationCount++;
            return true;
        }
    }
}
