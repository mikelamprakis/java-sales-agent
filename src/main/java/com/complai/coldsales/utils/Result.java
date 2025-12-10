package com.complai.coldsales.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Functional Result type representing either success (Ok) or failure (Err).
 * 
 * Eliminates reliance on magic strings/objects and enables declarative error handling.
 * Inspired by Rust's Result<T, E> and functional Either types.
 * 
 * @param <T> Success value type
 * @param <E> Error value type
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Err {
    
    /**
     * Create a successful result.
     */
    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }
    
    /**
     * Create a failed result.
     */
    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }
    
    /**
     * Check if this is a success.
     */
    boolean isOk();
    
    /**
     * Check if this is a failure.
     */
    default boolean isErr() {
        return !isOk();
    }
    
    /**
     * Get the success value, or throw if this is an error.
     */
    T unwrap() throws IllegalStateException;
    
    /**
     * Get the error value, or throw if this is a success.
     */
    E unwrapErr() throws IllegalStateException;
    
    /**
     * Get the success value as an Optional.
     */
    Optional<T> ok();
    
    /**
     * Get the error value as an Optional.
     */
    Optional<E> err();
    
    /**
     * Map the success value to another type.
     */
    <U> Result<U, E> map(Function<? super T, ? extends U> mapper);
    
    /**
     * Map the error value to another type.
     */
    <F> Result<T, F> mapErr(Function<? super E, ? extends F> mapper);
    
    /**
     * FlatMap (bind) - chain operations that may fail.
     */
    <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper);
    
    /**
     * Recover from an error by providing a fallback value.
     */
    T unwrapOr(T fallback);
    
    /**
     * Recover from an error by computing a fallback value.
     */
    T unwrapOrElse(Function<? super E, ? extends T> fallback);
    
    /**
     * Convert a Result wrapped in a CompletableFuture to a CompletableFuture of Result.
     * Useful for chaining async operations.
     */
    static <T, E> CompletableFuture<Result<T, E>> fromFuture(
            CompletableFuture<Result<T, E>> future
    ) {
        return future;
    }
    
    /**
     * Wrap a CompletableFuture that may throw into a Result.
     */
    static <T> CompletableFuture<Result<T, ServiceError>> catchAsync(
            CompletableFuture<T> future,
            Function<Throwable, ServiceError> errorMapper
    ) {
        return future
                .<Result<T, ServiceError>>thenApply(Result::ok)
                .exceptionally(throwable -> Result.err(errorMapper.apply(throwable)));
    }
    
    /**
     * Chain async operations that return Results.
     * If this Result is Ok, apply the mapper; if Err, pass through the error.
     */
    default <U> CompletableFuture<Result<U, E>> thenComposeAsync(
            Function<? super T, CompletableFuture<Result<U, E>>> mapper
    ) {
        if (this instanceof Ok<T, E> ok) {
            return mapper.apply(ok.value);
        } else {
            @SuppressWarnings("unchecked")
            Err<U, E> err = (Err<U, E>) this;
            return CompletableFuture.completedFuture(err);
        }
    }
    
    /**
     * Success variant.
     */
    record Ok<T, E>(T value) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return true;
        }
        
        @Override
        public T unwrap() {
            return value;
        }
        
        @Override
        public E unwrapErr() {
            throw new IllegalStateException("Called unwrapErr() on Ok value");
        }
        
        @Override
        public Optional<T> ok() {
            return Optional.of(value);
        }
        
        @Override
        public Optional<E> err() {
            return Optional.empty();
        }
        
        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return new Ok<>(mapper.apply(value));
        }
        
        @Override
        public <F> Result<T, F> mapErr(Function<? super E, ? extends F> mapper) {
            return new Ok<>(value);
        }
        
        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            return mapper.apply(value);
        }
        
        @Override
        public T unwrapOr(T fallback) {
            return value;
        }
        
        @Override
        public T unwrapOrElse(Function<? super E, ? extends T> fallback) {
            return value;
        }
    }
    
    /**
     * Error variant.
     */
    record Err<T, E>(E error) implements Result<T, E> {
        @Override
        public boolean isOk() {
            return false;
        }
        
        @Override
        public T unwrap() {
            throw new IllegalStateException("Called unwrap() on Err value: " + error);
        }
        
        @Override
        public E unwrapErr() {
            return error;
        }
        
        @Override
        public Optional<T> ok() {
            return Optional.empty();
        }
        
        @Override
        public Optional<E> err() {
            return Optional.of(error);
        }
        
        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return new Err<>(error);
        }
        
        @Override
        public <F> Result<T, F> mapErr(Function<? super E, ? extends F> mapper) {
            return new Err<>(mapper.apply(error));
        }
        
        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            return new Err<>(error);
        }
        
        @Override
        public T unwrapOr(T fallback) {
            return fallback;
        }
        
        @Override
        public T unwrapOrElse(Function<? super E, ? extends T> fallback) {
            return fallback.apply(error);
        }
    }
}

