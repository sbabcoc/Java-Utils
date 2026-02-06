package com.nordstrom.common.jdbc;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to encapsulate parameters for stored procedure calls. In addition to parameter value, instances
 * of this class define {@link Mode parameter mode} (IN/OUT/INOUT) and {@link Types parameter type} (e.g. - INTEGER).
 */
public class Param {
    
    private Mode mode = Mode.IN;
    private int paramType;
    private int valueType;
    private String valueTypeName;
    private Class<?> valueClass;
    private Object inputValue;
    
    private static final Map<Integer, Class<?>> TYPE_MAP = new HashMap<>();
    
    static {
        TYPE_MAP.put(Types.CHAR, String.class);
        TYPE_MAP.put(Types.VARCHAR, String.class);
        TYPE_MAP.put(Types.LONGVARCHAR, String.class);
        TYPE_MAP.put(Types.NCHAR, String.class);
        TYPE_MAP.put(Types.NVARCHAR, String.class);
        TYPE_MAP.put(Types.LONGNVARCHAR, String.class);
        TYPE_MAP.put(Types.BINARY, byte[].class);
        TYPE_MAP.put(Types.VARBINARY, byte[].class);
        TYPE_MAP.put(Types.LONGVARBINARY, byte[].class);
        TYPE_MAP.put(Types.BIT, Boolean.class);
        TYPE_MAP.put(Types.BOOLEAN, Boolean.class);
        TYPE_MAP.put(Types.SMALLINT, Short.class);
        TYPE_MAP.put(Types.INTEGER, Integer.class);
        TYPE_MAP.put(Types.BIGINT, Long.class);
        TYPE_MAP.put(Types.REAL, Float.class);
        TYPE_MAP.put(Types.DOUBLE, Double.class);
        TYPE_MAP.put(Types.FLOAT, Double.class);
        TYPE_MAP.put(Types.DECIMAL, BigDecimal.class);
        TYPE_MAP.put(Types.NUMERIC, BigDecimal.class);
        TYPE_MAP.put(Types.DATE, java.sql.Date.class);
        TYPE_MAP.put(Types.TIME, java.sql.Time.class);
        TYPE_MAP.put(Types.TIMESTAMP, java.sql.Timestamp.class);
        TYPE_MAP.put(Types.OTHER, Object.class);
        TYPE_MAP.put(Types.JAVA_OBJECT, Object.class);
    }

    /**
     * Constructor: Private, to discourage direct instantiation.
     */
    private Param() {
    }
    
    /**
     * Instantiate a parameter of the indicated mode and type with the specified value.
     * 
     * @param mode parameter {@link Mode mode}
     * @param paramType parameter {@link Types type}
     * @param inputValue parameter value
     * @return new {@link Param} object
     */
    public static Param create(Mode mode, int paramType, Object inputValue) {
        if (mode == Mode.OUT) {
            return Param.out(paramType);
        } else if (mode == Mode.INOUT) {
            return Param.inOut(paramType, inputValue);
        }
        return Param.in(paramType, inputValue);
    }
    
    /**
     * Instantiate an array parameter of the indicated mode and type with the specified value.
     * 
     * @param mode parameter {@link Mode mode}
     * @param valueType {@link Types type} of each value
     * @param inputValues array of zero of more input values
     * @return new {@link Param} object
     */
    public static Param create(Mode mode, int valueType, Object... inputValues) {
        Param parameter = new Param();
        parameter.mode = mode;
        parameter.paramType = Types.ARRAY;
        parameter.valueType = valueType;
        parameter.inputValue = inputValues;
        return parameter;
    }
    
    /**
     * Set the class of items in <b>Array</b> parameters.
     * 
     * @param valueClass array item class
     * @return this {@link Param} object
     */
    public Param setValueClass(Class<?> valueClass) {
        this.valueClass = valueClass;
        return this;
    }
    
    /**
     * Set the custom type name of items in <b>Array</b> parameters.
     * 
     * @param valueTypeName array item type name
     * @return this {@link Param} object
     */
    public Param setValueTypeName(String valueTypeName) {
        this.valueTypeName = valueTypeName;
        return this;
    }
    
    /**
     * Instantiate an IN parameter of the indicated type with the specified value.
     * 
     * @param paramType parameter {@link Types type}
     * @param inputValue parameter value
     * @return new {@link Param} object
     */
    public static Param in(int paramType, Object inputValue) {
        Param parameter = new Param();
        parameter.mode = Mode.IN;
        parameter.paramType = paramType;
        parameter.inputValue = inputValue;
        return parameter;
    }
    
