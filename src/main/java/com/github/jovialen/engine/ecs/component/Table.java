package com.github.jovialen.engine.ecs.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
    private final List<List<Object>> table;
    private final Map<Integer, Integer> tableRows = new HashMap<>();
    private final Map<Integer, Integer> pseudoRows = new HashMap<>();
    private int nextRow = 0;

    public Table(int columns) {
        this(columns, 1);
    }

    public Table(int columns, int initialRows) {
        table = new ArrayList<>(columns);
        for (int i = 0; i < columns; i++) {
            table.add(new ArrayList<>(initialRows));
        }
    }

    public Object get(int column, int row) {
        return table.get(column).get(tableRows.get(row));
    }

    public void set(int column, int row, Object entry) {
        table.get(column).set(tableRows.get(row), entry);
    }

    public int newRow() {
        // Check if the table has any columns
        if (table.isEmpty()) {
            // ...If not, return the dummy row
            return -1;
        }

        // Create rows
        int pseudoRow = nextRow++;
        int tableRow = table.getFirst().size();

        // Associate rows
        tableRows.put(pseudoRow, tableRow);
        pseudoRows.put(tableRow, pseudoRow);

        // Allocate row in table
        for (List<Object> column : table) {
            column.add(null);
        }

        // Return row
        return pseudoRow;
    }

    public void removeRow(int row) {
        // Check if this is a dummy row
        if (row == -1) {
            return;
        }

        // Check if the row is in the table
        if (!tableRows.containsKey(row)) {
            throw new IndexOutOfBoundsException(row + " not in table");
        }

        // Check if the row is the only row in the table
        if (tableRows.size() == 1) {
            // ...If so, just clear the table
            nextRow = 0;
            pseudoRows.clear();
            tableRows.clear();
            table.forEach(List::clear);
            return;
        }

        // Get the actual row in the table
        int tableRow = tableRows.get(row);

        // Get the last row in the table
        int lastRow = pseudoRows.get(table.getFirst().size() - 1);

        // Check if the row is the last row in the table
        if (row == lastRow) {
            // ...If so, just remove the last entry. Nothing fancy
            tableRows.remove(row);
            pseudoRows.remove(tableRow);
            table.forEach(List::removeLast);
            return;
        }

        // If neither the only nor last entry, switch it with the last.

        // For each component in the table
        for (List<Object> columns : table) {
            // Move the last row to the row we are removing
            Object component = columns.getLast();
            columns.set(tableRow, component);

            // And remove the last row
            columns.removeLast();
        }

        // And formally give the removed row to the last row
        tableRows.remove(row);
        tableRows.put(lastRow, tableRow);
        pseudoRows.put(tableRow, lastRow);
    }

    @Override
    public String toString() {
        if (table.isEmpty() || table.getFirst().isEmpty()) {
            return "Table{empty}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Table{\n\t");

        sb.append("columns=[");
        for (int column = 0; column < table.size(); column++) {
            if (column != 0) {
                sb.append(", ");
            }

            sb.append(table.get(column).getFirst().getClass());
        }
        sb.append("],\n\trows=[\n\t\t");

        for (int row = 0; row < table.getFirst().size(); row++) {
            sb.append(row).append("=[");
            for (int column = 0; column < table.size(); column++) {
                if (column != 0) {
                    sb.append(", ");
                }

                sb.append(table.get(column).get(row).toString());
            }
            sb.append("],\n\t\t");
        }

        sb.append("],\n}");
        return sb.toString();
    }
}
