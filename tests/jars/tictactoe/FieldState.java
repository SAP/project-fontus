/**
 * Represents the different states a field can be in.
 */
public enum FieldState {

    /**
     * The field is empty.
     */
    Empty,
    /**
     * Player 1 picked this field.
     */
    Player1,
    /**
     * Player 2 picked this field.
     */
    Player2;

    @Override
    public String toString() {
        switch (this) {
            case Empty:
                return " ";
            case Player1:
                return "X";
            case Player2:
                return "O";
        }
        return "?"; // Can't happen
    }
}
