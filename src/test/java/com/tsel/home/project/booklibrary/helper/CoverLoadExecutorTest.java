package com.tsel.home.project.booklibrary.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CoverLoadExecutorTest {

    @Test
    void shouldPromoteCurrentViewportAndDemotePreviousWithoutDuplicatingWork() throws Exception {
        CoverLoadExecutor executor = new CoverLoadExecutor(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        try {
            executor.submit(() -> await(release));
            Future<?> first = executor.submitCover(() -> order.add(1), false);
            Future<?> second = executor.submitCover(() -> order.add(2), false);
            Future<?> third = executor.submitCover(() -> order.add(3), false);
            Future<?> fourth = executor.submitCover(() -> order.add(4), false);
            executor.prioritize(Set.of(third, fourth));
            executor.prioritize(Set.of(second, fourth));
            executor.prioritize(Set.of(second, fourth));
            release.countDown();
            for (Future<?> task : List.of(first, second, third, fourth)) {
                task.get(5, TimeUnit.SECONDS);
            }
            assertEquals(List.of(2, 4, 1, 3), order);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldRemoveCancelledTasksAndPrioritizeNewVisibleRequests() throws Exception {
        CoverLoadExecutor executor = new CoverLoadExecutor(1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        try {
            executor.submit(() -> {
                started.countDown();
                await(release);
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            Future<?> background = executor.submitCover(() -> order.add(1), false);
            Future<?> cancelled = executor.submitCover(() -> order.add(2), true);
            Future<?> visible = executor.submitCover(() -> order.add(3), true);
            cancelled.cancel(true);
            assertEquals(2, executor.getQueue().size());
            release.countDown();
            background.get(5, TimeUnit.SECONDS);
            visible.get(5, TimeUnit.SECONDS);
            assertEquals(List.of(3, 1), order);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
