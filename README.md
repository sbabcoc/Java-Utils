# NORDSTROM COMMON

**Nordstrom Common** is a small collection of general-purpose utility classes with wide applicability.

## UncheckedThrow

The **UncheckedThrow** class uses type erasure to enable client code to throw checked exceptions as unchecked. This allows methods to throw checked exceptions without requiring clients to handle or declare them. It should be sparingly, as this exempts client code from handling or declaring exceptions created by their own actions. The target use case for this facility is to throw exceptions that were serialized in responses from a remote system. Although the compiler won't require clients of methods using this technique to handle or declare the suppressed exception, the JavaDoc for such methods should include a `@throws` declaration for implementers who might want to handle or declare it voluntarily.


```java
    ...
    
    String value;
    try {
        value = URLDecoder.decode(keyVal[1], "UTF-8");
    } catch (UnsupportedEncodingException e) {
        UncheckedThrow.throwUnchecked(e);
    }
    
    ...
```

## DatabaseUtils

