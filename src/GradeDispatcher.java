import java.util.ArrayList;

public class GradeDispatcher implements Dispatcher {
    private final ArrayList<ElevatorThread> elevatorThreads;

    public GradeDispatcher(ArrayList<ElevatorThread> elevatorThreads) {
        this.elevatorThreads = elevatorThreads;
    }

    public int dispatchElev(Person person) {
        int elevatorId = 1;
        int maxScore = 0; //* 乘客最多100人
        for (ElevatorThread elevator : elevatorThreads) {
            if (!elevator.isScheduling() && !elevator.isUpdating()) {
                if (elevatorNotAvailable(person, elevator)) {
                    continue;
                }
                int score = elevator.getScore(person);
                if (score > maxScore) {
                    maxScore = score;
                    elevatorId = elevator.getElevatorId();
                }
            }
        }

        return maxScore == 0 ? -1 : elevatorId;
    }
}
