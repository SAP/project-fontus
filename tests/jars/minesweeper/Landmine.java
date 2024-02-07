/**
 * A square containing a landmine.
 */
public class Landmine implements Square {

    private MineState state;

    Landmine() {
        this.state = MineState.Active;
    }

    @Override
    public boolean click() {
        this.state = MineState.Exploded;
        return false;
    }

    @Override
    public boolean rightClick() {
        this.state = MineState.Defused;
        return true;
    }

    @Override
    public boolean isMine() {
        return true;
    }

    @Override
    public String toDebugString() {
        return "X";
    }

    @Override
    public String toString() {
        switch(this.state) {
            case Active:
                return " ";
            case Defused:
                return "!";
            case Exploded:
                return "X";
            default:
                throw new IllegalStateException("Invalid Mine state");
        }

    }
}
