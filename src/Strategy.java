import java.util.ArrayList;

public interface Strategy {
    ElevatorState getNextState();

    ArrayList<Person> getOutPeople();

    ArrayList<Person> getInPeople();
}
