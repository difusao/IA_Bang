package com.Tests;

/*
// Adapted Sample
https://www.programcreek.com/java-api-examples/?code=fgulan/final-thesis/final-thesis-master/Project/src/main/java/hr/fer/zemris/test/croatian/OneToOneHVTest.java#
*/

import com.badlogic.gdx.Gdx;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

import java.util.Arrays;
import java.util.Locale;

public class TestNN {

    // Time Elapsed
    private long start = 0;
    private long finish = 0;
    private float timeElapsed = 0;

    public void StartTestNN(){
        start = System.currentTimeMillis();

        TestNeuralNetworkPML_XOR();

        finish = System.currentTimeMillis();
        timeElapsed = ((finish - start));
        System.out.printf(Locale.US, "\nTime Elapsed: %03.2f (%f)%n", (timeElapsed / 1000), timeElapsed );
    }

    private void TestNeuralNetworkPML_XOR(){
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

        // save trained neural network
        if(Gdx.graphics.getWidth() > (2246/2))
            // Android
            myMlPerceptron.save("/data/data/com.bang/files/myMlPerceptron.nnet");
        else
            // Desktop
            myMlPerceptron.save("myMlPerceptron.nnet");

        System.out.println();

        // load saved neural network
        NeuralNetwork loadedMlPerceptron;
        if(Gdx.graphics.getWidth() > (2246/2))
            // Android
            loadedMlPerceptron = NeuralNetwork.createFromFile("/data/data/com.bang/files/myMlPerceptron.nnet");
        else
            // Desktop
            loadedMlPerceptron = NeuralNetwork.createFromFile("myMlPerceptron.nnet");

        // test loaded neural network
        System.out.println("Testing loaded neural network");
        testNeuralNetwork(loadedMlPerceptron, trainingSet);
    }

    private static void testNeuralNetwork(NeuralNetwork nnet, DataSet testSet) {
        int i = 0;
        for(DataSetRow dataRow : testSet.getRows()) {
            nnet.setInput(dataRow.getInput());
            nnet.calculate();
            double[ ] networkOutput = nnet.getOutput();
            System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
            System.out.print(" Desired: " + Arrays.toString(testSet.getRows().get(i).getDesiredOutput()) );
            System.out.println(" Output: " + Arrays.toString(networkOutput) );
            i++;
        }
    }
}
