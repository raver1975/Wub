package com.klemstinegroup.wub.ai.encog;
/*
 * Encog(tm) Java Examples v3.4
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2016 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.echonest.api.v4.Segment;
import com.klemstinegroup.wub.system.Settings;
import com.klemstinegroup.wub.system.Song;
import com.klemstinegroup.wub.system.SongManager;
import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.bot.BotUtil;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.mathutil.error.ErrorCalculationMode;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import weka.core.Attribute;
import weka.core.Instance;

public class SunSpotTimeseries {
    public static final int WINDOW_SIZE = 40;
    public static final int COLUMN_SIZE = 28;
    public static final int NUM_OF_SONGS=1;
    private static List<Segment> seggs=new ArrayList<>();

    SunSpotTimeseries(){
        StringBuilder csvinput = new StringBuilder();
        for (int ii = 0; ii < NUM_OF_SONGS; ii++) {
            System.out.println("adding song: "+ii);
            Song song = SongManager.getRandom(ii);
            List<Segment> seggs1 = song.analysis.getSegments();
            seggs.addAll(seggs1);


            for (Segment s : seggs) {
                double[] d = segmentToDoubleArray(s);
                for (int i = 0; i < 28; i++) {
                    String line =d[i] + (i==27?"":",");
                    csvinput.append(line);
                }
//                csvinput = csvinput.substring(0, csvinput.length() - 1);
                csvinput.append("\n");
            }
//            for (int i = 0; i < 1; i++) {
//                csvinput += csvinput;
//            }
        }
        run(csvinput.toString());
    }

    public void run(String csvinput) {
        try {
            ErrorCalculation.setMode(ErrorCalculationMode.RMS);
            // Download the data that we will attempt to model.
            File filename = new File("./auto-mpg.data");

            System.out.println("csvinput is " + csvinput.length());
            Files.write(Paths.get(filename.getPath()), csvinput.getBytes());

            // Define the format of the data file.
            // This area will change, depending on the columns and
            // format of the file that you are trying to model.
            CSVFormat format = new CSVFormat('.', ','); // decimal point and
            // space separated
            VersatileDataSource source = new CSVDataSource(filename, false,
                    format);

            VersatileMLDataSet data = new VersatileMLDataSet(source);
            data.getNormHelper().setFormat(format);
            ColumnDefinition[] columns = new ColumnDefinition[COLUMN_SIZE];
            for (int i = 0; i < COLUMN_SIZE; i++) {
                columns[i] = data.defineSourceColumn((i + ""), i,
                        ColumnType.continuous);
                data.defineInput(columns[i]);
                data.defineOutput(columns[i]);
            }
            // Analyze the data, determine the min/max/mean/sd of every column.
            data.analyze();

            // Use SSN & DEV to predict SSN. For time-series it is okay to have
            // SSN both as
            // an input and an output.
//			data.defineInput(columnSSN);
//			data.defineInput(columnDEV);
//			data.defineOutput(columnSSN);
//			data.defineOutput(columnDEV);

            // Create feedforward neural network as the model type.
            // MLMethodFactory.TYPE_FEEDFORWARD.
            // You could also other model types, such as:
            // MLMethodFactory.SVM: Support Vector Machine (SVM)
            // MLMethodFactory.TYPE_RBFNETWORK: RBF Neural Network
            // MLMethodFactor.TYPE_NEAT: NEAT Neural Network
            // MLMethodFactor.TYPE_PNN: Probabilistic Neural Network
            EncogModel model = new EncogModel(data);
            model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);


            // Send any output to the console.
            model.setReport(new ConsoleStatusReportable());

            // Now normalize the data. Encog will automatically determine the
            // correct normalization
            // type based on the model you chose in the last step.
            data.normalize();

            // Set time series.
            data.setLeadWindowSize(1);
            data.setLagWindowSize(WINDOW_SIZE);

            // Hold back some data for a final validation.
            // Do not shuffle the data into a random ordering. (never shuffle
            // time series)
            // Use a seed of 1001 so that we always use the same holdback and
            // will get more consistent results.
            model.holdBackValidation(0.3, false, 1001);

            // Choose whatever is the default training type for this model.
            model.selectTrainingType(data);

            // Use a 5-fold cross-validated train. Return the best method found.
            // (never shuffle time series)
            MLRegression bestMethod = (MLRegression) model.crossvalidate(5,
                    false);

            // Display the training and validation errors.
            System.out.println("Training error: "
                    + model.calculateError(bestMethod,
                    model.getTrainingDataset()));
            System.out.println("Validation error: "
                    + model.calculateError(bestMethod,
                    model.getValidationDataset()));

            // Display our normalization parameters.
            NormalizationHelper helper = data.getNormHelper();
            System.out.println(helper.toString());

            // Display the final model.
            System.out.println("Final model: " + bestMethod);

            // Loop over the entire, original, dataset and feed it through the
            // model. This also shows how you would process new data, that was
            // not part of your training set. You do not need to retrain, simply
            // use the NormalizationHelper class. After you train, you can save
            // the NormalizationHelper to later normalize and denormalize your
            // data.
            ReadCSV csv = new ReadCSV(filename, false, format);
            String[] line = new String[COLUMN_SIZE];

            // Create a vector to hold each time-slice, as we build them.
            // These will be grouped together into windows.
            double[] slice = new double[COLUMN_SIZE];
            VectorWindow window = new VectorWindow(WINDOW_SIZE + 1);
            MLData input = helper.allocateInputVector(WINDOW_SIZE + 1);

            // Only display the first 100
            int stopAfter = 10000;

            while (csv.next() && stopAfter > 0) {
                StringBuilder result = new StringBuilder();

                for (int i = 0; i < COLUMN_SIZE; i++) {
                    line[i] = csv.get(columns[i].getIndex());// ssn
                }

                helper.normalizeInputVector(line, slice, false);

                // enough data to build a full window?
                if (window.isReady()) {
                    window.copyWindow(input.getData(), 0);
                    MLData output = bestMethod.compute(input);
                    double[] predictedArray = new double[COLUMN_SIZE];
                    for (int i = 0; i < COLUMN_SIZE; i++) {
                        String correct = csv.get(i); // trying to predict SSN.
                        String predicted = helper
                                .denormalizeOutputVectorToString(output)[i];
                        predictedArray[i] = Double.parseDouble(predicted);
                        result.append(correct + " = " + predicted + "\t");
                    }
                    Segment predSeg = DoubleArrayToSegment(predictedArray);
//					result.append(Arrays.toString(line));
//					result.append(" -> predicted: ");
//					result.append(predicted1);
//					result.append("(\tcorrect: ");
//					result.append(correct1);
//					result.append(")\t");
//					result.append(" -> predicted: ");
//					result.append(predicted2);
//					result.append("(\tcorrect: ");
//					result.append(correct2);
//					result.append(")");

//                    System.out.println(result.toString());
                    System.out.println(predSeg);
                }


                // Add the normalized slice to the window. We do this just after
                // the after checking to see if the window is ready so that the
                // window is always one behind the current row. This is because
                // we are trying to predict next row.
                window.add(slice);

                stopAfter--;
            }

            // Delete data file and shut down.
            filename.delete();
            Encog.getInstance().shutdown();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SunSpotTimeseries prg = new SunSpotTimeseries();
    }

    protected static double distance(double[] in1, double[] in2) {
        int dist = 0;
        for (int i = 0; i < in1.length; i++) {
            dist += (in1[i] - in2[i]) * (in1[i] - in2[i]);
        }
        return Math.sqrt(dist);
    }

    protected static Segment DoubleArrayToSegment(double[] attlist) {
        int cnt = 0;

//        attlist[cnt] = attlist[cnt++] / Settings.durationFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.loudFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.loudFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.loudFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.timbreFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;
//        attlist[cnt] = attlist[cnt++] / Settings.pitchFactor;

        Segment found = null;
        double best = Double.MAX_VALUE;
        for (Segment s : seggs) {
            double bdiss = distance(attlist, segmentToDoubleArray(s));
            if (bdiss < best) {
                found = s;
                best = bdiss;
            }
        }

        return found;
    }

    protected static double[] segmentToDoubleArray(Segment s) {
        double[] attlist = new double[28];
        int cnt = 0;
        attlist[cnt++] = s.getDuration();
        attlist[cnt++] = s.getLoudnessMax();
        attlist[cnt++] = s.getLoudnessStart();
        attlist[cnt++] = s.getLoudnessMaxTime();
        attlist[cnt++] = s.getTimbre()[0];
        attlist[cnt++] = s.getTimbre()[1];
        attlist[cnt++] = s.getTimbre()[2];
        attlist[cnt++] = s.getTimbre()[3];
        attlist[cnt++] = s.getTimbre()[4];
        attlist[cnt++] = s.getTimbre()[5];
        attlist[cnt++] = s.getTimbre()[6];
        attlist[cnt++] = s.getTimbre()[7];
        attlist[cnt++] = s.getTimbre()[8];
        attlist[cnt++] = s.getTimbre()[9];
        attlist[cnt++] = s.getTimbre()[10];
        attlist[cnt++] = s.getTimbre()[11];
        attlist[cnt++] = s.getPitches()[0];
        attlist[cnt++] = s.getPitches()[1];
        attlist[cnt++] = s.getPitches()[2];
        attlist[cnt++] = s.getPitches()[3];
        attlist[cnt++] = s.getPitches()[4];
        attlist[cnt++] = s.getPitches()[5];
        attlist[cnt++] = s.getPitches()[6];
        attlist[cnt++] = s.getPitches()[7];
        attlist[cnt++] = s.getPitches()[8];
        attlist[cnt++] = s.getPitches()[9];
        attlist[cnt++] = s.getPitches()[10];
        attlist[cnt++] = s.getPitches()[11];
        return attlist;
    }
}
