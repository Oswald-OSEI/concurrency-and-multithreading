import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumersBenchmark {

    private static final int NUM_OPERATIONS = 100000;

    // Implementation 1: Using BlockingQueue
    private static long benchmarkBlockingQueue() {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(100);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        long startTime = System.currentTimeMillis();

        // Producer
        executor.execute(() -> {
            try {
                for (int i = 0; i < NUM_OPERATIONS; i++) {
                    queue.put(i);
                }
                queue.put(-1);  // End signal
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer
        executor.execute(() -> {
            try {
                while (true) {
                    Integer value = queue.take();
                    if (value == -1) break;  // End signal
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        shutdownExecutor(executor);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    // Implementation 2: Using synchronized methods (wait/notify)
    private static long benchmarkSynchronizedMethods() {
        List<Integer> list = new ArrayList<>();
        Object lock = new Object();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        long startTime = System.currentTimeMillis();

        // Producer
        executor.execute(() -> {
            try {
                synchronized (lock) {
                    for (int i = 0; i < NUM_OPERATIONS; i++) {
                        list.add(i);
                        lock.notify();
                    }
                    list.add(-1); // End signal
                    lock.notify();
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer
        executor.execute(() -> {
            try {
                synchronized (lock) {
                    while (true) {
                        while (list.isEmpty()) {
                            lock.wait();
                        }
                        Integer value = list.remove(0);
                        if (value == -1) break;  // End signal
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        shutdownExecutor(executor);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    // Implementation 3: Using Locks and Conditions
    private static long benchmarkLocksAndConditions() {
        List<Integer> list = new ArrayList<>();
        Lock lock = new ReentrantLock();
        Condition notEmpty = lock.newCondition();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        long startTime = System.currentTimeMillis();

        // Producer
        executor.execute(() -> {
            try {
                lock.lock();
                for (int i = 0; i < NUM_OPERATIONS; i++) {
                    list.add(i);
                    notEmpty.signal();
                }
                list.add(-1); // End signal
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        });

        // Consumer
        executor.execute(() -> {
            try {
                lock.lock();
                while (true) {
                    while (list.isEmpty()) {
                        notEmpty.await();
                    }
                    Integer value = list.remove(0);
                    if (value == -1) break;  // End signal
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        shutdownExecutor(executor);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        System.out.println("Benchmark BlockingQueue: " + benchmarkBlockingQueue() + " ms");
        System.out.println("Benchmark Synchronized Methods: " + benchmarkSynchronizedMethods() + " ms");
        System.out.println("Benchmark Locks and Conditions: " + benchmarkLocksAndConditions() + " ms");
    }
}
