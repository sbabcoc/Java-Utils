package com.nordstrom.common.jdbc;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class Param {
    
    private Mode mode = Mode.IN;
    private Object inputValue;
    private int valueType;
    
    private Param() {
    }
    
    public static Param in(Object inputValue, int valueType) {
        Param parameter = new Param();
        parameter.mode = Mode.IN;
        parameter.inputValue = inputValue;
        parameter.valueType = valueType;
        return parameter;
    }
    
    public static Param out(int valueType) {
        Param parameter = new Param();
        parameter.mode = Mode.OUT;
        parameter.valueType = valueType;
        return parameter;
    }
    
    public static Param inOut(Object inputValue, int valueType) {
        Param parameter = new Param();
        parameter.mode = Mode.INOUT;
        parameter.inputValue = inputValue;
        parameter.valueType = valueType;
        return parameter;
    }
    
    public static Param[] array(int size) {
        return new Param[size];
    }
    
    public void set(CallableStatement sproc, int index) throws SQLException {
        if (isOutput()) {
            sproc.registerOutParameter(index, valueType);
        }
        
        if (isInput()) {
            switch (valueType) {

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    setCharString(sproc, index);
                    break;

                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                    setNCharString(sproc, index);
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    setBinary(sproc, index);
                    break;

                case Types.BIT:
                case Types.BOOLEAN:
                    setBoolean(sproc, index);
                    break;

                case Types.SMALLINT:
                    setSmallInt(sproc, index);
                    break;

                case Types.INTEGER:
                    setInteger(sproc, index);
                    break;

                case Types.BIGINT:
                    setBigInt(sproc, index);
                    break;

                case Types.REAL:
                    setReal(sproc, index);
                    break;

                case Types.DOUBLE:
                case Types.FLOAT:
                    setDouble(sproc, index);
                    break;

                case Types.DECIMAL:
                case Types.NUMERIC:
                    setDecimal(sproc, index);
                    break;

                case Types.DATE:
                    setDate(sproc, index);
                    break;

                case Types.TIME:
                    setTime(sproc, index);
                    break;

                case Types.TIMESTAMP:
                    setTimestamp(sproc, index);
                    break;

                case Types.OTHER:
                case Types.JAVA_OBJECT:
                    setJavaObject(sproc, index);
                    break;

                default:
                    break;
            }
        }
    }
    
    private void setCharString(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof String) {
            sproc.setString(index, (String) inputValue); 
        } else {
            
        }
    }
    
    private void setNCharString(CallableStatement sproc, int index) throws SQLException {
        if (inputValue == null) {
            sproc.setNull(index, valueType);
        } else if (inputValue instanceof String) {
            sproc.setNString(index, (String) inputValue); 
        } else {
            
        }
    }
    
    private void setBinary(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof byte[]) {
            sproc.setBytes(index, (byte[]) inputValue); 
        } else {
            
        }
    }
    
    private void setBoolean(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Boolean) {
            sproc.setBoolean(index, (Boolean) inputValue);
        } else {
            
        }
    }
    
    private void setSmallInt(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Short) {
            sproc.setShort(index, (Short) inputValue);
        } else {
            
        }
    }
    
    private void setInteger(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Integer) {
            sproc.setInt(index, (Integer) inputValue);
        } else {
            
        }
    }
    
    private void setBigInt(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Long) {
            sproc.setLong(index, (Long) inputValue);
        } else {
            
        }
    }
    
    private void setReal(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Float) {
            sproc.setFloat(index, (Float) inputValue);
        } else {
            
        }
    }
    
    private void setDouble(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Double) {
            sproc.setDouble(index, (Double) inputValue);
        } else {
            
        }
    }
    
    private void setDecimal(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof BigDecimal) {
            sproc.setBigDecimal(index, (BigDecimal) inputValue);
        } else {
            
        }
    }
    
    private void setDate(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Date) {
            sproc.setDate(index, (java.sql.Date) inputValue);
        } else {
            
        }
    }
    
    private void setTime(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Time) {
            sproc.setTime(index, (java.sql.Time) inputValue);
        } else {
            
        }
    }
    
    private void setTimestamp(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Timestamp) {
            sproc.setTimestamp(index, (java.sql.Timestamp) inputValue);
        } else {
            
        }
    }
    
    private void setJavaObject(CallableStatement sproc, int index) throws SQLException {
        if (inputValue == null) {
            sproc.setNull(index, valueType);
        } else {
            sproc.setObject(index, inputValue);
        }
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public boolean isInput() {
        return mode.isInput();
    }
    
    public boolean isOutput() {
        return mode.isOutput();
    }

    public Object getInValue() {
        return inputValue;
    }

    public int getOutType() {
        return valueType;
    }
    
    public enum Mode {
        IN('>', Mode.INPUT),
        OUT('<', Mode.OUTPUT),
        INOUT('=', Mode.INPUT | Mode.OUTPUT);
        
        private static final int INPUT = 1;
        private static final int OUTPUT = 2;
        
        private char chr;
        private int val;
        
        Mode(char chr, int val) {
            this.chr = chr;
            this.val = val;
        }
        
        public char chr() {
            return chr;
        }
        
        public int val() {
            return val;
        }
        
        public boolean isInput() {
            return ((val & INPUT) == INPUT);
        }
        
        public boolean isOutput() {
            return ((val & OUTPUT) == OUTPUT);
        }
        
        public static Mode fromChar(char chr) {
            for (Mode thisMode : values()) {
                if (thisMode.chr == chr) {
                    return thisMode;
                }
            }
            throw new IllegalArgumentException("Specified parameter mode character '" + chr + "' is unsupported");
        }
    }
}
