package de.tud.inf.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AbstractExampleReader;

public class ReverseSortedExampleReader extends AbstractExampleReader {
	private ExampleSet parent;
    private int currentIndex;
    
    public ReverseSortedExampleReader(ExampleSet parent) {
        this.parent = parent;
        this.currentIndex = this.parent.size() - 1;
    }
    
    public boolean hasNext() {
    	if(this.currentIndex >= 0) {
        	return true;
        } else {
        	return false;
        }
    }

    public Example next() {
    	Example example = this.parent.getExample(this.currentIndex);
    	this.currentIndex--;
        return example;
    }
}


