public class TempSchedule extends RequestObject {
    private final int elevatorId;
    private final int tempSpeed;

    public TempSchedule(int elevatorId, int tempSpeed, int toFloor) {
        super(toFloor);
        this.elevatorId = elevatorId;
        this.tempSpeed = tempSpeed;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getTempSpeed() {
        return tempSpeed;
    }
}
