/**
 * A landmine can be in three different states (at least in this game..) which are modeled by this enum type.
 */
public enum MineState {
    /**
     * A mine that has neither been triggered or defused yet.
     */
    Active,
    /**
     * The mine has been successfuly defused.
     */
    Defused,
    /**
     * The player stepped on the mine :(
     */
    Exploded
}
