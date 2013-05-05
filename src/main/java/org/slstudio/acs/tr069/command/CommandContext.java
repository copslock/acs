package org.slstudio.acs.tr069.command;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-5-4
 * Time: ����2:09
 */
public class CommandContext {
    private Map<String, Object> symbolTable = null;

    public CommandContext(Map<String, Object> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public Map<String, Object> getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(Map<String, Object> symbolTable) {
        this.symbolTable = symbolTable;
    }
}
