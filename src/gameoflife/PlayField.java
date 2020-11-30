package gameoflife;

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

}
