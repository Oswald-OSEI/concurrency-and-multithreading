import java.util.*;
import java.util.concurrent.*;

public class ConcurrencyCollections {
   

    private static final int NUM_OPERATIONS = 1000000;

    //this function appends values into a list
    private static long benchmarkCollection(List<Integer> list) {
        //check the time the process starts
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
            Thread.currentThread().interrupt();
        }
    }


    public static void main(String[] args) {
        List<Integer> nonConcurrentList = new ArrayList<>();
        List<Integer> concurrentList = new CopyOnWriteArrayList<>();

        System.out.println("Benchmarking non-concurrent collection...");
        long nonConcurrentTime = benchmarkCollection(nonConcurrentList);

        System.out.println("Benchmarking concurrent collection...");
        long concurrentTime = benchmarkCollection(concurrentList);

        System.out.println("Non-concurrent collection time: " + nonConcurrentTime + " ms");
        System.out.println("Concurrent collection time: " + concurrentTime + " ms");
    }

    


}
