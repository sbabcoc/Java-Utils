package com.nordstrom.common.jdbc;

/**
 * An implementation of {@link ExecutionResult} that encapsulates a database update count.
 * <p>
 * This class is used to represent the outcome of Data Manipulation Language (DML) 
 * statements—such as {@code INSERT}, {@code UPDATE}, or {@code DELETE}—which return 
 * the number of rows affected rather than a set of data rows.
 * </p>
 * @see ResultSetResult
 * @see ExecutionResult
 */
public final class UpdateCountResult implements ExecutionResult {

    private final int updateCount;

    /**
     * Constructs a new {@code UpdateCountResult} with the specified count.
     *
     * @param updateCount the number of rows affected by the execution; 
     * typically a non-negative integer.
     */
    public UpdateCountResult(int updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * Retrieves the number of rows affected by the database operation.
     *
     * @return the update count.
     */
    public int getUpdateCount() {
        return updateCount;
    }
}
