package com.nordstrom.common.jdbc;

import static org.testng.Assert.assertEquals;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nordstrom.common.jdbc.DatabaseUtils.QueryAPI;
import com.nordstrom.common.jdbc.DatabaseUtils.ResultPackage;
import com.nordstrom.common.jdbc.DatabaseUtils.SProcAPI;

public class DatabaseUtilsTest {

    private static Connection connection;

    static {
        System.setProperty("hsqldb.method_class_names", "com.nordstrom.common.jdbc.StoredProcedure.*");
    }
    
    @BeforeClass
    public static void startDB() throws SQLException {
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
    }

    @AfterClass
    public static void stopDB() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
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
            if (e instanceof SQLException) {
                DatabaseUtils.printSQLException((SQLException) e);
            }
        }
        
        int rowCount = 0;
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestSProc.SHOW_ADDRESSES);
        Iterator<ResultSet> iter = pkg.resultSets();
        while (iter.hasNext()) {
            ResultSet rs = iter.next();
            try {
                while (rs.next()) {
                    rowCount++;
                    int num = rs.getInt("num");
                    String addr = rs.getString("addr");
                    System.out.println("addr" + rowCount + ": " + num + " " + addr);
                }
            } catch (SQLException e) {
                DatabaseUtils.printSQLException(e);
            }
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
        	if (e instanceof SQLException) {
        	    DatabaseUtils.printSQLException((SQLException) e);
        	}
        }
        
        String result = DatabaseUtils.getString(TestSProc.IN_VARARGS, "", 5, 4, 3);
        DatabaseUtils.update(TestQuery.DROP_PROC_IN);
        assertEquals(result, "RESULT: 5 4 3");
    }
    
    @Test
    public void testInOutVarargs() throws SQLException {
        try {
            DatabaseUtils.update(TestQuery.INOUT_VARARGS);
        } catch (Exception e) {
        	if (e instanceof SQLException) {
        	    DatabaseUtils.printSQLException((SQLException) e);
        	}
        }
        
        ResultPackage pkg = DatabaseUtils.getResultPackage(TestSProc.INOUT_VARARGS, 5, 3, 10, 100);
        
        java.sql.Array sqlArray = pkg.getCallable().getArray(2);
        Object[] out = (Object[]) sqlArray.getArray();
        pkg.close();
        
        DatabaseUtils.update(TestQuery.DROP_PROC_INOUT);
        
        assertEquals((Integer) out[0], 8);
        assertEquals((Integer) out[1], 15);
        assertEquals((Integer) out[2], 105);
    }
    
    enum TestQuery implements QueryAPI {
        CREATE("CREATE TABLE IF NOT EXISTS location(num INTEGER, addr VARCHAR(40))"),
        INSERT("INSERT INTO location VALUES (?, ?)", "num", "addr"),
        UPDATE("UPDATE location SET num=?, addr=? WHERE num=?", "num", "addr", "whereNum"),
        SHOW_ADDRESSES(
            "CREATE PROCEDURE SHOW_ADDRESSES() "
            + "LANGUAGE JAVA "
            + "PARAMETER STYLE JAVA "
            + "READS SQL DATA "
            + "DYNAMIC RESULT SETS 1 "
            + "EXTERNAL NAME 'com.nordstrom.common.jdbc.StoredProcedure.showAddresses'"
        ),
        DROP_PROC_SHOW("DROP PROCEDURE IF EXISTS SHOW_ADDRESSES"),
        GET_NUM("SELECT num FROM location WHERE addr='Union St.'"),
        GET_STR("SELECT addr FROM location WHERE num=1910"),
        GET_RESULT_PACKAGE("SELECT * FROM location"),
        DROP("DROP TABLE IF EXISTS location"),
        IN_VARARGS(
            "CREATE PROCEDURE IN_VARARGS(OUT result VARCHAR(2000), IN inputs INTEGER ARRAY) "
            + "LANGUAGE JAVA "
            + "DETERMINISTIC "
            + "PARAMETER STYLE JAVA "
            + "EXTERNAL NAME 'CLASSPATH:com.nordstrom.common.jdbc.StoredProcedure.inVarargs'"
        ),
        DROP_PROC_IN("DROP PROCEDURE IF EXISTS IN_VARARGS"),
        INOUT_VARARGS(
            "CREATE PROCEDURE INOUT_VARARGS(IN seed INTEGER, INOUT b INTEGER ARRAY) "
            + "LANGUAGE JAVA "
            + "READS SQL DATA "
            + "PARAMETER STYLE JAVA "
            + "EXTERNAL NAME 'CLASSPATH:com.nordstrom.common.jdbc.StoredProcedure.inoutVarargs'"
        ),
        DROP_PROC_INOUT("DROP PROCEDURE IF EXISTS INOUT_VARARGS");

        private final String query;
        private final String[] args;

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
            return "jdbc:hsqldb:mem:testdb";
        }
    }
    
    enum TestSProc implements SProcAPI {
        SHOW_ADDRESSES("SHOW_ADDRESSES()"),
        IN_VARARGS("IN_VARARGS(<, >:)", Types.VARCHAR, Types.INTEGER),
        OUT_VARARGS("OUT_VARARGS(>, <:)", Types.INTEGER, Types.INTEGER),
        INOUT_VARARGS("INOUT_VARARGS(>, =:)", Types.INTEGER, Types.INTEGER);
        
        private final int[] argTypes;
        private final String signature;
        
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

}
