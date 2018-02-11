package com.jsdiuf.jsdiuf.thread;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 20/12/2016.
 * a thread pool that first expand its thread pool when the number of task exceeds
 * core pool size, and put them in a queue upon the pool size reached its maximum value.
 *
 * @author YuanhangWang
 */
public final class DynamicThreadPoolExecutor extends AbstractExecutorService {
    private final int availableQueueSize;
    private final ThreadPoolExecutor submitter;
    private final ThreadPoolExecutor executor;

    private DynamicThreadPoolExecutor(int corePoolSize,
                                      int maxPoolSize,
                                      int availableQueueSize,
                                      long keepAliveTime,
                                      TimeUnit unit,
                                      String name) {
        this.availableQueueSize = availableQueueSize;

        this.submitter = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new SyncFileThreadFactory(name));

        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                unit,
                new SynchronousQueue<Runnable>());

        executor.setRejectedExecutionHandler(new Enqueuing());
    }

    public static Builder of(@Nonnull final String name) {
        return new Builder(name);
    }

    @Override
    public void execute(@Nonnull final Runnable command) {
        if (command instanceof Submitter) {
            submitter.execute(command);
        } else {
            submitter.execute(newTaskFor(command, null));
        }
    }

    public boolean isAvailable() {
        return submitter.getQueue().size() < availableQueueSize;
    }

    ThreadPoolExecutor executor() {
        return executor;
    }

    ThreadPoolExecutor submitter() {
        return submitter;
    }

    @Override
    public void shutdown() {
        submitter.shutdown();
        executor.shutdown();
    }

    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        long start = System.currentTimeMillis();
        if (!submitter.awaitTermination(timeout, unit)) {
            return false;
        }
        long finish = System.currentTimeMillis();
        long left = unit.toMillis(timeout) - (finish - start);
        return executor.awaitTermination(left > 0 ? left : 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the task queue used by this executor. Access to the
     * task queue is intended primarily for debugging and monitoring.
     * This queue may be in active use.  Retrieving the task queue
     * does not prevent queued tasks from executing.
     *
     * @return the task queue
     * @since 1.3.3
     */
    public BlockingQueue<Runnable> getQueue() {
        return submitter.getQueue();
    }

    /**
     * Returns the maximum allowed number of threads.
     *
     * @return the maximum allowed number of threads
     * @since 1.3.3
     */
    public int getMaximumPoolSize() {
        return executor.getMaximumPoolSize();
    }

    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     * @since 1.3.3
     */
    public int getPoolSize() {
        return executor.getPoolSize();
    }

    @Override
    public boolean isShutdown() {
        return submitter.isShutdown() && executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return submitter.isTerminated() && executor.isTerminated();
    }

    @Nonnull
    @Override
    public List<Runnable> shutdownNow() {
        final List<Runnable> left = submitter.shutdownNow();
        left.addAll(executor.shutdownNow());
        return left;
    }

    @Override
    public String toString() {
        return "submitter: {" + submitter.toString() +
                "}, executor: {" + executor.toString() + "}";
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> command) {
        if (command == null) {
            throw new NullPointerException("cannot submit null to execution pool");
        }

        return new Submitter<>(executor, command);

    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable command, T value) {
        if (command == null) {
            throw new NullPointerException("cannot submit null to execution pool");
        }

        return new Submitter<>(executor, command, value);

    }

    static class Enqueuing implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                return;
            }
            if (r instanceof Submitter && ((Submitter) r).state.get() != Submitter.State.NEW) {
                return;
            }
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException ignored) {
                if (r instanceof Submitter) {
                    ((Submitter<?>) r).state.compareAndSet(
                            Submitter.State.SUBMITTING, Submitter.State.CANCELLED);
                }
            }
        }
    }

    static class SubmitterFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String name;

        private SubmitterFactory(String group, String name) {
            this.group = new ThreadGroup(group);
            this.name = name;
        }

        @Override
        public Thread newThread(@Nonnull Runnable task) {
            return new Thread(group, task, name);
        }
    }

    public static class Submitter<T> implements RunnableFuture<T> {
        private final ThreadPoolExecutor executor;
        private final RunnableFuture<T> command;
        private final long createdMilli;
        private final long createdNano;

        private final AtomicReference<State> state = new AtomicReference<>(State.NEW);

        private Submitter(@Nonnull final ThreadPoolExecutor executor,
                          @Nonnull final Runnable command,
                          @Nullable final T result) {
            this.executor = executor;
            this.command = new FutureTask<>(command, result);
            this.createdMilli = System.currentTimeMillis();
            this.createdNano = System.nanoTime();
        }

        private Submitter(@Nonnull final ThreadPoolExecutor executor,
                          @Nonnull final Callable<T> command) {
            this.executor = executor;
            this.command = new FutureTask<>(command);
            this.createdMilli = System.currentTimeMillis();
            this.createdNano = System.nanoTime();
        }

        @Override
        public void run() {
            if (!state.compareAndSet(State.NEW, State.SUBMITTING)) {
                return;
            }
            executor.execute(command);
            state.compareAndSet(State.SUBMITTING, State.SUBMITTED);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            switch (state.get()) {
                case NEW:
                    return state.compareAndSet(State.NEW, State.CANCELLED) || command.cancel(mayInterruptIfRunning);
                case SUBMITTING:
                case SUBMITTED:
                    return command.cancel(mayInterruptIfRunning);
                default:
                    return false;
            }
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return command.get();
        }

        @Override
        public T get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return command.get(timeout, unit);
        }

        @Override
        public boolean isCancelled() {
            return state.get() == State.CANCELLED || command.isCancelled();
        }

        @Override
        public boolean isDone() {
            return state.get() == State.CANCELLED || command.isDone();
        }

        long getCreatedMilli() {
            return createdMilli;
        }

        long getCreatedNano() {
            return createdNano;
        }

        private enum State {
            NEW, SUBMITTING, SUBMITTED, CANCELLED
        }

    }

    public static class Builder {
        private final int numCores = Runtime.getRuntime().availableProcessors();

        private final String name;
        private int corePoolSize = numCores;
        private int maxPoolSize = numCores;
        private int availableQueueSize = Integer.MAX_VALUE;
        private long keepAliveTime = 0L;
        private TimeUnit unit = TimeUnit.MILLISECONDS;

        private Builder(@Nonnull final String name) {
            this.name = name;
        }

        public Builder withCorePoolSize(final int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        public Builder withMaxPoolSize(final int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder withAvailableQueueSize(final int availableQueueSize) {
            this.availableQueueSize = availableQueueSize;
            return this;
        }

        public Builder keepAliveIn(final long keepAliveTime,
                                   @Nonnull final TimeUnit unit) {
            this.keepAliveTime = keepAliveTime;
            this.unit = unit;
            return this;
        }


        public DynamicThreadPoolExecutor build() {
            return new DynamicThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    availableQueueSize,
                    keepAliveTime,
                    unit,
                    name);

        }
    }

}
