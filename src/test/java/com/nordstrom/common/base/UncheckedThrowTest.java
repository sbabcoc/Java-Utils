package com.nordstrom.common.base;

import java.io.IOException;

import org.testng.annotations.Test;

public class UncheckedThrowTest {
    
    @Test(expectedExceptions = {IOException.class})
    public void testCheckedException() {
        try {
            throwCheckedException();
        } catch (Throwable t) {
            throw UncheckedThrow.throwUnchecked(t);
        }
    }
    
    @Test(expectedExceptions = {AssertionError.class})
    public void testUncheckedException() {
        try {
            throwUncheckedException();
        } catch (Throwable t) {
            throw UncheckedThrow.throwUnchecked(t);
        }
    }
    
    private void throwCheckedException() throws IOException {
        throw new IOException("This is a checked exception");
    }
    
    private void throwUncheckedException() {
        throw new AssertionError("This is an unchecked exception");
    }

}
