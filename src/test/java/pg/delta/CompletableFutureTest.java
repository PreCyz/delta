package pg.delta;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompletableFutureTest {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private void sleepEnough() {
        var start = System.currentTimeMillis();
        Stream.generate(() -> new Random().nextInt(1_000_000))
                .limit(20_000_000)
                .toList();
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
        System.out.printf("Sleep done. duration %s%n", formatDuration(duration));
    }

    private void randomSleep() {
        var start = System.currentTimeMillis();
        List<Integer> list = Stream.generate(() -> new Random().nextInt(1_000_000))
                .limit(new Random().nextInt(1_000_000))
                .toList();
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - start);
        LoggerFactory.getLogger(getClass()).info("Random sleep done. {}, duration {}", list.size(), formatDuration(duration));
    }

    private String formatDuration(Duration duration) {
        return String.format("%d:%02d:%03d", duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
    }

    private String formatDuration(long duration) {
        return formatDuration(Duration.ofMillis(duration));
    }

    @Test
    void name() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            LoggerFactory.getLogger(getClass()).info("is Deamon {}", Thread.currentThread().isDaemon());
            randomSleep();
        }, EXECUTOR);
        assertFalse(cf.isDone());
        sleepEnough();
        assertTrue(cf.isDone());
    }

    @Test
    void name2() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            LoggerFactory.getLogger(getClass()).info("is Deamon {}", Thread.currentThread().isDaemon());
            randomSleep();
        }, EXECUTOR).thenAccept(a -> assertFalse(Thread.currentThread().isDaemon()));
        assertFalse(cf.isDone());
        sleepEnough();
        assertTrue(cf.isDone());
    }

    @Test
    void name3() {
        long start = System.currentTimeMillis();
        CompletableFuture.allOf(
                createCF(1_000_000, "1").thenAcceptAsync(
                        a -> createCF(2_000_000, "a1").join(), EXECUTOR
                ).whenComplete((v, t) -> System.out.printf("Group 1 duration %s%n", formatDuration(System.currentTimeMillis() - start))),

                createCF(4_000_000, "2").thenAcceptAsync(
                        a -> createCF(1_000_000, "a2").thenAcceptAsync(
                                b -> createCF(2_000_000, "b1").join(), EXECUTOR
                        ).join(), EXECUTOR
                ).whenComplete((v, t) -> System.out.printf("Group 2 duration %s%n", formatDuration(System.currentTimeMillis() - start))),

                createCF(4_000_000, "3").thenAccept(
                        a -> createCF(1_000_000, "a5").thenRun(createRunnable(20_000_000, "r3")).join()
                ).whenComplete((v, t) -> System.out.printf("Group 3 duration %s%n", formatDuration(System.currentTimeMillis() - start))),

                createCF(3_000_000, "11").thenAcceptAsync(
                        a -> createCF(1_000_000, "a3").join(), EXECUTOR
                ).whenComplete((v, t) -> System.out.printf("Group 11 duration %s%n", formatDuration(System.currentTimeMillis() - start))),

                createCF(2_000_000, "12").thenAcceptAsync(
                        a -> createCF(5_000_000, "a4").thenAcceptAsync(
                                b -> createCF(2_000_000, "b2").join(), EXECUTOR
                        ).join(), EXECUTOR
                ).whenComplete((v, t) -> System.out.printf("Group 12 duration %s%n", formatDuration(System.currentTimeMillis() - start))),

                createCF(10_500_000, "20").thenRun(createRunnable(5_000_000, "r1")),
                createCF(15_500_000, "21").thenRun(createRunnable(2_000_000, "r2"))
        ).join();
        System.out.println();
        //sleepEnough();
        System.out.printf("Duration %s", formatDuration(System.currentTimeMillis() - start));
    }

    CompletableFuture<Void> createCF(final int limit, final String name) {
        return CompletableFuture.runAsync(createRunnable(limit, name), EXECUTOR);
    }

    CompletableFuture<Void> createCF(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, EXECUTOR);
    }

    private Runnable createRunnable(int limit, String name) {
        return () -> {
            var start = System.currentTimeMillis();
            List<Integer> list = Stream.generate(() -> new Random().nextInt(1000))
                    .limit(limit)
                    .toList();
            System.out.printf("%2s-done, %08d, duration %2s, %s%n",
                    name,
                    list.size(),
                    formatDuration(System.currentTimeMillis() - start),
                    Thread.currentThread().getName()
            );
        };
    }
}
