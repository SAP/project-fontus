/**
 *  Represents a Coordinate inside the playing area.
 */
class Coordinate {

    private int row;
    private int column;

    /**
     * The row index.
     * @return The row index.
     */
    public int getRow() {
        return row;
    }

    /**
     * The column index.
     * @return The column index.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Create a new coordinate instance. The parameters have to be validated earlier.
     * @param row The row index.
     * @param column The column index.
     */
    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return "Coordinate{row=" + row + ", column=" + column + '}';
    }
}
