public class MyQueue {
    private boolean isEnd;
    private final Object endLock = new Object();

    public MyQueue() {
        isEnd = false;
    }

    public void setEnd() {
        synchronized (endLock) {
            isEnd = true;
            if (this instanceof RequestQueue) {
                endLock.notifyAll();
            }
        }
    }

    public boolean isEnd() {
        synchronized (endLock) {
            return isEnd;
        }
    }

    public synchronized boolean isEmpty() {
        return false;
    }

    public synchronized void addPerson(Person person) {
        notifyAll();
    }
}
