package com.nn;

import java.io.File;
import java.util.Locale;
import java.util.Random;

public class NetworkUtils {
    public void NetworkUtils(){

    }

    public float RamdomValues(float  min, float  max){
        Random b = new Random();
        return min + (max - min) * b.nextFloat();
    }

    public int RamdomValuesInt(int  min, int  max){
        Random b = new Random();
        return min + (max - min) * b.nextInt();
    }

    public double RamdomValuesDouble(double  min, double  max){
        Random b = new Random();
        return min + (max - min) * b.nextDouble();
    }

    public double Sigmoid(double sum){
        return 1 / (1 + Math.exp(-sum));
    }

    public double Sum(double[] inputs, double[] weight){
        double sum = 0;

        if (weight.length == 0){
            weight = new double[inputs.length];
            for (int i = 0; i < inputs.length; i++)
                weight[i] = RamdomValues(0.1f, 10.0f);
        }

        for(int i=0; i<inputs.length; i++)
            sum = sum + inputs[i] * weight[i];

        System.out.print("Weigths: ");

        for(int i=0; i<weight.length; i++)
            System.out.printf(Locale.US, " %012.8f", weight[i]);

        System.out.println();

        return sum;
    }

    public boolean DeleteFileNN(String filename){
        File file = new File(filename);
        boolean ret = false;

        if(file.delete())
            ret = true;

        return ret;
    }
}

