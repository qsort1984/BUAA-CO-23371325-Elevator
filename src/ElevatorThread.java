import java.util.ArrayList;

import com.oocourse.elevator3.TimableOutput;

public class ElevatorThread extends Thread {
    //* 基本属性
    private final int id;
    private int currentFloor = 1;
    private int speed = 400; //* 400 ms/floor
    private TransferFloor transferFloor;
    private final Strategy strategy;
    //* 状态
    // todo : 能用AtomicBoolean吗？
    private boolean isUp = true;
    private boolean isScheduling = false;
    private boolean isOpen = false;
    private boolean updateStartSign = false;
    private boolean updateEndSign = false;
    private boolean isUpdating = false;
    private boolean isUpdated = false;
    //* 任务队列
    private final WaitQueue waitQueue;
    private final ArrayList<TempSchedule> tempSchedules;
    private final RequestQueue requestQueue;
    private final ArrayList<Person> currentPeople = new ArrayList<>();
    private ArrayList<Person> pendingPeople = new ArrayList<>();
    //* 锁
    private final Object lock;
    private final Object sharedLock;
    private volatile Object updateLock;
    private final Object pendingLock = new Object();

    public ElevatorThread(int id, WaitQueue waitQueue, ArrayList<TempSchedule> tempSchedules,
        RequestQueue requestQueue, Object lock, Object sharedLock) {
        this.id = id;
        this.waitQueue = waitQueue;
        this.tempSchedules = tempSchedules;
        this.requestQueue = requestQueue;
        this.lock = lock;
        this.sharedLock = sharedLock;
        strategy = new LookStrategy(this, waitQueue, tempSchedules);
    }

    @Override
    public void run() {
        while (true) {
            ElevatorState state = strategy.getNextState();
            if (state.equals(ElevatorState.END)) {
                break;
            } else if (state.equals(ElevatorState.OPEN)) {
                open();
            } else if (state.equals(ElevatorState.CLOSE)) {
                close();
            } else if (state.equals(ElevatorState.MOVE)) {
                move();
            } else if (state.equals(ElevatorState.REVERSE)) {
                reverse();
            } else if (state.equals(ElevatorState.WAITING)) {
                waiting();
            } else if (state.equals(ElevatorState.SCHEDULE)) {
                schedule();
            } else if (state.equals(ElevatorState.UPDATE)) {
                upDate();
            } else if (state.equals(ElevatorState.UPDATEEND)) {
                updateEnd();
            } else if (state.equals(ElevatorState.TRANSFER)) {
                transfer();
            } else {
                System.out.println("At ElevatorThread.run() unknown state" + state);
            }

        }
    }

    private void reverse() {
        isUp = !isUp;
    }

    private void schedule() {
        synchronized (sharedLock) {
            isScheduling = true;
            speed = tempSchedules.get(0).getTempSpeed();
            synchronized (requestQueue) {
                synchronized (pendingLock) {
                    pendingPeople.addAll(waitQueue.clear());
                    requestQueue.notifyAll();
                }
            }
            TimableOutput.println("SCHE-BEGIN-" + id);
        }
    }

    private void move() {
        try {
            sleep(speed);
        } catch (InterruptedException e) {
            System.out.println("At ElevatorThread.move() interrupted");
        }

        if (isUp) {
            currentFloor++;
        } else {
            currentFloor--;
        }

        if (isUpdated && hasArriveTransFloor()) {
            transferFloor.setOccupied();
        }

        TimableOutput.println("ARRIVE-" + floorToString() + "-" + id);
    }

    private void open() {
        isOpen = true;
        TimableOutput.println("OPEN-" + floorToString() + "-" + id);
        outElevator();

        try {
            if (isScheduling) {
                sleep(1000);
            } else {
                sleep(400);
            }
        } catch (InterruptedException e) {
            System.out.println("At ElevatorThread.arrive() interrupted");
        }
        inElevator();
    }

    private void close() {
        inElevator();
        TimableOutput.println("CLOSE-" + floorToString() + "-" + id);
        isOpen = false;
        if (isScheduling) {
            synchronized (sharedLock) {
                isScheduling = false;
                speed = 400;
                tempSchedules.remove(0);
                TimableOutput.println("SCHE-END-" + id);
                sharedLock.notifyAll();
            }
        }
    }

    private void upDate() {
        synchronized (sharedLock) {
            updateStartSign = false;
            isUpdating = true;
            synchronized (requestQueue) {
                synchronized (pendingLock) {
                    pendingPeople.addAll(waitQueue.clear());
                    requestQueue.notifyAll();
                }
            }
            Object lock = this.updateLock;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    private void updateEnd() {
        synchronized (sharedLock) {
            updateEndSign = false;
            isUpdating = false;
            isUpdated = true;
            speed = 200;
            sharedLock.notifyAll();
        }
    }

    private void transfer() {
        open();
        close();
        reverse();
        move();
        transferFloor.setEmpty();
    }

    private void outElevator() {
        ArrayList<Person> outPeople = strategy.getOutPeople();
        if (!outPeople.isEmpty()) {
            synchronized (requestQueue) {
                for (Person p : outPeople) {
                    if (p.getToFloor() == currentFloor) {
                        TimableOutput.println("OUT-S-" + p.getId() + "-" +
                            floorToString() + "-" + id);
                    } else {
                        TimableOutput.println("OUT-F-" + p.getId() + "-" +
                            floorToString() + "-" + id);
                        p.setFromFloor(currentFloor);
                        synchronized (pendingLock) {
                            pendingPeople.add(p);
                        }
                    }
                    currentPeople.remove(p);
                }
                requestQueue.notifyAll();
            }
        }
    }

    private void inElevator() {
        ArrayList<Person> inPeople = strategy.getInPeople();
        for (Person p : inPeople) {
            TimableOutput.println("IN-" + p.getId() + "-" + floorToString() + "-" + id);
            currentPeople.add(p);
        }
    }

    private void waiting() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    private String floorToString() {
        if (currentFloor <= 0) {
            return "B" + (1 - currentFloor);
        } else {
            return "F" + currentFloor;
        }
    }

    public void setUpdateLock(Object updateLock) {
        this.updateLock = updateLock;
    }

    public void setTransferFloor(TransferFloor transferFloor) {
        this.transferFloor = transferFloor;
    }

    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    public boolean isEmpty() {
        synchronized (pendingLock) {
            return currentPeople.isEmpty() && pendingPeople.isEmpty() && waitQueue.isEmpty();
        }
    }

    public ArrayList<Person> sendPendingPeople() {
        synchronized (pendingLock) {
            ArrayList<Person> tem = pendingPeople;
            pendingPeople = new ArrayList<>();
            return tem;
        }
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getCurrentNum() {
        return currentPeople.size();
    }

    public boolean hasArriveTransFloor() {
        return transferFloor.getFloor() == currentFloor;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isScheduling() {
        return isScheduling;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUpdateStartSign() {
        this.updateStartSign = true;
    }

    public boolean getUpdateStartSign() {
        return updateStartSign;
    }

    public void setUpdateEndSign() {
        this.updateEndSign = true;
    }

    public boolean getUpdateEndSign() {
        return updateEndSign;
    }

    public ArrayList<Person> getCurrentPeople() {
        return currentPeople;
    }

    public int getElevatorId() {
        return id;
    }

    public int getSize() {
        return currentPeople.size() + waitQueue.getSize();
    }
}
