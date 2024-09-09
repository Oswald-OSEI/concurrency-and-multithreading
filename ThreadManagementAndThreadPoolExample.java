import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadManagementAndThreadPoolExample {

    public static void main(String[] args) {
        // Step 1: Thread Management and Synchronization
        SharedResource sharedResource = new SharedResource();

        System.out.println("Starting threads for shared resource increment:");

        // Create two threads that increment the shared resource
        Thread thread1 = new Thread(() -> incrementResource(sharedResource, "Thread-1"));
        Thread thread2 = new Thread(() -> incrementResource(sharedResource, "Thread-2"));

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final value of shared resource: " + sharedResource.getValue());

        // Step 2: Thread Pool Implementation for Handling Client Requests
        System.out.println("\nSimulating client request handling using a thread pool:");

        // Create a thread pool with 5 threads
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Simulate processing 10 client requests
        for (int i = 1; i <= 10; i++) {
            final int requestId = i;
            executorService.execute(() -> handleClientRequest(requestId));
        }

        // Shutdown the executor service
        shutdownExecutor(executorService);

        System.out.println("All client requests have been handled.");
    }

    // Method to increment the shared resource
    private static void incrementResource(SharedResource sharedResource, String threadName) {
        for (int i = 0; i < 5; i++) {
            sharedResource.increment();
            System.out.println(threadName + " incremented to: " + sharedResource.getValue());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to simulate handling a client request
    private static void handleClientRequest(int requestId) {
        System.out.println("Handling client request " + requestId + " on " + Thread.currentThread().getName());
        try {
            Thread.sleep(2000);  // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Completed client request " + requestId);
    }

    // Method to shutdown the executor service
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

    // SharedResource class for synchronization example
    static class SharedResource {
        private int value = 0;

        public synchronized void increment() {
            value++;
        }

        public int getValue() {
            return value;
        }
    }
}
