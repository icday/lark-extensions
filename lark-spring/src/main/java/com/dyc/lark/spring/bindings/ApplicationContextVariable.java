package com.dyc.lark.spring.bindings;

import com.dyc.lark.core.groovy.GroovyContextVariable;

/**
 * @author daiyc
 */
public class ApplicationContextVariable implements GroovyContextVariable {
    @Override
    public String getName() {
        return "ctx";
    }

    @Override
    public Object getValue() {
        return null;
    }
}
