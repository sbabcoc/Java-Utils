package com.nordstrom.common.jdbc;

/**
 * Represents the result of a database operation or statement execution.
 * <p>
 * This interface serves as a common supertype for various types of execution outputs,
 * such as query results ({@link ResultSetResult}), update counts, or generated keys. 
 * It provides a unified way to handle different return types from an execution engine.
 *
 * @see ResultSetResult
 */
public interface ExecutionResult {
}
