package com.nordstrom.common.jdbc;

import java.sql.ResultSet;

/**
 * An implementation of {@link ExecutionResult} that encapsulates a JDBC {@link ResultSet}.
 * <p>
 * This class serves as a wrapper for results returned from database queries,
 * providing a consistent interface for handling data retrieved from the execution
 * of SQL statements.
 */public final class ResultSetResult implements ExecutionResult {

    private final ResultSet resultSet;

    /**
     * Constructs a new {@code ResultSetResult} with the specified result set.
     *
     * @param resultSet the JDBC {@link ResultSet} containing the data from the execution;
     * should not be null.
     */
    public ResultSetResult(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    /**
     * Retrieves the underlying JDBC result set.
     *
     * @return the {@link ResultSet} associated with this execution result.
     */
    public ResultSet getResultSet() {
        return resultSet;
    }
}
