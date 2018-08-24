package mariannelinhares.mnistandroid.models;


//Provides access to an application's raw asset files;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.Log;
//Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
import java.io.BufferedReader;
//for erros
import java.io.FileInputStream;
import java.io.IOException;
//An InputStreamReader is a bridge from byte streams to character streams:
// //It reads bytes and decodes them into characters using a specified charset.
// //The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
//made by google, used as the window between android and tensorflow native C++
import org.tensorflow.Shape;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.lite.Interpreter;

import static android.content.ContentValues.TAG;


/**
 * Changed from https://github.com/MindorksOpenSource/AndroidTensorFlowMNISTExample/blob/master
 * /app/src/main/java/com/mindorks/tensorflowexample/TensorFlowImageClassifier.java
 * Created by marianne-linhares on 20/04/17.
 */

//lets create this classifer
public class TensorFlowClassifier {
    private static TensorFlowInferenceInterface tfHelper;

    static {
        System.loadLibrary("tensorflow_inference");
    }

    // Only returns if at least this confidence
    //must be a classification percetnage greater than this
    private static final float THRESHOLD = 0.1f;

    private static final String MODEL_FILE = "model.tflite";
    private static final String INPUT_NODE = "input_x";
    private static final String OUTPUT_NODE = "output/predictions";
    private static final String[] OUTPUT_NODES = new String[] {OUTPUT_NODE};

    private static final long[] INPUT_SIZE = {1, 78};
    private static final long[][] OUTPUT_SIZE = new long[1][1];


    protected Interpreter tflite;

    public TensorFlowClassifier(final Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity, MODEL_FILE));
    }

    public long[][] predictEmotion(int[] data) throws IOException {

        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
        }
//        Log.v("Input: ", INPUT_NODE);
//        Log.v("Classifying: ", String.valueOf(data[0]));
//        Log.v("Input size: ", String.valueOf(INPUT_SIZE));

        long startTime = SystemClock.uptimeMillis();
        tflite.run(data, OUTPUT_SIZE);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time cost to run model inference: " + Long.toString(endTime - startTime));


//        tfHelper.feed(INPUT_NODE, data, INPUT_SIZE);
////        tfHelper.feed("dropout_keep_prob", new float[]{1});
//        tfHelper.run(OUTPUT_NODES);
//        tfHelper.fetch(OUTPUT_NODE, result);

        return OUTPUT_SIZE;
    }


    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    //given a saved drawn model, lets read all the classification labels that are
    //stored and write them to our in memory labels list
    private static List<String> readLabels(AssetManager am, String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }

        br.close();
        return labels;
    }

    /**
     * Get the name of the model file stored in Assets.
     *
     * @return
     */
    protected String getModelPath() {
        return MODEL_FILE;
    }

    protected String getLabelPath() {
        return null;
    }


//   //given a model, its label file, and its metadata
//    //fill out a classifier object with all the necessary
//    //metadata including output prediction
//    public static TensorFlowClassifier create(AssetManager assetManager, String name,
//            String modelPath, String labelFile, int inputSize, String inputName, String outputName,
//            boolean feedKeepProb) throws IOException {
//        //intialize a classifier
//        TensorFlowClassifier c = new TensorFlowClassifier();
//
//        //store its name, input and output labels
//        c.name = name;
//
//        c.inputName = inputName;
//        c.outputName = outputName;
//
//        //read labels for label file
//        c.labels = readLabels(assetManager, labelFile);
//
//        //set its model path and where the raw asset files are
//        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
//        int numClasses = 10;
//
//        //how big is the input?
//        c.inputSize = inputSize;
//
//        // Pre-allocate buffer.
//        c.outputNames = new String[] { outputName };
//
//        c.outputName = outputName;
//        c.output = new float[numClasses];
//
//        c.feedKeepProb = feedKeepProb;
//
//        return c;
//    }
//
//    @Override
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public Classification recognize(final float[] pixels) {
//
//        //using the interface
//        //give it the input name, raw pixels from the drawing,
//        //input size
//        tfHelper.feed(inputName, pixels, 1, inputSize, inputSize, 1);
//
//        //probabilities
//        if (feedKeepProb) {
//            tfHelper.feed("keep_prob", new float[] { 1 });
//        }
//        //get the possible outputs
//        tfHelper.run(outputNames);
//
//        //get the output
//        tfHelper.fetch(outputName, output);
//
//        // Find the best classification
//        //for each output prediction
//        //if its above the threshold for accuracy we predefined
//        //write it out to the view
//        Classification ans = new Classification();
//        for (int i = 0; i < output.length; ++i) {
//            System.out.println(output[i]);
//            System.out.println(labels.get(i));
//            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
//                ans.update(output[i], labels.get(i));
//            }
//        }
//
//        return ans;
//    }
}
