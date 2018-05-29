package com.nordstrom.common.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StoredProcedure {

    public static void showAddresses(ResultSet[] rs) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:default:connection");
        String query = "select NUM, ADDR from LOCATION";
        Statement stmt = con.createStatement();
        rs[0] = stmt.executeQuery(query);
    }

    //////////////////////////
    //
    // IN, OUT, IN/OUT PARAMETERS
    //
    //////////////////////////

    public static void inVarargs(String[] result, int... values) {
        String retval;
        if (values == null) {
            retval = null;
        } else if (values.length == 0) {
            retval = null;
        } else {
            StringBuilder buffer = new StringBuilder();

            buffer.append("RESULT: ");

            for (int value : values) {
                buffer.append(" " + Integer.toString(value));
            }

            retval = buffer.toString();
        }

        result[0] = retval;
    }

    public static void outVarargs(int seed, int[]... values) throws Exception {
        if (values == null) {
            return;
        } else {
            for (int i = 0; i < values.length; i++) {
                values[i][0] = seed + i;
            }
        }
    }

    public static void inoutVarargs(int seed, int[]... values) throws Exception {
        if (values == null) {
            return;
        } else {
            for (int i = 0; i < values.length; i++) {
                values[i][0] += seed;
            }
        }
    }
}
