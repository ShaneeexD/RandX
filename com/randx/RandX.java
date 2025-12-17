package com.randx;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RandX - High-speed cryptographically-inspired random number generator
 * 
 * Combines:
 * 1. Hardware entropy harvesting (timing jitter, GC noise, thread scheduling)
 * 2. ChaCha-inspired quarter-round mixing (ARX: Add-Rotate-XOR)
 * 3. Continuous entropy pooling with automatic refresh
 * 
 * Usage: RandX.range(1, 100), RandX.next(), RandX.bool()
 */
public class RandX {
    
    private static long[] state = new long[4];
    private static final AtomicLong entropyPool = new AtomicLong(0);
    private static long entropyCounter = 0;
    private static final int ENTROPY_REFRESH_INTERVAL = 1000;
    private static volatile boolean collecting = true;
    
    static {
        initializeState();
        startEntropyCollector();
    }
    
    private static void initializeState() {
        state[0] = harvestTimingEntropy();
        state[1] = harvestMemoryEntropy();
        state[2] = harvestThreadEntropy();
        state[3] = harvestSystemEntropy();
        for (int i = 0; i < 20; i++) {
            chachaRound();
        }
    }
    
    private static long harvestTimingEntropy() {
        long entropy = 0;
        long prev = System.nanoTime();
        for (int i = 0; i < 64; i++) {
            long now = System.nanoTime();
            entropy = (entropy << 1) | ((now - prev) & 1);
            prev = now;
            Math.sin(i * 0.1);
        }
        return entropy ^ System.nanoTime();
    }
    
    private static long harvestMemoryEntropy() {
        Runtime rt = Runtime.getRuntime();
        long entropy = rt.freeMemory() ^ rt.totalMemory();
        Object[] noise = new Object[100];
        for (int i = 0; i < noise.length; i++) {
            noise[i] = new byte[(int)(System.nanoTime() & 0xFF)];
            entropy ^= System.nanoTime();
        }
        entropy ^= rt.freeMemory();
        entropy ^= System.identityHashCode(noise);
        return entropy;
    }
    
    private static long harvestThreadEntropy() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long entropy = Thread.currentThread().getId();
        entropy ^= threadBean.getCurrentThreadCpuTime();
        entropy ^= threadBean.getCurrentThreadUserTime();
        entropy ^= (long) Thread.activeCount() << 32;
        entropy ^= System.nanoTime();
        return entropy;
    }
    
    private static long harvestSystemEntropy() {
        long entropy = 0;
        entropy ^= System.getProperty("user.name", "").hashCode();
        entropy ^= (long) System.getProperty("os.name", "").hashCode() << 32;
        entropy ^= Runtime.getRuntime().availableProcessors();
        entropy ^= ProcessHandle.current().pid();
        entropy ^= System.nanoTime();
        return entropy;
    }
    
    private static void chachaRound() {
        state[0] += state[1]; state[3] ^= state[0]; state[3] = Long.rotateLeft(state[3], 32);
        state[2] += state[3]; state[1] ^= state[2]; state[1] = Long.rotateLeft(state[1], 24);
        state[0] += state[1]; state[3] ^= state[0]; state[3] = Long.rotateLeft(state[3], 16);
        state[2] += state[3]; state[1] ^= state[2]; state[1] = Long.rotateLeft(state[1], 7);
        state[0] += state[3]; state[1] ^= state[0]; state[1] = Long.rotateLeft(state[1], 32);
        state[2] += state[1]; state[3] ^= state[2]; state[3] = Long.rotateLeft(state[3], 24);
        state[0] += state[3]; state[1] ^= state[0]; state[1] = Long.rotateLeft(state[1], 16);
        state[2] += state[1]; state[3] ^= state[2]; state[3] = Long.rotateLeft(state[3], 7);
    }
    
    private static void startEntropyCollector() {
        Thread collector = new Thread(() -> {
            while (collecting) {
                try {
                    Thread.sleep(10);
                    long fresh = harvestTimingEntropy() ^ System.nanoTime();
                    entropyPool.accumulateAndGet(fresh, (a, b) -> a ^ Long.rotateLeft(b, 17));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "RandX-EntropyCollector");
        collector.setDaemon(true);
        collector.start();
    }
    
    private static synchronized long nextLong() {
        if (++entropyCounter % ENTROPY_REFRESH_INTERVAL == 0) {
            state[(int)(entropyCounter % 4)] ^= entropyPool.get();
            state[(int)((entropyCounter + 1) % 4)] ^= System.nanoTime();
        }
        chachaRound();
        return state[0] ^ state[1] ^ state[2] ^ state[3];
    }
    
    // ============ PUBLIC API ============
    
    /**
     * Returns random int in range [min, max] inclusive
     */
    public static int range(int min, int max) {
        if (min > max) { int temp = min; min = max; max = temp; }
        if (min == max) return min;
        long rangeSize = (long) max - min + 1;
        long bits = nextLong() & 0x7FFFFFFFFFFFFFFFL;
        return (int) (min + (bits % rangeSize));
    }
    
    /**
     * Returns random double in range [min, max)
     */
    public static double range(double min, double max) {
        if (min > max) { double t = min; min = max; max = t; }
        double normalized = (nextLong() >>> 11) / (double)(1L << 53);
        return min + normalized * (max - min);
    }
    
    /**
     * Returns random int from 0 to max (exclusive)
     */
    public static int next(int max) {
        return range(0, max - 1);
    }
    
    /**
     * Returns random double between 0.0 and 1.0
     */
    public static double next() {
        return (nextLong() >>> 11) / (double)(1L << 53);
    }
    
    /**
     * Returns random boolean
     */
    public static boolean bool() {
        return (nextLong() & 1) == 1;
    }
    
    /**
     * Returns random bytes
     */
    public static byte[] bytes(int count) {
        byte[] result = new byte[count];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        while (buffer.remaining() >= 8) {
            buffer.putLong(nextLong());
        }
        if (buffer.remaining() > 0) {
            long last = nextLong();
            while (buffer.remaining() > 0) {
                buffer.put((byte) (last & 0xFF));
                last >>>= 8;
            }
        }
        return result;
    }
    
    /**
     * Force entropy refresh
     */
    public static void refresh() {
        state[0] ^= harvestTimingEntropy();
        state[1] ^= harvestMemoryEntropy();
        state[2] ^= harvestThreadEntropy();
        state[3] ^= System.nanoTime();
        for (int i = 0; i < 10; i++) {
            chachaRound();
        }
    }
    
    /**
     * Cleanup - stop background thread
     */
    public static void shutdown() {
        collecting = false;
    }
}
