package com.nordstrom.common.jdbc;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StoredProcedure {

    public static void showAddresses(ResultSet[] rs) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:default:connection");
        Statement stmt = con.createStatement();
        rs[0] = stmt.executeQuery("SELECT NUM, ADDR FROM LOCATION");
    }

    public static void inVarargs(String[] result, Array inputs) throws SQLException {
        String retval;
        if (inputs == null) {
            retval = null;
        } else {
            Object[] objects = (Object[]) inputs.getArray();
            if (objects.length == 0) {
                retval = null;
            } else {
                StringBuilder buffer = new StringBuilder("RESULT:");
                for (Object value : objects) {
                    buffer.append(" ").append(value);
                }
    
                retval = buffer.toString();
            }
        }
        
        result[0] = retval;
    }

    public static void inoutVarargs(Integer seed, Array[] values) throws SQLException {
        if (values != null && values.length > 0 && values[0] != null) {
            Array sqlArray = values[0];
            Object[] buffer = (Object[]) sqlArray.getArray();
            
            for (int i = 0; i < buffer.length; i++) {
                int currentValue = (buffer[i] == null) ? 0 : (Integer) buffer[i];
                buffer[i] = currentValue + seed;
            }
            
            try (Connection conn = DriverManager.getConnection("jdbc:default:connection")) {
                values[0] = conn.createArrayOf("INTEGER", buffer);
            }
        }
    }
}
