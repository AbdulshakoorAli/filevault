package com.filevault.functions.dto;

import java.util.List;

public class ExtractedTablePayload {
    private int rowCount;
    private int columnCount;
    private List<String> cells;

    public ExtractedTablePayload() {
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public List<String> getCells() {
        return cells;
    }

    public void setCells(List<String> cells) {
        this.cells = cells;
    }
}
