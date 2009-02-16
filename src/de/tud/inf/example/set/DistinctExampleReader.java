package de.tud.inf.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AbstractExampleReader;

public class DistinctExampleReader extends AbstractExampleReader{
	
	private ExampleSet parent;
	
	private int currentIndex;
	
	public DistinctExampleReader(ExampleSet parent) {
		
		this.parent = parent;
		currentIndex = 0;
	}

	public boolean hasNext() {
		
		return currentIndex < parent.size();
	}

	public Example next() {
		
		Example currentExample = parent.getExample(currentIndex);
		currentIndex++;
		return currentExample;
	}
}
