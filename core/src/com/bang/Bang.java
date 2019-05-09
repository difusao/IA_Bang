package com.bang;

/*
// Adapted Sample
https://www.programcreek.com/java-api-examples/?code=fgulan/final-thesis/final-thesis-master/Project/src/main/java/hr/fer/zemris/test/croatian/OneToOneHVTest.java#
*/

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

import java.util.Arrays;

public class Bang extends ApplicationAdapter {

	@Override
	public void create() {
		// create training set (logical XOR function)
		DataSet trainingSet = new DataSet(2, 1);
		trainingSet.addRow(new DataSetRow(new double[]{0, 0}, new double[]{0}));
		trainingSet.addRow(new DataSetRow(new double[]{0, 1}, new double[]{1}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 0}, new double[]{1}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 1}, new double[]{0}));

		// create multi layer perceptron
		MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 2, 3, 1);
		myMlPerceptron.setLearningRule(new BackPropagation());
		// learn the training set
		myMlPerceptron.learn(trainingSet);
		// test perceptron
		System.out.println("Testing trained neural network");
		testNeuralNetwork(myMlPerceptron, trainingSet);

		// save trained neural network
		myMlPerceptron.save("myMlPerceptron.nnet");

		System.out.println();

		// load saved neural network
		NeuralNetwork loadedMlPerceptron = NeuralNetwork.createFromFile("myMlPerceptron.nnet");

		// test loaded neural network
		System.out.println("Testing loaded neural network");
		testNeuralNetwork(loadedMlPerceptron, trainingSet);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	public static void testNeuralNetwork(NeuralNetwork nnet, DataSet testSet) {

		for(DataSetRow dataRow : testSet.getRows()) {
			nnet.setInput(dataRow.getInput());
			nnet.calculate();
			double[ ] networkOutput = nnet.getOutput();
			System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
			System.out.println(" Output: " + Arrays.toString(networkOutput) );
		}

	}
}
