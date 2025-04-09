import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WaitQueue extends MyQueue {
    private final ArrayList<Person> upQueue = new ArrayList<>();
    private final ArrayList<Person> downQueue = new ArrayList<>();

    public WaitQueue() {
        super();
    }

    public synchronized boolean hasReqInDirection(int curFloor, boolean isUp) {
        return Stream.concat(upQueue.stream(), downQueue.stream())
                .anyMatch(p ->
                        (p.getFromFloor() > curFloor && isUp) ||
                        (p.getFromFloor() < curFloor && !isUp));
    }

    public synchronized boolean canTake(int curFloor, boolean upSign) {
        return Stream.concat(upQueue.stream(), downQueue.stream())
                .anyMatch(p -> p.getFromFloor() == curFloor && sameDirection(p, upSign));
    }

    private synchronized boolean sameDirection(Person p, boolean upSign) {
        return (upSign && p.isUp()) ||
               (!upSign && !p.isUp());
    }

    public synchronized int getLowestFloor() {
        //* 获取等待用户的最低层
        return Stream.concat(upQueue.stream(), downQueue.stream())
                .flatMapToInt(p -> IntStream.of(p.getFromFloor()))
                .min()
                .orElse(7);
    }

    public synchronized int getHighestFloor() {
        //* 获取等待用户的最高层
        return Stream.concat(upQueue.stream(), downQueue.stream())
                .flatMapToInt(p -> IntStream.of(p.getFromFloor()))
                .max()
                .orElse(-3);
    }

    public synchronized int getSize() {
        return upQueue.size() + downQueue.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return upQueue.isEmpty() && downQueue.isEmpty();
    }

    public synchronized void addPerson(Person person) {
        if (person.isUp()) {
            upQueue.add(person);
        } else {
            downQueue.add(person);
        }
    }

    public synchronized ArrayList<Person> takeElev(int curFloor,int maxNumber,boolean upSign) {
        ArrayList<Person> result = new ArrayList<>();
        List<Person> people = (upSign ? upQueue : downQueue).stream()
            .filter(person -> person.getFromFloor() == curFloor)
            .sorted((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()))
            .collect(Collectors.toList());

        int cnt = 0;
        for (Person person : people) {
            if (cnt == maxNumber) {
                break;
            }

            result.add(person);
            if (person.isUp()) {
                upQueue.remove(person);
            } else {
                downQueue.remove(person);
            }
            cnt++;
        }

        return result;
    }

    public synchronized ArrayList<Person> clear() {
        ArrayList<Person> result = new ArrayList<>();
        result.addAll(upQueue);
        result.addAll(downQueue);
        upQueue.clear();
        downQueue.clear();
        return result;
    }
}
