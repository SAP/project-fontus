public class Plains implements Square {

    private final int neighboringMines;
    private boolean clicked = false;

    Plains(int neighboringMines) {
        this.neighboringMines = neighboringMines;
    }

    @Override
    public boolean click() {
        this.clicked = true;
        return true;
    }

    @Override
    public boolean rightClick() {
        return false;
    }

    @Override
    public boolean isMine() {
        return false;
    }

    @Override
    public String toDebugString() {
        return String.format("%d", this.neighboringMines);
    }

    @Override
    public String toString(){
        if(clicked) {
            return String.format("%d", this.neighboringMines);
        } else {
            return " ";
        }
    }
}
