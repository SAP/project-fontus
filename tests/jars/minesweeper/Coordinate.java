/**
 * A coordinate inside the minesweeper game.
 */
class Coordinate {
    private final int row;
    private final int column;

    Coordinate(int row, char column) {
        this.row = row;
        this.column = columnAsInt(column);
    }

    private static int columnAsInt(char column) {
        return column - 'A';
    }

    private static char columnAsChar(int column) {
        return (char) (column + 'A');
    }

    int getRow() {
        return this.row;
    }

    int getColumn() {
        return this.column;
    }

    @Override
    public String toString() {
        return String.format("%c%d", columnAsChar(this.column), this.row);
    }

}
