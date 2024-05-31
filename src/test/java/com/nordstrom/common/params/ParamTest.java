package com.nordstrom.common.params;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.testng.annotations.Test;

public class ParamTest implements Params {
    
    @Test
    public void testParam() {
        Param param = Param.param("boolean", true);
        assertEquals(param.getKey(), "boolean");
        verifyBoolean(param.getVal());
        
        param = Param.param("int", 1);
        assertEquals(param.getKey(), "int");
        verifyInt(param.getVal());
        
        param = Param.param("String", "one");
        assertEquals(param.getKey(), "String");
        verifyString(param.getVal());
        
        param = Param.param("Map", Param.mapOf(Param.param("key", "value")));
        assertEquals(param.getKey(), "Map");
        verifyMap(param.getVal());
    }
    
    @Test
    public void testParams() {
        Optional<Map<String, Object>> optParameters = getParameters();
        assertTrue(optParameters.isPresent());
        Map<String, Object> parameters = optParameters.get();
        assertFalse(parameters.isEmpty());
        
        assertTrue(parameters.containsKey("boolean"));
        verifyBoolean(parameters.get("boolean"));
        
        assertTrue(parameters.containsKey("int"));
        verifyInt(parameters.get("int"));
        
        assertTrue(parameters.containsKey("String"));
        verifyString(parameters.get("String"));
        
        assertTrue(parameters.containsKey("Map"));
        verifyMap(parameters.get("Map"));
    }
    
    private void verifyBoolean(Object value) {
        assertTrue(value instanceof Boolean);
        assertTrue((Boolean) value);
    }
    
    private void verifyInt(Object value) {
        assertTrue(value instanceof Integer);
        assertEquals(value, 1);
    }
    
    private void verifyString(Object value) {
        assertTrue(value instanceof String);
        assertEquals(value, "one");
    }
    
    public void verifyMap(Object value) {
        assertTrue(value instanceof Optional);
        Optional<?> optObj = (Optional<?>) value;
        assertTrue(optObj.isPresent());
        Object obj = optObj.get();
        assertTrue(obj instanceof Map);
        Map<?, ?> map = (Map<?, ?>) obj;
        assertTrue(map.containsKey("key"));
        assertEquals(map.get("key"), "value");
    }
    
    @Override
    public Optional<Map<String, Object>> getParameters() {
		return Param.mapOf(Param.param("boolean", true), Param.param("int", 1), Param.param("String", "one"),
				Param.param("Map", Param.mapOf(Param.param("key", "value"))));
    }
}
