package com.complai.coldsales.agents.base;

import com.complai.coldsales.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Utils class.
 */
class UtilsTest {
    
    @Test
    void testTraceRunnable() {
        AtomicBoolean executed = new AtomicBoolean(false);
        
        Utils.trace("Test Trace", () -> {
            executed.set(true);
        });
        
        assertTrue(executed.get());
    }
    
    @Test
    void testTraceSupplier() {
        String result = Utils.trace("Test Trace", () -> "test result");
        
        assertEquals("test result", result);
    }
    
    @Test
    void testTraceWithException() {
        AtomicBoolean finallyExecuted = new AtomicBoolean(false);
        
        assertThrows(RuntimeException.class, () -> {
            Utils.trace("Test Trace", () -> {
                try {
                    throw new RuntimeException("Test exception");
                } finally {
                    finallyExecuted.set(true);
                }
            });
        });
        
        // Trace should still complete even with exception
        assertTrue(finallyExecuted.get());
    }
    
    @Test
    void testTraceSupplierWithException() {
        assertThrows(RuntimeException.class, () -> {
            Utils.trace("Test Trace", () -> {
                throw new RuntimeException("Test exception");
            });
        });
    }
}

