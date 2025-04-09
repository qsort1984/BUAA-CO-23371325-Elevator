public class Update extends RequestObject {
    private final int elevatorAId;
    private final int elevatorBId;

    public Update(int elevatorAId, int elevatorBId, int toFloor) {
        super(toFloor);
        this.elevatorAId = elevatorAId;
        this.elevatorBId = elevatorBId;
    }

    public int getElevatorAId() {
        return elevatorAId;
    }

    public int getElevatorBId() {
        return elevatorBId;
    }
}
