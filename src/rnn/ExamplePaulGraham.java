package rnn;

import java.util.List;
import java.util.Random;

import rnn.model.Model;
import rnn.trainer.Trainer;
import rnn.util.NeuralNetworkHelper;
import rnn.datasets.TextGeneration;
import rnn.datastructs.DataSet;

public class ExamplePaulGraham {
//    private static long seed=4337;

    public static String go(String textSource) throws Exception {

		/*
		 * Character-by-character sentence prediction and generation, closely following the example here:
		 * http://cs.stanford.edu/people/karpathy/recurrentjs/
		*/

//		String textSource = "PaulGraham";
        DataSet data = new TextGeneration(textSource + ".txt");
        String savePath = textSource + ".ser";
        boolean initFromSaved = true; //set this to false to start with a fresh model
        boolean overwriteSaved = true;

        TextGeneration.reportSequenceLength = 75;

        int bottleneckSize = 20; //one-hot input is squeezed through this
        int hiddenDimension = 400;
        int hiddenLayers =2;
        double learningRate = 0.0001;
        double initParamsStdDev = 0.08;

        Random rng = new Random();
        Model lstm = NeuralNetworkHelper.makeLstmWithInputBottleneck(
                data.inputDimension, bottleneckSize,
                hiddenDimension, hiddenLayers,
                data.outputDimension, data.getModelOutputUnitToUse(),
                initParamsStdDev, rng);

        int reportEveryNthEpoch =5;
        int trainingEpochs = 5;

//		while(true) {
        Trainer.train(trainingEpochs, learningRate-=.00001d, lstm, data, reportEveryNthEpoch, initFromSaved, overwriteSaved, savePath, rng);
        List<String> predicted = TextGeneration.generateText(lstm,TextGeneration.reportSequenceLength,false, .5f, new Random());
        int cnt = 0;
        System.out.println("--------------------------------------------");
        for (String b : predicted) {
            System.out.println((cnt++) + "\t" + b);
            return b;
        }
        System.out.println("--------------------------------------------");

//		}

        return null;
    }
}
