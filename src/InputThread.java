import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.io.IOException;

public class InputThread extends Thread {
    private final RequestQueue requestQueue;

    public InputThread(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();

            if (request == null) {
                requestQueue.setEnd();
                break;
            }

            if (request instanceof PersonRequest) {
                dealPersonRequest(request);
            } else if (request instanceof ScheRequest) {
                dealScheRequest(request);
            } else if (request instanceof UpdateRequest) {
                dealUpdateRequest(request);
            } else {
                dealInvalidRequest(request);
            }
        }

        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int floor2Int(String floor) {
        int result = Integer.parseInt(floor.substring(1));
        if (floor.charAt(0) == 'B') {
            result = 1 - result;
        } else if (floor.charAt(0) != 'F') {
            System.out.println("Invalid floor: " + floor);
        }

        return result;
    }

    private int speed2Int(double speed) {
        if (speed == 0.2) {
            return 200;
        } else if (speed == 0.3) {
            return 300;
        } else if (speed == 0.4) {
            return 400;
        } else if (speed == 0.5) {
            return 500;
        } else {
            System.out.println("Invalid speed: " + speed);
            return 400;
        }
    }

    private void dealPersonRequest(Request request) {
        PersonRequest personRequest = (PersonRequest) request;
        Person person = new Person(
            personRequest.getPersonId(),
            personRequest.getPriority(),
            floor2Int(personRequest.getFromFloor()),
            floor2Int(personRequest.getToFloor()));

        requestQueue.addPerson(person);
    }

    private void dealScheRequest(Request request) {
        ScheRequest scheRequest = (ScheRequest) request;
        TempSchedule tempSchedule = new TempSchedule(
            scheRequest.getElevatorId(),
            speed2Int(scheRequest.getSpeed()),
            floor2Int(scheRequest.getToFloor()));

        requestQueue.addSchedule(tempSchedule);
    }

    private void dealUpdateRequest(Request request) {
        UpdateRequest updateRequest = (UpdateRequest) request;
        Update update = new Update(
            updateRequest.getElevatorAId(),
            updateRequest.getElevatorBId(),
            floor2Int(updateRequest.getTransferFloor()));

        requestQueue.addUpdate(update);
    }

    private void dealInvalidRequest(Request request) {
        System.out.println("Invalid request: " + request);
    }
}
