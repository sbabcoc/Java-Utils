package com.nordstrom.common.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nordstrom.common.jdbc.DatabaseUtils.QueryAPI;
import com.nordstrom.common.jdbc.DatabaseUtils.ResultPackage;

public class DatabaseUtilsTest {
    
    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to load the Derby JDBC driver", e);
        }
    }
    
    @BeforeClass
    public static void startDerby() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(TestQuery.connection() + ";create=true");
        } catch (SQLException e) {
            printSQLException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    printSQLException(e);
                }
            }
        }
    }
    
    @AfterClass
    public static void stopDerby() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(TestQuery.connection() + ";shutdown=true");
        } catch (SQLException e) {
            if ( (e.getErrorCode() == 45000) && ("08006".equals(e.getSQLState())) ) {
                // we got the expected exception
                System.out.println("Derby shut down normally");
            } else {
                System.err.println("Derby did not shut down normally");
                printSQLException(e);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    printSQLException(e);
                }
            }
        }
    }
    
    @Test
    public void createTable() {
        DatabaseUtils.update(TestQuery.CREATE);
    }
    
    @Test(dependsOnMethods= {"createTable"})
    public void insertRows() {
        DatabaseUtils.update(TestQuery.INSERT, 1956, "Webster St.");
        DatabaseUtils.update(TestQuery.INSERT, 1910, "Union St.");
    }
    
    @Test(dependsOnMethods={"insertRows"})
    public void updateRows() {
        DatabaseUtils.update(TestQuery.UPDATE, 180, "Grand Ave.", 1956);
        DatabaseUtils.update(TestQuery.UPDATE, 300, "Lakeshore Ave.", 180);
    }
    
    @Test(dependsOnMethods={"updateRows"})
    public void getInt() {
        DatabaseUtils.getInt(TestQuery.GET_NUM);
    }
    
    @Test(dependsOnMethods={"getInt"})
    public void getString() {
        DatabaseUtils.getString(TestQuery.GET_STR);
    }
    
    @Test(dependsOnMethods={"getString"})
    public void getResultPackage() {
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestQuery.GET_RESULT_PACKAGE);
        pkg.close();
    }
    
    @Test(dependsOnMethods={"getResultPackage"}, alwaysRun=true)
    public void dropTable() {
        DatabaseUtils.update(TestQuery.DROP);
    }
    
    enum TestQuery implements QueryAPI {
        CREATE("create table location(num int, addr varchar(40))"),
        INSERT("insert into location values (?, ?)", "num", "addr"),
        UPDATE("update location set num=?, addr=? where num=?", "num", "addr", "whereNum"),
        GET_NUM("select num from location where addr='Union St.'"),
        GET_STR("select addr from location where num=1910"),
        GET_RESULT_PACKAGE("select * from location"),
        DROP("drop table location");
        
        private String query;
        private String[] args;
        
        TestQuery(String query, String... args) {
            this.query = query;
            this.args = args;
        }

        @Override
        public String getQueryStr() {
            return query;
        }

        @Override
        public String[] getArgNames() {
            return args;
        }

        @Override
        public int getArgCount() {
            return args.length;
        }

        @Override
        public String getConnection() {
            return connection();
        }

        @Override
        public Enum<TestQuery> getEnum() {
            return this;
        }
        
        public static String connection() {
            return "jdbc:derby:@TestDB";
        }
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            e = e.getNextException();
        }
    }

}
