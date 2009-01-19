package de.tud.inf.example.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AbstractExampleTable;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.ListDataRowReader;

public class UpdateableMemoryExampleTable extends AbstractExampleTable implements UpdateableExampleTable {
	private static final long serialVersionUID = 6185821259728827238L;
	
	/** List of {@link DataRow}s. */
	private List<DataRow> dataList = new LinkedList<DataRow>();
	
	public UpdateableMemoryExampleTable(List<Attribute> attributes) {
		super(attributes);
	}

	public UpdateableMemoryExampleTable(List<Attribute> attributes, DataRowReader i) {
		this(attributes, i, false);
	}
	
	public UpdateableMemoryExampleTable(List<Attribute> attributes, DataRowReader i, boolean permutate) {
		this(attributes);
		readExamples(i, permutate);
	}

	public UpdateableMemoryExampleTable(List<Attribute> attributes, DataRowFactory factory, int size) {
		this(attributes);
		dataList = new ArrayList<DataRow>(size);
		for (int i = 0; i < size; i++) {
			DataRow dataRow = factory.create(attributes.size());
			for (Attribute attribute : attributes) {
				dataRow.set(attribute, Double.NaN);
			}
			dataList.add(dataRow);
		}
	}
	
//	public UpdateableMemoryExampleTable(UpdateableMemoryExampleTable exampleTable) {
//		Attribute[] attribs = exampleTable.getAttributes();
//		List<Attribute> attribList = new LinkedList<Attribute>();
//		for (Attribute attribute : attribs) {
//			attribList.add(attribute);
//		}
//		addAttributes(attribList);
//		this.dataList = new LinkedList(exampleTable.dataList);
//	}

	public void readExamples(DataRowReader i) {
		readExamples(i, false);
	}

    public void readExamples(DataRowReader i, boolean permutate) {
        readExamples(i, false, null);
    }
    
	public void readExamples(DataRowReader i, boolean permutate, Random random) {
		dataList.clear();
		while (i.hasNext()) {
			if (permutate) {
				int index = random.nextInt(dataList.size() + 1);
				dataList.add(index, i.next());
			} else {
				dataList.add(i.next());
			}
		}
	}

	public DataRowReader getDataRowReader() {
		return new ListDataRowReader(dataList.iterator());
	}

	public DataRow getDataRow(int index) {
		return dataList.get(index);
	}

	public int size() {
		return dataList.size();
	}

	public void addDataRow(DataRow dataRow) {
		dataList.add(dataRow);
	}

	public boolean removeDataRow(DataRow dataRow) {
		return dataList.remove(dataRow);
	}
	
	public DataRow removeDataRow(int index) {
		return dataList.remove(index);
	}
	
	public void clear() {
		dataList.clear();
	}
	
	public synchronized int addAttribute(Attribute attribute) {
		return super.addAttribute(attribute);
	}
    
	public static UpdateableMemoryExampleTable createCompleteCopy(ExampleTable oldTable) {
		UpdateableMemoryExampleTable table = new UpdateableMemoryExampleTable(Arrays.asList(oldTable.getAttributes()));
        DataRowReader reader = oldTable.getDataRowReader();
        while (reader.hasNext()) {
            DataRow dataRow = reader.next();
            double[] newDataRowData = new double[oldTable.getNumberOfAttributes()];
            for (int a = 0; a < oldTable.getNumberOfAttributes(); a++) {
                Attribute attribute = oldTable.getAttribute(a);
                if (attribute != null) {
                    newDataRowData[a] = dataRow.get(attribute);
                } else {
                    newDataRowData[a] = Double.NaN;
                }
            }
            table.addDataRow(new DoubleArrayDataRow(newDataRowData));
        }
        return table;
    }
}
