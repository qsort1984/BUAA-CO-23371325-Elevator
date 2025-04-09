public class Person extends RequestObject {
    private final int id;
    private final int priority;
    private int fromFloor;

    public Person(int id, int priority, int fromFloor, int toFloor) {
        super(toFloor);
        this.id = id;
        this.priority = priority;
        this.fromFloor = fromFloor;
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public boolean isUp() {
        return fromFloor < getToFloor();
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", priority=" + priority +
                ", fromFloor=" + fromFloor +
                ", toFloor=" + getToFloor() +
                '}';
    }
}