    /**
     * Instantiate an OUT parameter of the indicated type.
     * 
     * @param paramType parameter {@link Types type}
     * @return new {@link Param} object
     */
    public static Param out(int paramType) {
        Param parameter = new Param();
        parameter.mode = Mode.OUT;
        parameter.paramType = paramType;
        return parameter;
    }
    
    /**
     * Instantiate an INOUT parameter of the indicated type with the specified value.
     * 
     * @param paramType parameter {@link Types type}
     * @param inputValue parameter value
     * @return new {@link Param} object
     */
    public static Param inOut(int paramType, Object inputValue) {
        Param parameter = new Param();
        parameter.mode = Mode.INOUT;
        parameter.inputValue = inputValue;
        parameter.paramType = paramType;
        return parameter;
    }
    
    /**
     * Allocate an array with the specified capacity of {@link Param} objects.
     * 
     * @param size desired array capacity
     * @return {@link Param} array of specified capacity
     */
    static Param[] array(int size) {
        return new Param[size];
    }
    
    /**
     * Store this parameter at the indicated index for the specified callable statement.
     *  
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    public void set(CallableStatement sproc, int index) throws SQLException {
        if (isOutput()) {
            if (paramType == java.sql.Types.ARRAY && valueTypeName != null) {
                sproc.registerOutParameter(index, paramType, valueTypeName);
            } else {
                sproc.registerOutParameter(index, paramType);
            }
        }
        
        if (isInput()) {
            if (inputValue == null) {
                sproc.setNull(index, paramType);
            } else {
                switch (paramType) {

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

                case Types.ARRAY:
                    setSqlArray(sproc, index);
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Specified parameter type [" + paramType + "] is unsupported");
                }
            }
        }
    }
    
    /**
     * Store this character string parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setCharString(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof String) {
            sproc.setString(index, (String) inputValue); 
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a string");
        }
    }
    
    /**
     * Store this character string parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setNCharString(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof String) {
            sproc.setNString(index, (String) inputValue); 
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a string");
        }
    }
    
    /**
     * Store this binary parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setBinary(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof byte[]) {
            sproc.setBytes(index, (byte[]) inputValue); 
        } else {
            throw new IllegalArgumentException("Specified parameter value is not an array of bytes");
        }
    }
    
    /**
     * Store this boolean parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setBoolean(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Boolean) {
            sproc.setBoolean(index, (Boolean) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a boolean");
        }
    }
    
    /**
     * Store this small integer parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setSmallInt(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Short) {
            sproc.setShort(index, (Short) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a small integer (short)");
        }
    }
    
    /**
     * Store this integer parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setInteger(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Integer) {
            sproc.setInt(index, (Integer) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not an integer");
        }
    }
    
    /**
     * Store this big integer parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setBigInt(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Long) {
            sproc.setLong(index, (Long) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a big integer (long)");
        }
    }
    
    /**
     * Store this single-precision floating-point parameter at the indicated index for the specified callable
     * statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setReal(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Float) {
            sproc.setFloat(index, (Float) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a single-precision float (float)");
        }
    }
    
    /**
     * Store this double-precision floating-point parameter at the indicated index for the specified callable
     * statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setDouble(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof Double) {
            sproc.setDouble(index, (Double) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a double-precision float (double)");
        }
    }
    
    /**
     * Store this decimal parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setDecimal(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof BigDecimal) {
            sproc.setBigDecimal(index, (BigDecimal) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a decimal (BigDecimal)");
        }
    }
    
    /**
     * Store this SQL Date parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setDate(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Date) {
            sproc.setDate(index, (java.sql.Date) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a SQL Date object");
        }
    }
    
    /**
     * Store this SQL Time parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setTime(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Time) {
            sproc.setTime(index, (java.sql.Time) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a SQL Time object");
        }
    }
    
    /**
     * Store this SQL Timestamp parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setTimestamp(CallableStatement sproc, int index) throws SQLException {
        if (inputValue instanceof java.sql.Timestamp) {
            sproc.setTimestamp(index, (java.sql.Timestamp) inputValue);
        } else {
            throw new IllegalArgumentException("Specified parameter value is not a SQL Timestamp object");
        }
    }
    
    /**
     * Store this Java object parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setJavaObject(CallableStatement sproc, int index) throws SQLException {
        sproc.setObject(index, inputValue);
    }
    
    /**
     * Store this Java array parameter at the indicated index for the specified callable statement.
     * 
     * @param sproc target {@link CallableStatement} object
     * @param index parameter index (1-based)
     * @throws SQLException if the specified index is not valid; if a database access error occurs or this method is
     *         called on a closed {@link CallableStatement}
     */
    private void setSqlArray(CallableStatement sproc, int index) throws SQLException {
        String sqlTypeName = Optional.ofNullable(valueTypeName)
                .orElseGet(() -> JDBCType.valueOf(valueType).getName());

        int length = java.lang.reflect.Array.getLength(inputValue);
        Class<?> valueClass = getValueClass();
        
        Object typedArray = java.lang.reflect.Array.newInstance(valueClass, length);

        for (int i = 0; i < length; i++) {
            Object item = java.lang.reflect.Array.get(inputValue, i);
            
            if (item != null && !valueClass.isInstance(item)) {
                throw new IllegalArgumentException(String.format(
                    "Array element mismatch at index %d: expected %s, found %s",
                    i, valueClass.getName(), item.getClass().getName()));
            }
            java.lang.reflect.Array.set(typedArray, i, item);
        }

        java.sql.Array sqlArray = sproc.getConnection().createArrayOf(sqlTypeName, (Object[]) typedArray);
        sproc.setArray(index, sqlArray);
    }
    
