import com.oocourse.elevator3.TimableOutput;

public class UpdateThread extends Thread {
    private final Update update;
    private final ElevatorThread elevatorAThread;
    private final ElevatorThread elevatorBThread;
    private final Object lockA;
    private final Object lockB;
    private final Object updateLock;

    public UpdateThread(Update update,
        ElevatorThread elevatorAThread, ElevatorThread elevatorBThread,
        Object lockA, Object lockB, Object updateLock) {
        this.update = update;
        this.elevatorAThread = elevatorAThread;
        this.elevatorBThread = elevatorBThread;
        this.lockA = lockA;
        this.lockB = lockB;
        this.updateLock = updateLock;
    }

    @Override
    public void run() {
        synchronized (lockA) {
            elevatorAThread.setUpdateLock(updateLock);
            elevatorAThread.setUpdateStartSign();
            lockA.notifyAll();
        }
        synchronized (lockB) {
            elevatorBThread.setUpdateLock(updateLock);
            elevatorBThread.setUpdateStartSign();
            lockB.notifyAll();
        }
        synchronized (updateLock) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        int elevatorAId = elevatorAThread.getElevatorId();
        int elevatorBId = elevatorBThread.getElevatorId();
        TimableOutput.println("UPDATE-BEGIN-" + elevatorAId + "-" + elevatorBId);
        synchronized (updateLock) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        TimableOutput.println("UPDATE-END-" + elevatorAId + "-" + elevatorBId);
        TransferFloor transferFloor = new TransferFloor(update.getToFloor());
        synchronized (lockA) {
            elevatorAThread.setUpdateEndSign();
            elevatorAThread.setTypeA();
            elevatorAThread.setTransferFloor(transferFloor);
            elevatorAThread.setCurrentFloor(update.getToFloor() + 1);
            lockA.notifyAll();
        }
        synchronized (lockB) {
            elevatorBThread.setUpdateEndSign();
            elevatorBThread.setTypeB();
            elevatorBThread.setTransferFloor(transferFloor);
            elevatorBThread.setCurrentFloor(update.getToFloor() - 1);
            lockB.notifyAll();
        }
    }
}
