import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyCollections {
   
    private static final int NUM_OPERATIONS = 1000000;

    // this function appends values into a list
    private static long benchmarkCollection(List<Integer> list) {
        // check the time the process starts
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < NUM_OPERATIONS; i++) {
            final int value = i;
            executorService.execute(() -> list.add(value));
        }

        shutdownExecutor(executorService);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    
    // Thread Interruption Example
    public static void threadInterruptionExample() {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread is running...");
                    Thread.sleep(100);  // Simulating work
                }
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted.");
                
            }
        });

        thread.start();
        try {
            Thread.sleep(500);  // Let the thread run for a bit
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.interrupt();  // Interrupt the thread
    }

    // Fork/Join Example
    static class RecursiveSum extends RecursiveTask<Integer> {
        private final int[] array;
        private final int start, end;

        RecursiveSum(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= 10) {  // Threshold for simplicity
                int sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                RecursiveSum leftTask = new RecursiveSum(array, start, mid);
                RecursiveSum rightTask = new RecursiveSum(array, mid, end);
                leftTask.fork();  // Asynchronously execute the left task
                return rightTask.compute() + leftTask.join();  // Wait for left task and combine results
            }
        }
    }

    public static int forkJoinExample(int[] array) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new RecursiveSum(array, 0, array.length));
    }

    // Deadlock Scenario
    public static void deadlockExample() {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Holding lock 1...");

                try { Thread.sleep(10); } catch (InterruptedException e) {}
                System.out.println("Thread 1: Waiting for lock 2...");

                synchronized (lock2) {
                    System.out.println("Thread 1: Holding lock 1 & 2...");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Holding lock 2...");

                try { Thread.sleep(10); } catch (InterruptedException e) {}
                System.out.println("Thread 2: Waiting for lock 1...");

                synchronized (lock1) {
                    System.out.println("Thread 2: Holding lock 1 & 2...");
                }
            }
        });

        thread1.start();
        thread2.start();
    }

    // Deadlock Solution
    public static void deadlockSolution() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();

        Thread thread1 = new Thread(() -> {
            try {
                if (lock1.tryLock() && lock2.tryLock()) {
                    try {
                        System.out.println("Thread 1: Holding lock 1 & 2...");
                    } finally {
                        lock2.unlock();
                        lock1.unlock();
                    }
                }
            } finally {
                if (lock1.isHeldByCurrentThread()) lock1.unlock();
                if (lock2.isHeldByCurrentThread()) lock2.unlock();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                if (lock2.tryLock() && lock1.tryLock()) {
                    try {
                        System.out.println("Thread 2: Holding lock 1 & 2...");
                    } finally {
                        lock1.unlock();
                        lock2.unlock();
                    }
                }
            } finally {
                if (lock1.isHeldByCurrentThread()) lock1.unlock();
                if (lock2.isHeldByCurrentThread()) lock2.unlock();
            }
        });

        thread1.start();
        thread2.start();
    }

    public static void main(String[] args) {
        // Example usages
        threadInterruptionExample();

        int[] array = new int[100];
        Arrays.fill(array, 1);
        System.out.println("Fork/Join result: " + forkJoinExample(array));

        deadlockExample();
        deadlockSolution();
    }
}