    /**
     * Determines the appropriate Java {@link Class} type for the current value based on its 
     * JDBC {@link java.sql.Types}.
     * <p>
     * If an explicit {@code valueClass} has already been provided, it is returned immediately. 
     * Otherwise, this method performs a lookup based on {@code valueType} to find the most 
     * compatible Java wrapper or data type (e.g., mapping {@code Types.VARCHAR} to {@code String.class}).
     * </p>
     * 
     * @return the {@link Class} representing the Java type mapping for this value.
     * @throws UnsupportedOperationException if the provided {@code paramType} does not have 
     * a defined mapping in this implementation.
     */
    private Class<?> getValueClass() {
        if (valueClass != null) return valueClass;

        return Optional.ofNullable(TYPE_MAP.get(valueType))
                .orElseThrow(() -> new UnsupportedOperationException(
                    "Specified parameter type [" + valueType + "] is unsupported"));
    }    
    
    /**
     * Get the {@link Mode} of this parameter (IN/OUT/INOUT)
     * 
     * @return parameter mode
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     * Determine if this parameter is an input.
     * 
     * @return {@code true} if this parameter is an input; otherwise {@code false}
     */
    public boolean isInput() {
        return mode.isInput();
    }
    
    /**
     * Determine if this parameter is an output.
     * 
     * @return {@code true} if this parameter is an output; otherwise {@code false}
     */
    public boolean isOutput() {
        return mode.isOutput();
    }
    
    /**
     * Get the input value of this parameter.
     * 
     * @return parameter input value
     */
    public Object getInValue() {
        return inputValue;
    }
    
    /**
     * Get the value {@link Types type} of this parameter.
     * 
     * @return parameter {@link Types type}
     */
    public int getParamType() {
        return paramType;
    }
    
    /**
     * This enumeration defines the stored procedure parameter modes with their associated placeholder characters.
     */
    public enum Mode {
        /** placeholder: '&gt;' */
        IN('>', Mode.INPUT),
        /** placeholder: '&lt;' */
        OUT('<', Mode.OUTPUT),
        /** placeholder: '=' */
        INOUT('=', Mode.INPUT | Mode.OUTPUT);
        
        private static final int INPUT = 1;
        private static final int OUTPUT = 2;
        
        private final char chr;
        private final int val;
        
        /**
         * Constructor
         * 
         * @param chr placeholder character
         * @param val directionality flags
         */
        Mode(char chr, int val) {
            this.chr = chr;
            this.val = val;
        }
        
        /**
         * Get the placeholder character for this parameter mode.
         * 
         * @return parameter mode placeholder
         */
        public char chr() {
            return chr;
        }
        
        /**
         * Get the directionality flags for this parameter mode.
         * 
         * @return parameter mode directionality
         */
        public int val() {
            return val;
        }
        
        /**
         * Determine if this mode represents an input parameter.
         * 
         * @return {@code true} if this is an input mode; otherwise {@code false}
         */
        public boolean isInput() {
            return ((val & INPUT) == INPUT);
        }
        
        /**
         * Determine if this mode represents an output parameter.
         * 
         * @return {@code true} if this is an output mode; otherwise {@code false}
         */
        public boolean isOutput() {
            return ((val & OUTPUT) == OUTPUT);
        }
        
        /**
         * Get the parameter mode constant that corresponds to the specified placeholder character.
         * 
         * @param chr parameter mode placeholder
         * @return parameter mode constant
         */
        public static Mode fromChar(char chr) {
            for (Mode thisMode : values()) {
                if (thisMode.chr == chr) {
                    return thisMode;
                }
            }
            throw new IllegalArgumentException("Specified parameter mode placeholder '" + chr + "' is unsupported");
        }
    }
}
