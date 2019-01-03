package com.klemstinegroup.wub.ai.vectorrnn;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.ai.custom.Levenshtein;
import com.klemstinegroup.wub.system.Audio;
import com.klemstinegroup.wub.system.AudioInterval;
import com.klemstinegroup.wub.system.ObjectManager;
import com.klemstinegroup.wub.system.Song;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * GravesLSTM Character modelling example
 *
 * @author Alex Black
 * <p>
 * Example: Train a LSTM RNNDemo to generates text, one character at a time.
 * This example is somewhat inspired by Andrej Karpathy's blog post,
 * "The Unreasonable Effectiveness of Recurrent Neural Networks"
 * http://karpathy.github.io/2015/05/21/rnn-effectiveness/
 * <p>
 * This example is set up to train on the Complete Works of William Shakespeare, downloaded
 * from Project Gutenberg. Training on other text sources should be relatively easy to implement.
 * <p>
 * For more details on RNNs in DL4J, see the following:
 * http://deeplearning4j.org/usingrnns
 * http://deeplearning4j.org/lstm
 * http://deeplearning4j.org/recurrentnetwork
 */
public class RNNDemo {

    private static String generationInitialization = "";
    private static boolean bbtest;
    static File locationToSave = new File("MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
    private static int lastQueueSize;


//    public static void main(String[] args) {
//        String fileLocation = "aabbccdd";
//        String fileLocation1 = "";
//        for (int i = 0; i < 100; i++) fileLocation1 += fileLocation;
////        process(language, audio, fileLocation1);
//
//    }

    public static String[] process(Song song, HashMap<AudioInterval, Character> language, HashMap<AudioInterval, Character> origlanguage,Audio audio, String input) {
        generationInitialization = input.substring(input.length() / 2 - 20, input.length() / 2 + 20);
        HashMap<Character, AudioInterval> languageRev = new HashMap<>();
        for (Map.Entry<AudioInterval, Character> entry : language.entrySet())
            languageRev.put(entry.getValue(), entry.getKey());
        boolean smoothing = true;
        final double[] bestScore = {Double.MAX_VALUE};
        double startPlayingScore = 10040d;
        int miniBatchSize = 1000;                        //Size of mini batch to use when  training
        int exampleLength = 800;                    //Length of each training example sequence to use. This could certainly be increased
        int numEpochs = 1000000;                            //Total number of training epochs
        int generateSamplesEveryNMinibatches = 1;  //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        int nSamplesToGenerate = 1;                    //Number of samples to generate after each training epoch
        final int[] nCharactersToSample = {40};                //Length of each sample to generate

        // Above is Used to 'prime' the LSTM with a character sequence to continue/complete.
        // Initialization characters must all be in CharacterIteratorRNN.getMinimalCharacterSet() by default
        Random rng = new Random();

        //Get a DataSetIterator that handles vectorization of text into something we can use to train
        // our GravesLSTM network.
        CharacterIteratorRNNDemo iter = null;
        try {
            iter = getShakespeareIterator(input, miniBatchSize, exampleLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int nOut = iter.totalOutcomes();

/*        //Set up network configuration:
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .learningRate(0.1)
                .rmsDecay(0.95)
                .seed(12345)
                .regularization(true)
                .l2(0.001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.SIGMOID).build())
                .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(mstmLayerSize)
                        .activation(Activation.SIGMOID).build())
                .layer(2, new GravesLSTM.Builder().nIn(mstmLayerSize).nOut(nstmLayerSize)
                        .activation(Activation.SIGMOID).build())
                .layer(3, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(nstmLayerSize).nOut(nOut).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .pretrain(false).backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();*/

        int HIDDEN_LAYER_WIDTH = 800;
        int HIDDEN_LAYER_CONT = 2;
        // some common parameters
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        builder.seed(123);
        builder.biasInit(0);
        builder.miniBatch(true);
        builder.updater(new RmsProp(0.1));
        builder.weightInit(WeightInit.XAVIER);

        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

        // first difference, for rnns we need to use LSTM.Builder
        for (int i = 0; i < HIDDEN_LAYER_CONT; i++) {
            LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
            hiddenLayerBuilder.nIn(i == 0 ? iter.inputColumns() : HIDDEN_LAYER_WIDTH);
            hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);
            // adopted activation function from LSTMCharModellingExample
            // seems to work well with RNNs
            hiddenLayerBuilder.activation(Activation.TANH);
            listBuilder.layer(i, hiddenLayerBuilder.build());
        }

        // we need to use RnnOutputLayer for our RNN
        RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MCXENT);
        // softmax normalizes the output neurons, the sum of all outputs is 1
        // this is required for our sampleFromDistribution-function
        outputLayerBuilder.activation(Activation.SOFTMAX);
        outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
        outputLayerBuilder.nOut(iter.inputColumns());
        listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build());

        // finish builder
        listBuilder.pretrain(false);
        listBuilder.backprop(true);

        // create network
        MultiLayerConfiguration conf = listBuilder.build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        //Print the  number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);

        //Do training, and then generate and print samples from network

        CharacterIteratorRNNDemo finalIter = iter;
        new Thread(new Runnable() {

            boolean fitting = true;

            @Override
            public void run() {
                int miniBatchNumber = 0;
                for (int i = 0; i < numEpochs; i++) {
                    while (finalIter.hasNext()) {
                        DataSet ds = finalIter.next();
                        if (fitting) net.fit(ds);
                        if (net.score() < bestScore[0]) {
                            bestScore[0] = net.score();
                            System.out.println("!!!!!!!!!!!!!!!!!!!   saving!");

                            if (net.score() < 1d) fitting = false;
                            saveNet(net);
                            saveSamples(language);
                        }

                        if (++miniBatchNumber % generateSamplesEveryNMinibatches == 0) {
//                    int pos=(int)(Math.random()*input.length())-sizeOfSongSeed;
//                    if (pos<0)pos=0;
//                    int pod2=Math.max(0,generationInitialization.length()-sizeOfCurrentlyPlayingSeed);
//                    String xx=generationInitialization.substring(pod2);
//                    generationInitialization=input.substring(pos,pos+sizeOfSongSeed)+xx;
                            System.out.println("--------------------");

                            System.out.println("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " characters");
                            System.out.println("Sampling characters from network given initialization \"" + (generationInitialization == null ? "" : generationInitialization) + "\"");
                            String[] samples = sampleCharactersFromNetwork(generationInitialization, net, finalIter, rng, nCharactersToSample[0], nSamplesToGenerate);
                            if (generationInitialization != null && samples != null && samples[0] != null && samples[0].startsWith(generationInitialization))
                                samples[0] = samples[0].substring(generationInitialization.length());

//                            List<Segment> segments = song.analysis.getSegments();

//            audio.play(song.getAudioInterval(sem,segMapped));

                            if (net.score() < startPlayingScore) {
                                generationInitialization = samples[0];
                                AudioInterval[] listAudioIntervals = new AudioInterval[generationInitialization.length()];
                                for (int j = 0; j < generationInitialization.length(); j++) {
                                    AudioInterval ss = languageRev.get(generationInitialization.charAt(j));
//                                    Segment sem = segments.get(ss.segment);
                                    listAudioIntervals[j] = ss;
//
//                            audio.play(song.getAudioInterval(sem, ss));
                                }
                                if (smoothing) {
                                    //smoothing below just restores the song
                                    //input
                                    System.out.println("input=" + input);
                                    System.out.println("gener=" + generationInitialization);

                                    //matching algorithm

                                    int pos = 0;
                                    String toProcess = generationInitialization;
                                    while (pos < generationInitialization.length() - 1) {
                                        int lowest = Integer.MAX_VALUE;
                                        String best = "";
                                        int bestpos = -1;
                                        int sizeOfMatches = generationInitialization.length();
                                        for (int size = 1; size < Math.min(sizeOfMatches, toProcess.length()); size++) {
                                            String g = toProcess.substring(0, size);
                                            for (int jj = 0; jj < input.length() - size; jj++) {
                                                String h = input.substring(jj, jj + size);
//                                    System.out.println(g+"="+h);
                                                int score = (int) (((sizeOfMatches - size) * 1.2d) + Levenshtein.getLevenshteinDistance(g, h));
                                                if (score < lowest) {
                                                    lowest = score;
                                                    best = h;
                                                    bestpos = jj;
                                        System.out.println("score:" + score + "\tbest:" + bestpos + "\t" + best);
                                                }
                                            }

                                        }
                                        int pos2 = pos;
                                        for (int hh = bestpos; hh < bestpos + best.length(); hh++) {

                                            //listAudioIntervals[pos2] = song.getAudioIntervalForSegment(hh);
                                            pos2++;
                                        }
                                        toProcess = toProcess.substring(best.length());
                                        pos += best.length();

                                    }
                                }
                                generationInitialization ="";
                                for (AudioInterval ai:listAudioIntervals){
                                    generationInitialization+=origlanguage.get(ai);
                                }
                                if (audio.queue.size() < 50) {
                                    nCharactersToSample[0]+=2;
                                }
                                if (audio.queue.size() > 300) {
                                    nCharactersToSample[0]-=2;
                                }
                                if (audio.queue.size() - lastQueueSize > 0) {
                                    nCharactersToSample[0]--;
                                }
                                if (audio.queue.size() - lastQueueSize < 0) {
                                    nCharactersToSample[0]++;
                                }
                                if (nCharactersToSample[0]<1){
                                    nCharactersToSample[0] = 1;
                                }
                                System.out.println("audio queue size = " + audio.queue.size() + "\t" + "nCharactersToSample = " + nCharactersToSample[0]);
                                lastQueueSize = audio.queue.size();
                                for (AudioInterval s : listAudioIntervals) {
                                    audio.play(s);
                                }


                                System.out.println("-------------------------------------------------------");
                            }
                        }
                    }

                    finalIter.reset();    //Reset iterator for another epoch
                }
            }
        }).start();

        while (!bbtest) try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String[] samples = sampleCharactersFromNetwork(generationInitialization, net, iter, rng, nCharactersToSample[0], nSamplesToGenerate);
        for (int j = 0; j < samples.length; j++) {
            System.out.println("----- Sample " + j + " -----");
            System.out.println(samples[j]);
            System.out.println();

        }
        System.out.println("\n\nExample complete");
        return samples;
    }

    private static MultiLayerNetwork loadNet() {
        //Load the model
        MultiLayerNetwork restored = null;
        try {
            restored = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        } catch (IOException e) {


        }
        return restored;
    }

    private static void saveNet(MultiLayerNetwork net) {
        //Save the model
        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        try {
            ModelSerializer.writeModel(net, locationToSave, saveUpdater);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<AudioInterval, Character> loadSamples() {
        return (HashMap<AudioInterval, Character>) ObjectManager.read("samples.ser");
    }

    private static void saveSamples(HashMap<AudioInterval, Character> samples) {
        ObjectManager.write(samples, "samples.ser");
    }

    /**
     * Downloads Shakespeare training data and stores it locally (temp directory). Then set up and return a simple
     * DataSetIterator that does vectorization based on the text.
     *
     * @param miniBatchSize  Number of text segments in each training mini-batch
     * @param sequenceLength Number of characters in each text segment.
     */
    public static CharacterIteratorRNNDemo getShakespeareIterator(String input, int miniBatchSize, int sequenceLength) throws Exception {
        //The Complete Works of William Shakespeare
        //5.3MB file in UTF-8 Encoding, ~5.4 million characters
        //https://www.gutenberg.org/ebooks/100
        //String url = "https://s3.amazonaws.com/dl4j-distribution/pg100.txt";
        //String tempDir = System.getProperty("java.io.tmpdir");
        //String fileLocation = tempDir + "/Shakespeare.txt";	//Storage location from downloaded file
        //File f = new File(fileLocation);
        //if( !f.exists() ){
        //		FileUtils.copyURLToFile(new URL(url), f);
        //		System.out.println("File downloaded to " + f.getAbsolutePath());
        //	} else {
        //		System.out.println("Using existing text file at " + f.getAbsolutePath());
        //	}

        //	if(!f.exists()) throw new IOException("File does not exist: " + fileLocation);	//Download problem?

//        Vector[] validCharacters = CharacterIteratorRNNDemo.getMinimalCharacterSet(input);    //Which characters are allowed? Others will be removed

        return new CharacterIteratorRNNDemo(input, Charset.forName("UTF-8"),
                miniBatchSize, sequenceLength, new Random(12345));
    }

    /**
     * Generate a sample from the network, given an (optional, possibly null) initialization. Initialization
     * can be used to 'prime' the RNNDemo with a sequence you want to extend/continue.<br>
     * Note that the initalization is used for all samples
     *
     * @param initialization     String, may be null. If null, select a random character as initialization for all samples
     * @param net                MultiLayerNetwork with one or more GravesLSTM/RNNDemo layers and a softmax output layer
     * @param iter               CharacterIteratorRNN. Used for going from indexes back to characters
     * @param charactersToSample Number of characters to sample from network (excluding initialization)
     */
    private static String[] sampleCharactersFromNetwork(String initialization, MultiLayerNetwork net,
                                                        CharacterIteratorRNNDemo iter, Random rng, int charactersToSample, int numSamples) {
        //Set up initialization. If no initialization: use a random character
        if (initialization == null) {
            initialization = String.valueOf(iter.getRandomCharacter());
        }

        //Create input for initialization
        INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), initialization.length());
        Vector[] init = new Vector[initialization.length()];
        for (int i = 0; i < init.length; i++) {
            init[i] = new Vector(initialization.charAt(i));
        }
        for (int i = 0; i < init.length; i++) {
            int idx = iter.convertCharacterToIndex(init[i]);
            for (int j = 0; j < numSamples; j++) {
                initializationInput.putScalar(new int[]{j, idx, i}, 1.0f);
            }
        }

        StringBuilder[] sb = new StringBuilder[numSamples];
        for (int i = 0; i < numSamples; i++) sb[i] = new StringBuilder(initialization);

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        //Sampling is done in parallel here
        net.rnnClearPreviousState();
        INDArray output = net.rnnTimeStep(initializationInput);
        output = output.tensorAlongDimension((int) (output.size(2) - 1), 1, 0);    //Gets the last time step output

        for (int i = 0; i < charactersToSample; i++) {
            //Set up next input (single time step) by sampling from previous output
            INDArray nextInput = Nd4j.zeros(numSamples, iter.inputColumns());
            //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
            for (int s = 0; s < numSamples; s++) {
                double[] outputProbDistribution = new double[iter.totalOutcomes()];
                for (int j = 0; j < outputProbDistribution.length; j++)
                    outputProbDistribution[j] = output.getDouble(s, j);
                int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution, rng);

                nextInput.putScalar(new int[]{s, sampledCharacterIdx}, 1.0f);        //Prepare next time step input
                sb[s].append(iter.convertIndexToCharacter(sampledCharacterIdx));    //Add sampled character to StringBuilder (human readable output)
            }

            output = net.rnnTimeStep(nextInput);    //Do one time step of forward pass
        }

        String[] out = new String[numSamples];
        for (int i = 0; i < numSamples; i++) out[i] = sb[i].toString();
        return out;
    }

    /**
     * Given a probability distribution over discrete classes, sample from the distribution
     * and return the generated class index.
     *
     * @param distribution Probability distribution over classes. Must sum to 1.0
     */
    public static int sampleFromDistribution(double[] distribution, Random rng) {
        double d = 0.0;
        double sum = 0.0;
        for (int t = 0; t < 10; t++) {
            d = rng.nextDouble();
            sum = 0.0;
            for (int i = 0; i < distribution.length; i++) {
                sum += distribution[i];
                if (d <= sum) return i;
            }
            //If we haven't found the right index yet, maybe the sum is slightly
            //lower than 1 due to rounding error, so try again.
        }
        //Should be extremely unlikely to happen if distribution is a valid probability distribution
//        throw new IllegalArgumentException("Distribution is invalid? d=" + d + ", sum=" + sum);
        return (int) (Math.random() * distribution.length);
    }

}