package gameoflife;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
     * Stores all previous play field arrays
     */
    private final HashMap<Integer, int[][]> playFields = new HashMap<>();

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
    private final HashSet<Integer> reanimateRule = new HashSet<>();

    /**
     * Stores the numbers of living cells needed to keep a cell alive
     */
    private final HashSet<Integer> keepLifeRule = new HashSet<>();

    /**
     * Stores the data and the functions for the game analysis
     */
    private final Analysis analysis = new Analysis();


    /**
     * PlayField Constructor
     *
     * @param dimensionX x dimension of the play field
     * @param dimensionY y dimension of the play field
     */
    public PlayField(int dimensionX, int dimensionY) {
        setSize(dimensionX, dimensionY);
    }

    /**
     * Extended PlayField Constructor
     *
     * @param dimensionX    x dimension of the play field
     * @param dimensionY    y dimension of the play field
     * @param gameSpeed     floating point value, which contains the game speed
     * @param reanimateRule int array of the needed cells for the reanimate rule
     * @param keepLifeRule  int array of the needed cells for the keep life rule
     */
    public PlayField(int dimensionX, int dimensionY,
                     float gameSpeed,
                     int[] reanimateRule, int[] keepLifeRule) {
        setSize(dimensionX, dimensionY);
        setGameSpeed(gameSpeed);
        setReanimateRule(reanimateRule);
        setKeepLifeRule(keepLifeRule);
    }


    /**
     * Override the playField array
     *
     * @param playField int[][] array which contains the play field
     */
    public void setPlayField(int[][] playField) {
        this.playField = playField;
    }

    /**
     * Get the play field array
     *
     * @return int[][] array which contains the play field
     */
    public int[][] getPlayField() {
        return playField;
    }


    /**
     * Override the playField array with a new int array, with new dimensions.
     *
     * @param dimensionX x dimension of the play field
     * @param dimensionY y dimension of the play field
     */
    public void setSize(int dimensionX, int dimensionY) {
        playField = new int[dimensionY][dimensionX];
    }


    /**
     * Get the x dimension of the play field
     *
     * @return x dimension
     */
    public int getDimensionX() {
        if (playField.length > 0) {
            return playField[0].length;
        } else {
            return 0;
        }
    }

    /**
     * Get the y dimension of the play field
     *
     * @return y dimension
     */
    public int getDimensionY() {
        return playField.length;
    }


    /**
     * Get the stored value of a specific cell.
     *
     * @param posX x position of the cell
     * @param posY y position of the cell
     * @return cell value
     */
    public int getCell(int posX, int posY) {
        return playField[posY][posX];
    }

    /**
     * Set value of a specific cell.
     *
     * @param posX  x position of the cell
     * @param posY  y position of the cell
     * @param value of the cell; 1 = alive, 0 = dead
     */
    public void setCell(int posX, int posY, int value) {
        playField[posY][posX] = value;
    }


    /**
     * Get the current generation
     *
     * @return integer of the generation
     */
    public int getGeneration() {
        return generationCount;
    }

    /**
     * Reset the generation count to zero
     */
    public void resetGeneration() {
        generationCount = 0;
    }


    /**
     * Set the value of the Game Speed
     *
     * @param gameSpeed floating point value, which contains the game speed
     */
    public void setGameSpeed(float gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    /**
     * Get the current Game Speed of the game
     *
     * @return floating point value of the game speed
     */
    public float getGameSpeed() {
        return gameSpeed;
    }


    /**
     * Set the field, that stores the needed cells for reanimation
     *
     * @param cellsNeeded cells needed to reanimate a dead cell
     */
    public void setReanimateRule(int... cellsNeeded) {
        reanimateRule.clear();
        for (int neededCell : cellsNeeded) {
            reanimateRule.add(neededCell);
        }
    }

    /**
     * Call the setReanimateRule method with an input String
     *
     * @param cellsNeeded String of the needed cells, separated via ","
     */
    public void setReanimateRule(String cellsNeeded) {
        setReanimateRule(GuiLogic.stringArToIntAr(cellsNeeded.split(",")));
    }

    /**
     * Get the cells needed for reanimation.
     *
     * @return String of the Reanimation Rule
     */
    public String getReanimateRule() {
        return reanimateRule.toString().replaceAll("[\\[\\]\\s]", "");
    }

    /**
     * Set the field, that stores the needed cells for keeping a cell
     *
     * @param cellsNeeded cells needed to keep a cell alive
     */
    public void setKeepLifeRule(int... cellsNeeded) {
        keepLifeRule.clear();
        for (int neededCell : cellsNeeded) {
            keepLifeRule.add(neededCell);
        }
    }

    /**
     * Call the setKeepLifeRule method with an input String
     *
     * @param cellsNeeded String of the needed cells, separated via ","
     */
    public void setKeepLifeRule(String cellsNeeded) {
        setKeepLifeRule(GuiLogic.stringArToIntAr(cellsNeeded.split(",")));
    }

    /**
     * Get the cells needed to keep a cell alive.
     *
     * @return String of the Keep Alive Rule
     */
    public String getKeepLifeRule() {
        return keepLifeRule.toString().replaceAll("[\\[\\]\\s]", "");
    }


    /**
     * Get the amount of living cells in the play field
     *
     * @return integer value with the counted cells
     */
    public int getLivingCells() {
        int livingCells = 0;

        for (int y = 0; y < getDimensionY(); y++) {
            for (int x = 0; x < getDimensionX(); x++) {
                if (getCell(x, y) == 1) {
                    livingCells++;
                }
            }
        }

        analysis.addCellCount(getGeneration(), livingCells);
        analysis.updateAnalysisGui();

        return livingCells;
    }


    /**
     * Place living cells randomly on the play field
     */
    public void placeRandomly() {
        setSize(getDimensionX(), getDimensionY());

        int playFieldArea = getDimensionX() * getDimensionY();
        double min = playFieldArea * .3;
        double max = playFieldArea * .5;
        // Random amount of cells being placed (range between 30% to 50% of the play field)
        int cellAmount = (int) ((Math.random() * (max - min)) + min);

        int cellCounter = 0;
        while (cellCounter != cellAmount) {
            int posX = (int) (Math.random() * getDimensionX());
            int posY = (int) (Math.random() * getDimensionY());

            if (getCell(posX, posY) == 0) {
                setCell(posX, posY, 1);
                cellCounter++;
            }
        }
    }


    /**
     * Get the play field to the next generation
     *
     * @return true if it was possible to go to the next generation
     */
    public boolean stepForward() {
        int[][] newPlayField = new int[getDimensionY()][getDimensionX()];

        for (int y = 0; y < getDimensionY(); y++) {
            for (int x = 0; x < getDimensionX(); x++) {
                int surroundedLivingCells = 0;

                // Go through the surrounding cells of the cell at (x|y)
                for (int yRad = y - 1; yRad <= y + 1; yRad++) {
                    for (int xRad = x - 1; xRad <= x + 1; xRad++) {
                        // Make sure that the surrounded cell is not out of bounds
                        // or at the same place as the original cell
                        if ((!(yRad == y && xRad == x))
                                && (yRad >= 0 && yRad < getDimensionY())
                                && (xRad >= 0 && xRad < getDimensionX())) {
                            // Increase the surroundedLivingCells variable
                            // with the value of the cell
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
            playFields.put(getGeneration(), playField);
            playField = newPlayField;
            generationCount++;
            return true;
        }
    }

    /**
     * Get the play field to a specific generation
     *
     * @param generation to which the play field is being updated
     * @return true if it was possible to go to the last generation
     */
    public boolean stepTo(int generation) {
        if (generation < 0 || getGeneration() == generation) {
            return false;
        }

        if (getGeneration() > generation) {
            playField = playFields.get(generation);
            for (int i = generation + 1; i < playFields.size(); i++) {
                playFields.remove(i);
            }

            generationCount = generation;
        } else {
            int startAt = getGeneration();
            for (int i = startAt; i < generation; i++) {
                stepForward();
                analysis.addCellCount(getGeneration(), getLivingCells());
            }
        }

        return true;
    }


    /**
     * Call the updateAnalysis function in the Analysis class
     */
    public void updateAnalysisGui() {
        analysis.updateAnalysisGui();
    }
}
