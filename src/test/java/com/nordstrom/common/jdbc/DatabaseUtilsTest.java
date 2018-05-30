package com.nordstrom.common.jdbc;

import static org.testng.Assert.assertEquals;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nordstrom.common.jdbc.DatabaseUtils.QueryAPI;
import com.nordstrom.common.jdbc.DatabaseUtils.ResultPackage;
import com.nordstrom.common.jdbc.DatabaseUtils.SProcAPI;

public class DatabaseUtilsTest {
    
    static {
        System.setProperty("derby.system.home", "target");
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
    public void showAddresses() {
        try {
            DatabaseUtils.update(TestQuery.SHOW_ADDRESSES);
        } catch (Exception e) {
        }
        
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestSProc.SHOW_ADDRESSES);
        
        int rowCount = 0;
        try {
            while (pkg.getResultSet().next()) {
                rowCount++;
                int num = pkg.getResultSet().getInt("num");
                String addr = pkg.getResultSet().getString("addr");
                System.out.println("addr" + rowCount + ": " + num + " " + addr);
            }
        } catch (SQLException e) {
        }
        pkg.close();
        
        DatabaseUtils.update(TestQuery.DROP_PROC_SHOW);
        assertEquals(rowCount, 2);
    }
    
    @Test(dependsOnMethods={"showAddresses"})
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
    
    @Test
    public void testInVarargs() {
        try {
            DatabaseUtils.update(TestQuery.IN_VARARGS);
        } catch (Exception e) {
        }
        
        String result = DatabaseUtils.getString(TestSProc.IN_VARARGS, "", 5, 4, 3);
        DatabaseUtils.update(TestQuery.DROP_PROC_IN);
        assertEquals(result, "RESULT:  5 4 3");
    }
    
    @Test()
    public void testOutVarargs() throws SQLException {
        try {
            DatabaseUtils.update(TestQuery.OUT_VARARGS);
        } catch (Exception e) {
        }
        
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestSProc.OUT_VARARGS, 5, 0, 0, 0);
        
        int[] out = new int[3];
        out[0] = ((CallableStatement) pkg.getStatement()).getInt(2);
        out[1] = ((CallableStatement) pkg.getStatement()).getInt(3);
        out[2] = ((CallableStatement) pkg.getStatement()).getInt(4);
        pkg.close();
        
        DatabaseUtils.update(TestQuery.DROP_PROC_OUT);
        
        assertEquals(out[0], 5);
        assertEquals(out[1], 6);
        assertEquals(out[2], 7);
    }
    
    @Test
    public void testInOutVarargs() throws SQLException {
        try {
            DatabaseUtils.update(TestQuery.INOUT_VARARGS);
        } catch (Exception e) {
        }
        
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestSProc.INOUT_VARARGS, 5, 3, 10, 100);
        
        int[] out = new int[3];
        out[0] = pkg.getCallable().getInt(2);
        out[1] = pkg.getCallable().getInt(3);
        out[2] = pkg.getCallable().getInt(4);
        pkg.close();
        
        DatabaseUtils.update(TestQuery.DROP_PROC_INOUT);
        
        assertEquals(out[0], 8);
        assertEquals(out[1], 15);
        assertEquals(out[2], 105);
    }
    
    enum TestQuery implements QueryAPI {
        CREATE("create table location(num int, addr varchar(40))"),
        INSERT("insert into location values (?, ?)", "num", "addr"),
        UPDATE("update location set num=?, addr=? where num=?", "num", "addr", "whereNum"),
        SHOW_ADDRESSES("create procedure SHOW_ADDRESSES() parameter style java "
                        + "language java dynamic result sets 1 "
                        + "external name 'com.nordstrom.common.jdbc.StoredProcedure.showAddresses'"),
        DROP_PROC_SHOW("drop procedure SHOW_ADDRESSES"),
        GET_NUM("select num from location where addr='Union St.'"),
        GET_STR("select addr from location where num=1910"),
        GET_RESULT_PACKAGE("select * from location"),
        DROP("drop table location"),
        IN_VARARGS("create procedure IN_VARARGS(out result varchar( 32672 ), b int ...) "
                        + "language java parameter style derby no sql deterministic "
                        + "external name 'com.nordstrom.common.jdbc.StoredProcedure.inVarargs'"),
        DROP_PROC_IN("drop procedure IN_VARARGS"),
        OUT_VARARGS("create procedure OUT_VARARGS(seed int, out b int ...) "
                        + "language java parameter style derby no sql deterministic "
                        + "external name 'com.nordstrom.common.jdbc.StoredProcedure.outVarargs'"),
        DROP_PROC_OUT("drop procedure OUT_VARARGS"),
        INOUT_VARARGS("create procedure INOUT_VARARGS(seed int, inout b int ...) "
                        + "language java parameter style derby no sql deterministic "
                        + "external name 'com.nordstrom.common.jdbc.StoredProcedure.inoutVarargs'"),
        DROP_PROC_INOUT("drop procedure INOUT_VARARGS");
        
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
    
    enum TestSProc implements SProcAPI {
        SHOW_ADDRESSES("SHOW_ADDRESSES()"),
        IN_VARARGS("IN_VARARGS(<, >:)", Types.VARCHAR, Types.INTEGER),
        OUT_VARARGS("OUT_VARARGS(>, <:)", Types.INTEGER, Types.INTEGER),
        INOUT_VARARGS("INOUT_VARARGS(>, =:)", Types.INTEGER, Types.INTEGER);
        
        private int[] argTypes;
        private String signature;
        
        TestSProc(String signature, int... argTypes) {
            this.signature = signature;
            this.argTypes = argTypes;
        }

        @Override
        public String getSignature() {
            return signature;
        }

        @Override
        public int[] getArgTypes() {
            return argTypes;
        }

        @Override
        public String getConnection() {
            return TestQuery.connection();
        }

        @Override
        public Enum<? extends SProcAPI> getEnum() {
            return this;
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
