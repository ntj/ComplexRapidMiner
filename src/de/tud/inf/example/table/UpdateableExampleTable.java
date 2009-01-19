package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;

public interface UpdateableExampleTable extends ExampleTable {
	/**
	 * Appends a data row to the example table.
	 */
	public void addDataRow(DataRow dataRow);
	
	/**
	 * Clear the example table.
	 */
	public void clear();
}
