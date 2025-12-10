package com.complai.coldsales.managers.reporting;

import java.util.function.Supplier;

/**
 * Synchronous step reporter with backward-compatible hooks and timing.
 * Existing reporters can override startLog/completeLog(T).
 * New reporters may override completeLogWithDuration(T, long) to include timing.
 */
public abstract class StepReporter<T> {
    
    protected void startLog() {
        // default no-op
    }
    
    protected abstract void completeLogWithDuration(T result, long durationMs);
    
    protected void errorLog(Exception e) {
        System.err.println("❌ Step error: " + e.getMessage());
    }
    
    public T run(Supplier<T> supplier){
        startLog();
        long t0 = System.currentTimeMillis();
        try {
            T result = supplier.get();
            long elapsed = System.currentTimeMillis() - t0;
            completeLogWithDuration(result, elapsed);
            return result;
        } catch (RuntimeException e) {
            errorLog(e);
            throw e;
        }
    }

    /**
     * Asynchronous variant that accepts a supplier producing a CompletableFuture.
     * Reports start and completion (with timing) when the future completes.
     */
    public java.util.concurrent.CompletableFuture<T> runAsync(java.util.function.Supplier<java.util.concurrent.CompletableFuture<T>> supplier){
        startLog();
        long t0 = System.currentTimeMillis();
        try {
            return supplier.get().whenComplete((result, throwable) -> {
                long elapsed = System.currentTimeMillis() - t0;
                if (throwable != null) {
                    if (throwable instanceof Exception) {
                        errorLog((Exception) throwable);
                    } else {
                        System.err.println("❌ Step error: " + throwable.getMessage());
                    }
                } else {
                    completeLogWithDuration(result, elapsed);
                }
            });
        } catch (RuntimeException e) {
            errorLog(e);
            throw e;
        }
    }
}
