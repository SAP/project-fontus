import java.util.Random;

class MineSweeperField {

    private final int size;
    private final int mineCount;
    private final Random rng;
    private final Square[][] area;

    MineSweeperField(int size, int mineCount, int initiallyVisible) {
        this.rng = new Random();
        this.rng.setSeed(123456789);
        this.size = size;
        this.area = new Square[size][size];
        this.mineCount = mineCount;
        this.setupField(initiallyVisible);
    }

    private void setupField(int initiallyVisible) {
        this.placeMines();
        this.completeField();
        this.showPlains(initiallyVisible);
    }

    private void showPlains(int initiallyVisible) {
        int showing = 0;
        while (showing < initiallyVisible) {
            int x = rng.nextInt(this.size);
            int y = rng.nextInt(this.size);

            Square current = this.area[x][y];
            if (!current.isMine()) {
                current.click();
                showing++;
            }
        }
    }

    private void placeMines() {
        int minesPlaced = 0;
        while (minesPlaced < this.mineCount) {
            int x = rng.nextInt(this.size);
            int y = rng.nextInt(this.size);
            if (this.area[x][y] == null) {
                // place mine
                this.area[x][y] = new Landmine();
                minesPlaced++;
            }
        }
    }

    /**
     * Tests whether the coordinate pair given as the arguments is valid.
     * @param x The row coordinate
     * @param y The column coordinate
     * @return True if (x,y) is inside teh playing area.
     */
    private boolean isValid(int x, int y) {
        return (x >= 0 && x < this.size) && (y >= 0 && y < this.size);
    }

    /**
     * Counts the number of mines surrounding the field (x,y)
     * @param x The row coordinate.
     * @param y The column coordinate.
     * @return The number of mines in directly neighboured fields.
     */
    private int countMines(int x, int y) {
        int surroundingMines = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int xAdjusted = x + i;
                int yAdjusted = y + j;
                if (isValid(xAdjusted, yAdjusted)) {
                    Square current = this.area[xAdjusted][yAdjusted];
                    if (current != null && current.isMine()) {
                        surroundingMines++;
                    }
                }
            }
        }
        return surroundingMines;
    }

    /**
     * Fills up the playing area with plains.
     */
    private void completeField() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (this.area[i][j] == null) {
                    int surroundingMines = countMines(i, j);
                    this.area[i][j] = new Plains(surroundingMines);
                }
            }
        }
    }

    /**
     * Validates a coordinate input.
     * @param n the row
     * @param c The column
     * @return True if (n,c) are inside the playing area.
     */
    boolean isValidCoordinate(int n, char c) {
        boolean numberIsValid = n >= 0 && n < size;
        boolean charIsValid = c >= 'A' && c < ('A' + this.size);
        return numberIsValid && charIsValid;
    }

    /**
     * Simulates a left click action in the playing area at the provided coordinate.
     * @param coordinate Where to click.
     * @return True if the action didn't lead to us getting blown up.
     */
    boolean leftClick(Coordinate coordinate) {
        System.out.println(String.format("Moving onto '%s'", coordinate));
        int row = coordinate.getRow();
        int column = coordinate.getColumn();
        return this.area[row][column].click();
    }

    /**
     * Simulates a right click action in the playing area at the provided coordinate.
     * @param coordinate Where to click.
     * @return True if the action led to a successful defusing of a landmine.
     */
    boolean rightClick(Coordinate coordinate) {
        System.out.println(String.format("Trying to defuse '%s'", coordinate));
        int row = coordinate.getRow();
        int column = coordinate.getColumn();
        return this.area[row][column].rightClick();
    }

    /**
     * How many mines are placed inside the playing area.
     * @return Total number of mines.
     */
    int getMineCount() {
        return this.mineCount;
    }

    /**
     * Creates a textual representation of the playing area.
     * @param debug Whether to show all fields, regardless of whether the user already interacted with them.
     * @return The playing area in textual form.
     */
    private String formatGame(boolean debug) {
        String col = " ";
        for (int i = 0; i < this.size; i++) {
            char colName = (char) ('A' + i);
            col += "|" + colName;
        }

        String game = col + "\n";
        for (int i = 0; i < this.size; i++) {
            String row = Integer.toString(i);
            for (int j = 0; j < this.size; j++) {
                String field = debug ? this.area[i][j].toDebugString() : this.area[i][j].toString();
                row += "|" + field;
            }
            game += row + "\n";
        }
        return game;
    }

    String toDebugString() {
        return this.formatGame(true);
    }

    @Override
    public String toString() {
        return this.formatGame(false);
    }
}
