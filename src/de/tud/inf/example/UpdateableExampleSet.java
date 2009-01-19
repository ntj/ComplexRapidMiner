package de.tud.inf.example;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

public interface UpdateableExampleSet extends ExampleSet {
	public void addExample(Example example);
}
