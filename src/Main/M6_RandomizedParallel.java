package Main;

import java.util.*;
import java.util.concurrent.*;

/**
 * Module 6: Randomized & Parallel Algorithms for EduTrack Platform
 * Contains Randomized QuickSort, Reservoir Sampling, Miller-Rabin Primality Test,
 * Blelloch Prefix Scan, Parallel Reduce, and Brent's Theorem Analysis.
 */
public class M6_RandomizedParallel {

    // =========================================================================
    // 1. RANDOMIZED QUICKSORT
    // =========================================================================
    
    private static final Random rand = new Random();

    public static void randomizedQuickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionRandom(arr, low, high);
            randomizedQuickSort(arr, low, pivotIndex - 1);
            randomizedQuickSort(arr, pivotIndex + 1, high);
        }
    }

    private static int partitionRandom(int[] arr, int low, int high) {
        int randomPivot = low + rand.nextInt(high - low + 1);
        int temp = arr[randomPivot];
        arr[randomPivot] = arr[high];
        arr[high] = temp;

        return partition(arr, low, high);
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    // =========================================================================
    // 2. RESERVOIR SAMPLING (ALGORITHM R)
    // =========================================================================
    
    public static int[] reservoirSample(int[] stream, int k) {
        int[] reservoir = new int[k];
        
        for (int i = 0; i < k; i++) {
            reservoir[i] = stream[i];
        }

        for (int i = k; i < stream.length; i++) {
            int r = rand.nextInt(i + 1);
            if (r < k) {
                reservoir[r] = stream[i];
            }
        }
        return reservoir;
    }

    // =========================================================================
    // 3. MILLER-RABIN PRIMALITY TEST
    // =========================================================================
    
    private static long powerMod(long base, long exp, long mod) {
        long res = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                res = (res * base) % mod;
            }
            base = (base * base) % mod;
            exp >>= 1;
        }
        return res;
    }

    public static boolean isPrimeMillerRabin(long n, int kIterations) {
        if (n <= 1 || n == 4) return false;
        if (n <= 3) return true;
        if (n % 2 == 0) return false;

        long d = n - 1;
        int s = 0;
        while (d % 2 == 0) {
            d /= 2;
            s++;
        }

        for (int i = 0; i < kIterations; i++) {
            long a = 2 + (long) (rand.nextDouble() * (n - 4));
            long x = powerMod(a, d, n);

            if (x == 1 || x == n - 1) continue;

            boolean composite = true;
            for (int r = 1; r < s; r++) {
                x = powerMod(x, 2, n);
                if (x == n - 1) {
                    composite = false;
                    break;
                }
            }

            if (composite) return false;
        }
        return true;
    }

    // =========================================================================
    // 4. BLELLOCH PARALLEL PREFIX SCAN
    // =========================================================================
    
    public static class BlellochScan {
        public int[] exclusiveScan;
        public int[] inclusiveScan;

        public BlellochScan(int[] exclusiveScan, int[] inclusiveScan) {
            this.exclusiveScan = exclusiveScan;
            this.inclusiveScan = inclusiveScan;
        }
    }

    public static BlellochScan executeBlellochScan(int[] input) {
        int n = input.length;
        int nextPow2 = 1;
        while (nextPow2 < n) nextPow2 <<= 1;

        int[] a = new int[nextPow2];
        System.arraycopy(input, 0, a, 0, n);

        // 1. Up-Sweep (Reduce Phase)
        for (int d = 0; d < (int)(Math.log(nextPow2) / Math.log(2)); d++) {
            int stride = 1 << (d + 1);
            for (int k = 0; k < nextPow2; k += stride) {
                a[k + stride - 1] += a[k + (1 << d) - 1];
            }
        }

        a[nextPow2 - 1] = 0;

        // 2. Down-Sweep Phase
        for (int d = (int)(Math.log(nextPow2) / Math.log(2)) - 1; d >= 0; d--) {
            int stride = 1 << (d + 1);
            for (int k = 0; k < nextPow2; k += stride) {
                int temp = a[k + (1 << d) - 1];
                a[k + (1 << d) - 1] = a[k + stride - 1];
                a[k + stride - 1] += temp;
            }
        }

        int[] exclusive = new int[n];
        int[] inclusive = new int[n];
        System.arraycopy(a, 0, exclusive, 0, n);

        for (int i = 0; i < n; i++) {
            inclusive[i] = exclusive[i] + input[i];
        }

        return new BlellochScan(exclusive, inclusive);
    }

    // =========================================================================
    // 5. PARALLEL REDUCE (MULTITHREADED SUMMATION)
    // =========================================================================
    
    public static long parallelReduceSum(int[] arr, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int chunkSize = (int) Math.ceil((double) arr.length / numThreads);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(start + chunkSize, arr.length);

            if (start < arr.length) {
                Callable<Long> task = () -> {
                    long sum = 0;
                    for (int j = start; j < end; j++) {
                        sum += arr[j];
                    }
                    return sum;
                };
                futures.add(executor.submit(task));
            }
        }

        long totalSum = 0;
        for (Future<Long> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();
        return totalSum;
    }

    // =========================================================================
    // 6. BRENT'S THEOREM (PARALLEL EXECUTION BOUND)
    // =========================================================================
    
    public static void printBrentsTheoremAnalysis(double W, double S, int P) {
        double estimatedTime = (W / P) + S;
        double speedup = W / estimatedTime;

        System.out.println("Brent's Theorem Parallel Performance Analysis:");
        System.out.println("  Total Work (W)     : " + (int)W + " operations");
        System.out.println("  Span / Depth (S)   : " + (int)S + " steps");
        System.out.println("  Processors (P)     : " + P + " cores");
        System.out.println("  Theoretical T(P)   : <= (W / P) + S");
        System.out.printf("  Estimated Time T(P): %.2f time units\n", estimatedTime);
        System.out.printf("  Theoretical Speedup: %.2fx speedup\n", speedup);
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM6() {
        System.out.println("=============================================");
        System.out.println("   M6 - RANDOMIZED & PARALLEL ALGORITHMS     ");
        System.out.println("=============================================");

        // 1. Randomized QuickSort Demonstration
        System.out.println("\n--- 1. RANDOMIZED QUICKSORT ---");
        int[] studentMarks = {88, 45, 92, 67, 74, 99, 53, 81, 60, 95};
        System.out.println("Original Student Marks: " + Arrays.toString(studentMarks));
        randomizedQuickSort(studentMarks, 0, studentMarks.length - 1);
        System.out.println("Sorted Student Marks  : " + Arrays.toString(studentMarks));

        // 2. Reservoir Sampling Demonstration
        System.out.println("\n--- 2. RESERVOIR SAMPLING ---");
        int[] streamData = new int[20];
        for (int i = 0; i < 20; i++) {
            streamData[i] = 1001 + i;
        }
        int k = 5;
        System.out.println("Arriving Activity Stream: " + Arrays.toString(streamData));
        int[] sample = reservoirSample(streamData, k);
        System.out.println("Random Reservoir Sample (k=" + k + "): " + Arrays.toString(sample));

        // 3. Miller-Rabin Primality Test Demonstration
        System.out.println("\n--- 3. MILLER-RABIN PRIMALITY TEST ---");
        long testPrime = 10007;
        long testComposite = 10008;
        int iterations = 5;
        System.out.println("Testing Token " + testPrime + " (Iterations=" + iterations + "): " 
                           + (isPrimeMillerRabin(testPrime, iterations) ? "PROBABLY PRIME" : "COMPOSITE"));
        System.out.println("Testing Token " + testComposite + " (Iterations=" + iterations + "): " 
                           + (isPrimeMillerRabin(testComposite, iterations) ? "PROBABLY PRIME" : "COMPOSITE"));

        // 4. Blelloch Parallel Prefix Scan Demonstration
        System.out.println("\n--- 4. BLELLOCH PARALLEL PREFIX SCAN ---");
        int[] scanInput = {1, 2, 3, 4, 5};
        System.out.println("Input Array: " + Arrays.toString(scanInput));
        BlellochScan bScan = executeBlellochScan(scanInput);
        System.out.println("Exclusive Scan Result: " + Arrays.toString(bScan.exclusiveScan));
        System.out.println("Inclusive Scan Result: " + Arrays.toString(bScan.inclusiveScan));

        // 5. Parallel Reduce Demonstration
        System.out.println("\n--- 5. PARALLEL REDUCE (MULTITHREADED SUMMATION) ---");
        int[] marksData = {85, 90, 78, 92, 88, 76, 95, 89, 84, 91, 93, 87, 82, 89, 96, 90};
        int numThreads = 4;
        System.out.println("Input Student Scores Count: " + marksData.length);
        try {
            long parallelSum = parallelReduceSum(marksData, numThreads);
            System.out.println("Parallel Aggregate Sum (Threads=" + numThreads + "): " + parallelSum);
        } catch (Exception e) {
            System.out.println("Parallel Reduce Error: " + e.getMessage());
        }

        // 6. Brent's Theorem Demonstration
        System.out.println("\n--- 6. BRENT'S THEOREM PARALLEL ANALYSIS ---");
        double totalWork = 1000.0;
        double span = 50.0;
        int processors = 8;
        printBrentsTheoremAnalysis(totalWork, span, processors);

        System.out.println("\n=============================================");
        System.out.println("         M6 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}