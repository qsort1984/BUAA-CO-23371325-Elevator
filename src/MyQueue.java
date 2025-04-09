public class MyQueue {
    private boolean isEnd;

    public MyQueue() {
        isEnd = false;
    }

    public synchronized void setEnd() {
        isEnd = true;
        if (this instanceof RequestQueue) {
            notifyAll();
        }
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return false;
    }

    public synchronized void addPerson(Person person) {
        notifyAll();
    }
}
