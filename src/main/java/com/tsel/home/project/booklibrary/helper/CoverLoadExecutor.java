package com.tsel.home.project.booklibrary.helper;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Выполняет запросы видимых обложек перед остальными, сохраняя FIFO внутри группы.
 * Изменяет приоритет только ожидающих задач, не прерывая начатое чтение.
 */
final class CoverLoadExecutor extends ThreadPoolExecutor {

    private final AtomicLong sequence = new AtomicLong();

    CoverLoadExecutor(int threads) {
        super(threads, threads, 0, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), action -> {
            Thread thread = new Thread(action, "book-cover-loader");
            thread.setDaemon(true);
            return thread;
        });
    }

    Future<?> submitCover(Runnable action, boolean visible) {
        CoverTask<Void> task = new CoverTask<>(Executors.callable(action, null), visible);
        execute(task);
        return task;
    }

    void prioritize(Set<Future<?>> visible) {
        for (Runnable runnable : getQueue().toArray(new Runnable[0])) {
            CoverTask<?> task = (CoverTask<?>) runnable;
            boolean priority = visible.contains(task);
            // The heap must never contain a task while its comparison key changes.
            if (task.visible != priority && getQueue().remove(task)) {
                task.visible = priority;
                if (!task.isCancelled() && !isShutdown()) {
                    getQueue().offer(task);
                }
            }
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new CoverTask<>(Executors.callable(runnable, value), false);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new CoverTask<>(callable, false);
    }

    private final class CoverTask<T> extends FutureTask<T> implements Comparable<CoverTask<?>> {

        private final long order = sequence.getAndIncrement();
        private boolean visible;

        private CoverTask(Callable<T> callable, boolean visible) {
            super(callable);
            this.visible = visible;
        }

        @Override
        public int compareTo(CoverTask<?> other) {
            int priority = Boolean.compare(other.visible, visible);
            return priority == 0 ? Long.compare(order, other.order) : priority;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                remove(this);
            }
        }
    }
}
