/**
 * A square in the playing area of the minesweeper game.
 */
interface Square {
    /**
     * Stepping onto this field.
     * @return false if the player stepped on a mine, true if the movement didn't kill anybody.
     */
    boolean click();

    /**
     * Trying to defuse a mine.
     * @return True if this Square contained a mine which is now defused.
     */
    boolean rightClick();

    /**
     * Test whether the field contains a mine.
     * @return Whether this instance of Square is a mine.
     */
    boolean isMine();

    /**
     * Prints the underlying field contents, regardless of whether the player interacted with it yet. For debug purposes.
     * @return The String representation of the underlying game state.
     */
    String toDebugString();
}
