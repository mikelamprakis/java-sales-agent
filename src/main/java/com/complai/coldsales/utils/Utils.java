package com.complai.coldsales.utils;

/**
 * Utility functions for agents.
 */
public class Utils {

    /**
     * Simple trace logging for agent execution.
     *
     * @param name Name of the trace
     * @param runnable Code to execute within the trace
     */
    public static void trace(String name, Runnable runnable) {
        System.out.println("🔍 Starting trace: " + name);
        try {
            runnable.run();
        } finally {
            System.out.println("🔍 Completed trace: " + name);
        }
    }

    /**
     * Async version of trace that returns a value.
     *
     * @param name Name of the trace
     * @param supplier Code to execute within the trace
     * @param <T> Return type
     * @return The result of the supplier
     */
    public static <T> T trace(String name, java.util.function.Supplier<T> supplier) {
        System.out.println("🔍 Starting trace: " + name);
        try {
            return supplier.get();
        } finally {
            System.out.println("🔍 Completed trace: " + name);
        }
    }
}
