import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  //* 初始化时间戳

        RequestQueue requestQueue = new RequestQueue();
        InputThread inputThread = new InputThread(requestQueue);

        HashMap<Integer,WaitQueue> waitQueues = new HashMap<>();
        HashMap<Integer,ArrayList<TempSchedule>> tempSchedules = new HashMap<>();
        HashMap<Integer,Object> locks = new HashMap<>();
        ArrayList<ElevatorThread> elevatorThreads = new ArrayList<>();
        Object sharedLock = new Object();
        for (int i = 1; i <= 6; i++) {
            WaitQueue waitQueue = new WaitQueue();
            waitQueues.put(i, waitQueue);
            ArrayList<TempSchedule> tempSchedule = new ArrayList<>();
            tempSchedules.put(i, tempSchedule);

            Object lock = new Object();
            locks.put(i, lock);

            ElevatorThread elevatorThread = new ElevatorThread(
                i,
                waitQueue,
                tempSchedule,
                requestQueue,
                lock,
                sharedLock);
            elevatorThreads.add(elevatorThread);
            elevatorThread.start();
        }

        ScheduleThread scheduleThread = new ScheduleThread(
            requestQueue,
            waitQueues,
            elevatorThreads,
            tempSchedules,
            locks,
            sharedLock);

        scheduleThread.start();
        inputThread.start();
    }
}
