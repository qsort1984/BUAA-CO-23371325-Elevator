import java.util.concurrent.atomic.AtomicInteger;

public class TransferFloor {
    private enum State {
        OCCUPIED,
        EMPTY
    }

    private final AtomicInteger floor;
    private State state;

    public TransferFloor(int floor) {
        this.floor = new AtomicInteger(floor);
    }

    public int getFloor() {
        return floor.get();
    }

    public synchronized void setOccupied() {
        while (this.state == State.OCCUPIED) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.state = State.OCCUPIED;
    }

    public synchronized void setEmpty() {
        this.state = State.EMPTY;
        notifyAll();
    }
}
