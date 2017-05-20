package com.nordstrom.common.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.nordstrom.common.base.UncheckedThrow;

import java.sql.PreparedStatement;

public class DatabaseUtils {
	
	private DatabaseUtils() {
		throw new AssertionError("DatabaseUtils is a static utility class that cannot be instantiated");
	}
	
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to load the Oracle JDBC driver", e);
		}
	}
	
	/**
	 * Execute the specified query object with supplied arguments as an 'update' operation
	 * 
	 * @param query query object to execute
	 * @param queryArgs replacement values for query place-holders
	 * @return count of records updated
	 */
	public static int update(QueryAPI query, Object... queryArgs) {
		Integer result = (Integer) executeOracleQuery(null, query, queryArgs);
		return result.intValue();
	}
	
	/**
	 * Execute the specified query object with supplied arguments as a 'query' operation
	 * 
	 * @param query query object to execute
	 * @param queryArgs replacement values for query place-holders
	 * @return row 1 / column 1 as integer; -1 if no rows were returned
	 */
	public static int getInt(QueryAPI query, Object... queryArgs) {
		Integer result = (Integer) executeOracleQuery(Integer.class, query, queryArgs);
		return result.intValue();
	}
	
	/**
	 * Execute the specified query object with supplied arguments as a 'query' operation
	 * 
	 * @param query query object to execute
	 * @param queryArgs replacement values for query place-holders
	 * @return row 1 / column 1 as string; 'null' if no rows were returned
	 */
	public static String getString(QueryAPI query, Object... queryArgs) {
		return (String) executeOracleQuery(String.class, query, queryArgs);
	}
	
	/**
	 * Execute the specified query with the supplied arguments, returning a result of the indicated type.
	 * <p>
	 * <b>TYPES</b>: Specific result types produce the following behaviors: <ul>
	 * <li>'null' - The query is executed as an update operation.</li>
	 * <li>{@link ResultPackage} - An object containing the connection, statement, and result set is returned</li>
	 * <li>{@link Integer} - If rows were returned, row 1 / column 1 is returned as an Integer; otherwise -1</li>
	 * <li>{@link String} - If rows were returned, row 1 / column 1 is returned as an String; otherwise 'null'</li>
	 * <li>For other types, {@link ResultSet#getObject(int, Class)} to return row 1 / column 1 as that type</li></ul>
	 * 
	 * @param resultType desired result type (see TYPES above)
	 * @param query query object to execute
	 * @param queryArgs replacement values for query place-holders
	 * @return for update operations, the number of rows affected; for query operations, an object of the indicated type<br>
	 * <b>NOTE</b>: If you specify {@link ResultPackage} as the result type, it's recommended that you close this object
	 * when you're done with it to free up database and JDBC resources that were allocated for it. 
	 */
	private static Object executeOracleQuery(Class<?> resultType, QueryAPI query, Object... queryArgs) {
		int expectCount = query.getArgCount();
		int actualCount = queryArgs.length;
		
		if (actualCount != expectCount) {
			String message;
			
			if (expectCount == 0) {
				message = "No arguments expected for " + query.getEnum().name();
			} else {
				message = String.format("Incorrect argument count for %s%s: expect: %d; actual: %d", 
						query.getEnum().name(), Arrays.toString(query.getArgNames()), expectCount, actualCount);
			}
			
			throw new IllegalArgumentException(message);
		}
		
		return executeOracleQuery(resultType, query.getConnection(), query.getQueryStr(), queryArgs);
	}
	
	/**
	 * Execute the specified query with the supplied arguments, returning a result of the indicated type.
	 * <p>
	 * <b>TYPES</b>: Specific result types produce the following behaviors: <ul>
	 * <li>'null' - The query is executed as an update operation.</li>
	 * <li>{@link ResultPackage} - An object containing the connection, statement, and result set is returned</li>
	 * <li>{@link Integer} - If rows were returned, row 1 / column 1 is returned as an Integer; otherwise -1</li>
	 * <li>{@link String} - If rows were returned, row 1 / column 1 is returned as an String; otherwise 'null'</li>
	 * <li>For other types, {@link ResultSet#getObject(int, Class)} to return row 1 / column 1 as that type</li></ul>
	 * 
	 * @param resultType desired result type (see TYPES above)
	 * @param connectionStr Oracle database connection string
	 * @param queryStr a SQL statement that may contain one or more '?' IN parameter placeholders
	 * @param param an array of objects containing the input parameter values
	 * @return for update operations, the number of rows affected; for query operations, an object of the indicated type<br>
	 * <b>NOTE</b>: If you specify {@link ResultPackage} as the result type, it's recommended that you close this object
	 * when you're done with it to free up database and JDBC resources that were allocated for it. 
	 */
	public static Object executeOracleQuery(Class<?> resultType, String connectionStr, String queryStr, Object... param) {
		Object result = null;
		boolean failed = false;
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = getOracleConnection(connectionStr);
			statement = connection.prepareStatement(queryStr);
			
			for (int i = 0; i < param.length; i++) {
				statement.setObject(i + 1, param[i]);
			}
			
			if (resultType == null) {
				result = Integer.valueOf(statement.executeUpdate());
			} else {
				resultSet = statement.executeQuery();
				
				if (resultType == ResultPackage.class) {
					result = new ResultPackage(connection, statement, resultSet);
				} else if (resultType == Integer.class) {
					result = Integer.valueOf((resultSet.next()) ? resultSet.getInt(1) : -1);
				} else if (resultType == String.class) {
					result = (resultSet.next()) ? resultSet.getString(1) : null;
				} else {
					result = (resultSet.next()) ? resultSet.getObject(1, resultType) : null;
				}
			}

		} catch (SQLException e) {
			failed = true;
			throw UncheckedThrow.throwUnchecked(e);
		} finally {
			if (failed || (resultType != ResultPackage.class)) {
				if (resultSet != null) {
					try {
						resultSet.close();
					} catch (SQLException e) { }
				}
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) { }
				}
				if (connection != null) {
					try {
						connection.commit();
						connection.close();
					} catch (SQLException e) { }
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Get a connection to the Oracle database associated with the specified connection string.
	 * 
	 * @param connectionString Oracle database connection string
	 * @return Oracle database connection object
	 */
	private static Connection getOracleConnection(String connectionString) {
		try {
			QueryCreds creds = new QueryCreds(connectionString);
			return DriverManager.getConnection(creds.url, creds.userId, creds.password);
		} catch (SQLException e) {
			throw UncheckedThrow.throwUnchecked(e);
		}
	}
	
	/**
	 * This class encapsulated database query credentials.
	 */
	private static class QueryCreds {
		
		private String url;
		private String userId;
		private String password;
		
		/**
		 * Constructor for database query credentials.
		 * 
		 * @param connectionString database connection string
		 */
		private QueryCreds(String connectionString) {
			String[] bits = connectionString.split(";");
			
			url = bits[0].trim();
			userId = bits[1].split("=")[1].trim();
			password = bits[2].split("=")[1].trim();
		}
	}
	
	/**
	 * This interface defines the API supported by database query collections
	 */
	public interface QueryAPI {
		
		/**
		 * Get the query string for this query object.
		 * 
		 * @return query object query string
		 */
		String getQueryStr();
		
		/**
		 * Get the argument name for this query object
		 *  
		 * @return query object argument names
		 */
		String[] getArgNames();
		
		/**
		 * Get the count of arguments for this query object.
		 * 
		 * @return query object argument count
		 */
		int getArgCount();
		
		/**
		 * Get the database connection string for this query object.
		 * 
		 * @return query object connection string
		 */
		String getConnection();
		
		/**
		 * Get the implementing enumerated constant for this query object.
		 * 
		 * @return query object enumerated constant
		 */
		Enum<?> getEnum();
	}
	
	/**
	 * This class defines a package of database objects associated with a query. These include:<ul>
	 * <li>{@link Connection} object</li>
	 * <li>{@link PreparedStatement} object</li>
	 * <li>{@link ResultSet} object</li></ul>
	 */
	public static class ResultPackage implements AutoCloseable {
		
		private Connection connection;
		private PreparedStatement statement;
		private ResultSet resultSet;
		
		/**
		 * Constructor for a result package object
		 * 
		 * @param connection {@link Connection} object
		 * @param statement {@link PreparedStatement} object
		 * @param resultSet {@link ResultSet} object
		 */
		private ResultPackage(Connection connection, PreparedStatement statement, ResultSet resultSet) {
			this.connection = connection;
			this.statement = statement;
			this.resultSet = resultSet;
		}
		
		/**
		 * Get the result set object of this package.
		 * 
		 * @return {@link ResultSet} object
		 */
		public ResultSet getResultSet() {
			if (resultSet != null) return resultSet;
			throw new IllegalStateException("The result set in this package has been closed");
		}
		
		@Override
		public void close() {
			if (resultSet != null) {
				try {
					resultSet.close();
					resultSet = null;
				} catch (SQLException e) { }
			}
			if (statement != null) {
				try {
					statement.close();
					statement = null;
				} catch (SQLException e) { }
			}
			if (connection != null) {
				try {
					connection.commit();
					connection.close();
					connection = null;
				} catch (SQLException e) { }
			}
		}
	}
}
