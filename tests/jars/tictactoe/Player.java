/**
 * Represents the two different players.
 */
public enum Player {
    /**
     * The first player.
     */
    PlayerOne,
    /**
     * The second player.
     */
    PlayerTwo;

    @Override
    public String toString() {
        switch (this) {
            case PlayerOne:
                return "Spieler 1";
            case PlayerTwo:
                return "Spieler 2";
        }
        return "Error"; //Can't happen..
    }

    /**
     * Maps a player to his/her field state.
     * @return The player's field state.
     */
    public FieldState getFieldState() {
        switch (this) {
            case PlayerOne:
                return FieldState.Player1;
            case PlayerTwo:
                return FieldState.Player2;
        }
        return FieldState.Empty; //can't happen..
    }
}
