/**
 * The Tic-Tac-Toe playing area.
 */
class GameArea {
    private FieldState[][] field;
    private static final int FIELD_SIZE = 3;

    /**
     * Initializes the game area as empty.
     */
    private void initField() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                this.field[i][j] = FieldState.Empty;
            }
        }
    }

    /**
     * Constructs a new and empty playing area.
     */
    GameArea() {
        this.field = new FieldState[FIELD_SIZE][FIELD_SIZE];
        initField();
    }

    /**
     * Tests whether the given coordinate is within the bounds of the game area.
     * @param coordinate The coordinates to check.
     * @return True if it's inside our game area.
     */
    private boolean validateCoordinate(Coordinate coordinate) {
        int row = coordinate.getRow();
        int column = coordinate.getColumn();
        return row >= 0 && row < FIELD_SIZE && column >= 0 && column < FIELD_SIZE;
    }


    /**
     * Allows a player to make a move.
     * @param coordinate Where to move.
     * @param player Who moves there.
     * @return Was the move successful? The Field has to be free for the player to be able to pick it.
     */
    boolean pickField(Coordinate coordinate, Player player) {
        if (!validateCoordinate(coordinate)) {
            return false;
        }
        FieldState state = player.getFieldState();
        int row = coordinate.getRow();
        int column = coordinate.getColumn();
        if (isFieldFree(row, column)) {
            this.field[row][column] = state;
            return true;
        }
        return false;
    }

    private boolean isFieldFree(int i, int j) {
        return this.field[i][j] == FieldState.Empty;
    }

    /**
     * Is the game complete?
     * @return True if either player has won or the game ended with a draw.
     */
    public boolean isGameOver() {
        return isWinner(Player.PlayerOne) || isWinner(Player.PlayerTwo) || isFieldFull();
    }

    /**
     * Is the game area full?
     * @return True if no empty fields are left.
     */
    public boolean isFieldFull() {
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                if (isFieldFree(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether a player won the game.
     * @param player The player to check for
     * @return Did the player win?
     */
    public boolean isWinner(Player player) {
        FieldState state = player.getFieldState();
        return allHorizontal(state) || allVertical(state) || allDiagonal(state);
    }

    private boolean allHorizontal(FieldState toCheck) {
        for (int j = 0; j < FIELD_SIZE; j++) {
            int i = 0;
            if (this.field[i][j] == toCheck) {
                boolean allSame = true;
                for (i = 1; i < FIELD_SIZE; i++) {
                    allSame = allSame && (this.field[i][j] == toCheck);
                }
                if (allSame) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allVertical(FieldState toCheck) {
        for (int i = 0; i < FIELD_SIZE; i++) {
            int j = 0;
            if (this.field[i][j] == toCheck) {
                boolean allSame = true;
                for (j = 1; j < FIELD_SIZE; j++) {
                    allSame = allSame && (this.field[i][j] == toCheck);
                }
                if (allSame) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allDiagonal(FieldState toCheck) {
        if (this.field[0][0] == toCheck && this.field[1][1] == toCheck && this.field[2][2] == toCheck) {
            return true;
        }
        if (this.field[0][2] == toCheck && this.field[1][1] == toCheck && this.field[2][0] == toCheck) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String area = "";
        for (int j = 0; j < 7; j++) {
            area += "-";
        }

        area += "\n";
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                area += "|";
                area += this.field[i][j].toString();
            }
            area += "|\n";
            for (int j = 0; j < 7; j++) {
                area += "-";
            }
            area += "\n";
        }

        return area;
    }
}
