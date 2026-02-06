package com.nordstrom.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A specialized {@link Iterator} that traverses through all outcomes of a SQL execution.
 * <p>
 * This class wraps a JDBC {@link Statement} and iterates over both {@link ResultSetResult} 
 * (for queries) and {@link UpdateCountResult} (for DML operations). It follows the JDBC 
 * "multiple results" protocol, allowing seamless processing of stored procedures or 
 * batched statements that return mixed outputs.
 * </p>
 *
 * <p><strong>Resource Management:</strong> As an {@link AutoCloseable} resource, this 
 * iterator must be closed after use to ensure the underlying {@code Statement} 
 * is released properly.</p>
 */
public final class ExecutionResultIterator implements Iterator<ExecutionResult>, AutoCloseable {
    private final Statement statement;
    private boolean hasMoreResults = true;
    private boolean isFirstCheck = true;
    private ExecutionResult nextResult;

    /**
     * Constructs a new iterator for navigating the results of an executed {@link Statement}.
     * <p>
     * This constructor accepts an optional "initial" {@link ResultSet}. If provided, this 
     * result set is treated as the first element in the iteration sequence. This is 
     * essential for JDBC statements where the first result is already available 
     * immediately after {@code statement.execute()} is called.
     * </p>
     * 
     * @param statement the executed {@link Statement} to iterate over.
     * @param initialResultSet the first {@link ResultSet} found during the initial execution, 
     * or {@code null} if the iteration should start by advancing 
     * the statement state.
     */
    public ExecutionResultIterator(Statement statement, ResultSet initialResultSet) {
        this.statement = statement;
        if (initialResultSet != null) {
            this.nextResult = new ResultSetResult(initialResultSet);
        }
    }

    @Override
    public boolean hasNext() {
        if (nextResult != null) return true;
        if (!hasMoreResults) return false;

        try {
            // Only advance the JDBC cursor if we've already used the initial result
            if (!isFirstCheck) {
                hasMoreResults = statement.getMoreResults();
            }
            
            isFirstCheck = false;

            if (hasMoreResults || statement.getUpdateCount() != -1) {
                ResultSet rs = statement.getResultSet();
                if (rs != null) {
                    nextResult = new ResultSetResult(rs);
                } else {
                    int count = statement.getUpdateCount();
                    if (count != -1) {
                        nextResult = new UpdateCountResult(count);
                    } else {
                        hasMoreResults = false;
                    }
                }
            } else {
                hasMoreResults = false;
            }
            return nextResult != null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExecutionResult next() {
        if (!hasNext()) throw new NoSuchElementException();
        ExecutionResult result = nextResult;
        nextResult = null;
        return result;
    }

    @Override
    public void close() {
        // nothing to do here
    } 
}
