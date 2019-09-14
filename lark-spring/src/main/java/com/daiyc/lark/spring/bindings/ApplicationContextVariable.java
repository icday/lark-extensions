package com.daiyc.lark.spring.bindings;

import com.daiyc.lark.core.groovy.GroovyContextVariable;
import com.daiyc.lark.spring.SpringContextHolder;

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
        return SpringContextHolder.get();
    }
}