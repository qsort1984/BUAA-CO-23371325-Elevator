import java.util.ArrayList;

public class RequestQueue extends MyQueue {
    private final ArrayList<RequestObject> requestObjects = new ArrayList<>();

    public RequestQueue() {
        super();
    }

    @Override
    public synchronized boolean isEmpty() {
        return requestObjects.isEmpty();
    }

    @Override
    public synchronized void addPerson(Person person) {
        requestObjects.add(person);
        notifyAll();
    }

    public synchronized void addSchedule(TempSchedule schedule) {
        requestObjects.add(schedule);
        notifyAll();
    }

    public synchronized void addUpdate(Update update) {
        requestObjects.add(update);
        notifyAll();
    }

    public synchronized RequestObject getRequestObject() {
        if (requestObjects.isEmpty()) {
            return null;
        }

        notifyAll();
        return requestObjects.remove(0);
    }
}
