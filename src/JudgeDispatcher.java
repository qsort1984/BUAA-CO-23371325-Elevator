import java.util.ArrayList;

public class JudgeDispatcher implements Dispatcher {
    private final ArrayList<ElevatorThread> elevatorThreads;

    public JudgeDispatcher(ArrayList<ElevatorThread> elevatorThreads) {
        this.elevatorThreads = elevatorThreads;
    }

    public int dispatchElev(Person person) {
        // todo 调度策略如何优化
        //* 或许可以考虑打分策略？综合考虑电梯乘客量加等待人数，当前位置，是否为双轿厢
        int elevatorId = 1;
        int minSize = 200; //* 乘客最多100人
        for (ElevatorThread elevator : elevatorThreads) {
            if (!elevator.isScheduling() && !elevator.isUpdating()) {
                if (continueSign(person, elevator)) {
                    continue;
                }
                if (elevator.getSize() < minSize) {
                    elevatorId = elevator.getElevatorId();
                    minSize = elevator.getSize();
                }
            }
        }

        return minSize == 200 ? -1 : elevatorId;
    }

    private boolean continueSign(Person person, ElevatorThread elevator) {
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
