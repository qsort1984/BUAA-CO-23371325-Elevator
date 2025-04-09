import java.util.ArrayList;

public class LookStrategy implements Strategy {
    private final ElevatorThread elevator;
    private final WaitQueue waitQueue;
    private final ArrayList<TempSchedule> tempSchedules;

    public LookStrategy(ElevatorThread elevator, WaitQueue waitQueue,
        ArrayList<TempSchedule> tempSchedules) {
        this.elevator = elevator;
        this.waitQueue = waitQueue;
        this.tempSchedules = tempSchedules;
    }

    public ElevatorState getNextState() {
        if (elevator.getUpdateStartSign() && !elevator.isUpdating()) {
            return updatePrepare();
        }
        if (elevator.isUpdating()) {
            boolean isUpdateEnd = elevator.getUpdateEndSign();
            return isUpdateEnd ? ElevatorState.WAITING : ElevatorState.UPDATEEND;
        }

        if (!tempSchedules.isEmpty()) {
            return schedule();
        }

        if (elevator.isUpdated() && elevator.hasArriveTransFloor()) {
            return ElevatorState.TRANSFER;
        }

        int curNum = elevator.getCurrentNum();
        boolean isOpen = elevator.isOpen();

        if (isOpen) {
            return canClose() ? ElevatorState.CLOSE : ElevatorState.WAITING;
        }

        if (canOpen()) {
            return ElevatorState.OPEN;
        }

        if (curNum != 0) {
            return reverseSign() ? ElevatorState.REVERSE : ElevatorState.MOVE;
        } else {
            if (waitQueue.isEmpty()) {
                return waitQueue.isEnd() ? ElevatorState.END : ElevatorState.WAITING;
            } else {
                return hasReqInDirection() ? ElevatorState.MOVE : ElevatorState.REVERSE;
            }
        }
    }

    private ElevatorState updatePrepare() {
        int curNum = elevator.getCurrentNum();
        boolean isOpen = elevator.isOpen();

        if (curNum == 0) {
            return isOpen ? ElevatorState.CLOSE : ElevatorState.UPDATE;
        } else {
            return ElevatorState.OPEN;
        }
    }

    private ElevatorState schedule() {
        int curFloor = elevator.getCurrentFloor();
        int toFloor = tempSchedules.get(0).getToFloor();
        boolean isUp = elevator.isUp();
        boolean isOpen = elevator.isOpen();
        boolean isScheduled = elevator.isScheduling();

        if (isScheduled) {
            if (curFloor == toFloor) {
                return isOpen ? ElevatorState.CLOSE : ElevatorState.OPEN;
            } else {
                boolean sameDirection = sameDirection(curFloor, toFloor, isUp);
                return sameDirection ? ElevatorState.MOVE : ElevatorState.REVERSE;
            }
        } else {
            return isOpen ? ElevatorState.CLOSE : ElevatorState.SCHEDULE;
        }
    }

    private boolean sameDirection(int curFloor, int toFloor, boolean isUp) {
        return (isUp && curFloor < toFloor) || (!isUp && curFloor > toFloor);
    }

    private boolean canOpen() {
        ArrayList<Person> curPeople = elevator.getCurrentPeople();
        int curFloor = elevator.getCurrentFloor();
        for (Person person : curPeople) {
            if (person.getToFloor() == curFloor) {
                return true;
            }
        }

        if (curPeople.size() >= 6) {
            return false;
        }

        return waitQueue.canTake(curFloor, elevator.isUp());
    }

    private boolean canClose() {
        ArrayList<Person> curPeople = elevator.getCurrentPeople();
        return !curPeople.isEmpty() || !waitQueue.isEmpty() || waitQueue.isEnd();
    }

    private boolean hasReqInDirection() {
        return waitQueue.hasReqInDirection(elevator.getCurrentFloor(), elevator.isUp());
    }

    private boolean reverseSign() {
        boolean isUp = elevator.isUp();
        int curFloor = elevator.getCurrentFloor();
        return isUp ? curFloor > getHighestFloor() : curFloor < getLowestFloor();
    }

    private int getHighestFloor() {
        ArrayList<Person> curPeople = elevator.getCurrentPeople();
        int highestFloor = curPeople.size() >= 6 ? -3 : waitQueue.getHighestFloor();
        for (Person person : curPeople) {
            if (person.getToFloor() > highestFloor) {
                highestFloor = person.getToFloor();
            }
        }

        return highestFloor;
    }

    private int getLowestFloor() {
        ArrayList<Person> curPeople = elevator.getCurrentPeople();
        int lowestFloor = curPeople.size() >= 6 ? 7 : waitQueue.getLowestFloor();
        for (Person person : curPeople) {
            if (person.getToFloor() < lowestFloor) {
                lowestFloor = person.getToFloor();
            }
        }

        return lowestFloor;
    }

    public ArrayList<Person> getOutPeople() {
        ArrayList<Person> outPeople = new ArrayList<>();
        boolean isScheduling = elevator.isScheduling();
        boolean isUpdating = elevator.isUpdating();
        if (isScheduling || isUpdating) {
            outPeople = new ArrayList<>(elevator.getCurrentPeople());
        } else {
            ArrayList<Person> curPeople = elevator.getCurrentPeople();
            for (Person person : curPeople) {
                if (person.getToFloor() == elevator.getCurrentFloor()) {
                    outPeople.add(person);
                }
            }
        }

        return outPeople;
    }

    public ArrayList<Person> getInPeople() {
        ArrayList<Person> inPeople = new ArrayList<>();
        boolean isScheduling = elevator.isScheduling();
        boolean isUpdating = elevator.isUpdating();
        if (!isScheduling && !isUpdating) {
            inPeople = waitQueue.takeElev(elevator.getCurrentFloor(),
            6 - elevator.getCurrentNum(), elevator.isUp());
        }

        return inPeople;
    }
}
