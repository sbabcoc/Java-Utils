package com.nordstrom.common.params;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This interface enables implementers to provide methods to support for concisely-defined parameters.
 */
public interface Params {
    
    /**
     * Get the defined parameters.
     * 
     * @return optional map of named parameters
     */
    default Optional<Map<String, Object>> getParameters() {
        return Optional.empty();
    }
    
    /**
     * Assemble a map of parameters.
     * 
     * @param params array of {@link Param} objects; may be {@code null} or empty
     * @return optional map of parameters (may be empty)
     */
    public static Optional<Map<String, Object>> mapOf(Param... params) {
        if ((params == null) || (params.length == 0)) {
            return Optional.empty();
        }
        Map<String, Object> paramMap = new HashMap<>();
        for (Param param : params) {
            paramMap.put(param.key, param.val);
        }
        return Optional.of(Collections.unmodifiableMap(paramMap));
    }
    
    /**
     * Create a parameter object for the specified key/value pair.
     * 
     * @param key parameter key (name)
     * @param val parameter value
     * @return parameter object
     */
    public static Param param(String key, Object val) {
        return new Param(key, val);
    }
    
    /**
     * This class defines a parameter object.
     */
    static class Param {
        
        private final String key;
        private final Object val;
        
        /**
         * Constructor for parameter object.
         * 
         * @param key parameter key
         * @param val parameter value
         */
        public Param(String key, Object val) {
            if ((key == null) || key.isEmpty()) {
                throw new IllegalArgumentException("[key] must be a non-empty string");
            }
            this.key = key;
            this.val = val;
        }
        
        /**
         * Get key of this parameter.
         * 
         * @return parameter key
         */
        public String getKey() {
            return key;
        }
        
        /**
         * Get value of this parameter.
         * 
         * @return parameter value
         */
        public Object getVal() {
            return val;
        }
    }
}
