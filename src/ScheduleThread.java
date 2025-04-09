import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleThread extends Thread {
    private final RequestQueue requestQueue;
    private final HashMap<Integer,WaitQueue> waitQueues;
    private final ArrayList<ElevatorThread> elevatorThreads;
    private final HashMap<Integer,ArrayList<TempSchedule>> tempSchedules;
    private final HashMap<Integer,Object> locks;
    private final Object sharedLock;
    private final Dispatcher dispatcher;

    public ScheduleThread(RequestQueue requestQueue, HashMap<Integer,WaitQueue> waitQueues,
        ArrayList<ElevatorThread> elevatorThreads,
        HashMap<Integer, ArrayList<TempSchedule>> tempSchedules,
        HashMap<Integer,Object> locks,
        Object sharedLock) {
        this.requestQueue = requestQueue;
        this.waitQueues = waitQueues;
        this.elevatorThreads = elevatorThreads;
        this.tempSchedules = tempSchedules;
        this.dispatcher = new JudgeDispatcher(elevatorThreads);
        this.locks = locks;
        this.sharedLock = sharedLock;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEnd() && requestQueue.isEmpty() && elevatorAllEmpty()) {
                for (Integer elevatorId : waitQueues.keySet()) {
                    synchronized (locks.get(elevatorId)) {
                        waitQueues.get(elevatorId).setEnd();
                        locks.get(elevatorId).notifyAll();
                    }
                }
                break;
            }

            if (requestQueue.isEmpty() && (!elevatorAllEmpty() || !requestQueue.isEnd())) {
                synchronized (requestQueue) {
                    try {
                        requestQueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            ArrayList<Person> pendingPeople = sendAllPendingPeople();
            if (!pendingPeople.isEmpty()) {
                pendingPeople.forEach(requestQueue::addPerson);
            }

            RequestObject tem = requestQueue.getRequestObject();
            if (tem == null) {
                continue;
            }

            if (tem instanceof Person) {
                distribute((Person)tem);
            } else if (tem instanceof TempSchedule) {
                distribute((TempSchedule)tem);
            } else if (tem instanceof Update) {
                distribute((Update)tem);
            } else {
                System.out.println("Invalid request object: " + tem);
            }
        }
    }

    private boolean elevatorAllEmpty() {
        for (ElevatorThread elevator : elevatorThreads) {
            if (!elevator.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Person> sendAllPendingPeople() {
        ArrayList<Person> people = new ArrayList<>();
        for (ElevatorThread elevatorThread : elevatorThreads) {
            ArrayList<Person> tem = elevatorThread.sendPendingPeople();
            if (!tem.isEmpty()) {
                people.addAll(tem);
            }
        }

        return people;
    }

    private void distribute(Person person) {
        synchronized (sharedLock) {
            int elevatorId = dispatcher.dispatchElev(person);
            while (elevatorId == -1) {
                try {
                    sharedLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                elevatorId = dispatcher.dispatchElev(person);
            }

            synchronized (locks.get(elevatorId)) {
                TimableOutput.println("RECEIVE-" + person.getId() + "-" + elevatorId);
                waitQueues.get(elevatorId).addPerson(person);
                locks.get(elevatorId).notifyAll();
            }
        }
    }

    private void distribute(TempSchedule schedule) {
        int elevatorId = schedule.getElevatorId();
        synchronized (locks.get(elevatorId)) {
            tempSchedules.get(elevatorId).add(schedule);
            locks.get(elevatorId).notifyAll();
        }
    }

    private void distribute(Update update) {
        int elevatorAId = update.getElevatorAId();
        int elevatorBId = update.getElevatorBId();
        Object updateLock = new Object();
        UpdateThread updateThread = new UpdateThread(update,
            elevatorThreads.get(elevatorAId),
            elevatorThreads.get(elevatorBId),
            locks.get(elevatorAId),
            locks.get(elevatorBId),
            updateLock);
        updateThread.start();
    }
}
