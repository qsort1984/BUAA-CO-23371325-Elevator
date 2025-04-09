import java.util.ArrayList;

public class JudgeDispatcher implements Dispatcher {
    private final ArrayList<ElevatorThread> elevatorThreads;

    public JudgeDispatcher(ArrayList<ElevatorThread> elevatorThreads) {
        this.elevatorThreads = elevatorThreads;
    }

    public int dispatchElev(Person person) {
        // todo 加入双轿厢如何调度
        // todo 调度策略如何优化
        int elevatorId = 1;
        int minSize = 200; //* 乘客最多100人
        for (ElevatorThread elevator : elevatorThreads) {
            if (!elevator.isScheduling()) {
                if (elevator.getSize() >= 20) {
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
}
