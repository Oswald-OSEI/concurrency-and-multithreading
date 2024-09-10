import java.util.*;
import java.util.concurrent.*;

public class ConcurrencyCollections {

    private static final int NUM_OPERATIONS = 1000000;

    // Benchmark using a synchronized list
    private static long benchmarkSynchronizedList() {
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        return benchmarkCollection(synchronizedList);
    }

    // Benchmark using a concurrent collection: CopyOnWriteArrayList
    private static long benchmarkConcurrentCollection() {
        List<Integer> concurrentList = new CopyOnWriteArrayList<>();
        return benchmarkCollection(concurrentList);
    }

    // Benchmark using manual synchronization
    private static long benchmarkManualSynchronization() {
        List<Integer> list = new ArrayList<>();
        Object lock = new Object();

        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < NUM_OPERATIONS; i++) {
            final int value = i;
            executorService.execute(() -> {
                synchronized (lock) {
                    list.add(value);
                }
            });
        }

        shutdownExecutor(executorService);

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    // Original benchmark method for reference
    private static long benchmarkCollection(List<Integer> list) {
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

    public static void main(String[] args) {
        System.out.println("Benchmark Synchronized List: " + benchmarkSynchronizedList() + " ms");
        System.out.println("Benchmark Concurrent Collection: " + benchmarkConcurrentCollection() + " ms");
        System.out.println("Benchmark Manual Synchronization: " + benchmarkManualSynchronization() + " ms");
    }
}

