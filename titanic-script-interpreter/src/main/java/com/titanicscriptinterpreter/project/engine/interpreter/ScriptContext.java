package com.titanicscriptinterpreter.project.engine.interpreter;

import com.titanicscriptinterpreter.project.engine.statements.FunctionDeclarationStatement;
import java.util.HashMap;
import java.util.Map;

public class ScriptContext {
    private Map<String, FunctionDeclarationStatement> functionMap = new HashMap<>();
    private Map<String, Object> variables = new HashMap<>();
    private ScriptContext parent; // optional parent scope

    public void setParent(ScriptContext parentContext) {
        this.parent = parentContext;
    }

    public ScriptContext getParent() {
        return parent;
    }

    public void registerFunction(String name, FunctionDeclarationStatement funcDecl) {
        functionMap.put(name, funcDecl);
    }

    public FunctionDeclarationStatement getFunction(String name) {
        return functionMap.get(name);
    }

    public void setVariable(String varName, Object value) {
        variables.put(varName, value);
    }

   /**
     * Returns the variable value if found in this scope or any parent scope.
     */
    public Object getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.getVariable(name);
        }
        return null;  // not found
    }
    
    public boolean hasVariable(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        
        return false;
    }

    /**
     * Checks if the current local scope contains a variable.
     */
    public boolean hasVariableLocally(String name) {
        return variables.containsKey(name);
    }

    /**
     * Return the top context by going-up trough parents.
     * If there's no parent, this context is itself the root.
     */
    public ScriptContext getRootContext() {
        if (parent == null) {
            return this; 
        }
        // recursively climb up
        return parent.getRootContext();
    }
}

