public interface Dispatcher {
    int dispatchElev(Person person);

    default boolean elevatorNotAvailable(Person person, ElevatorThread elevator) {
        if (elevator.getSize() >= 20) {
            return true;
        }
        int transferFloor = elevator.getTransferFloor();
        int fromFloor = person.getFromFloor();
        if (elevator.isUpdated()) {
            if (transferFloor > fromFloor && elevator.typeA()) {
                return true;
            }
            if (transferFloor < fromFloor && elevator.typeB()) {
                return true;
            }
            if (transferFloor == fromFloor) {
                int toFloor = person.getToFloor();
                return (elevator.typeB() && toFloor > transferFloor) ||
                        (elevator.typeA() && toFloor < transferFloor);
            }
        }

        return false;
    }
}
