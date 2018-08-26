package mariannelinhares.mnistandroid.models;


//Provides access to an application's raw asset files;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.Log;
//Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
//for erros
import java.io.FileInputStream;
import java.io.IOException;
//An InputStreamReader is a bridge from byte streams to character streams:
// //It reads bytes and decodes them into characters using a specified charset.
// //The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
//made by google, used as the window between android and tensorflow native C++
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.lite.Interpreter;

import static android.content.ContentValues.TAG;

//lets create this classifer
public class TensorFlowClassifier {

    static {
        System.loadLibrary("tensorflow_inference");
    }

//Initialize files and variables needed for inference
    private static final String MODEL_FILE = "model.tflite";
    private static final long[][] OUTPUT_SIZE = new long[1][1];

//Instantiate Interpreter class
    protected Interpreter tflite;

//    Constructor
    public TensorFlowClassifier(final Activity activity) throws IOException {
        tflite = new Interpreter(loadTfLiteModel(activity, MODEL_FILE));
    }

    public long[][] predictEmotion(int[] data) {

        if (tflite == null) {
            Log.e(TAG, "Classifier has not been initialized.");
        }

        long startTime = SystemClock.uptimeMillis();
        tflite.run(data, OUTPUT_SIZE);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time cost to run model inference: " + Long.toString(endTime - startTime));

        return OUTPUT_SIZE;
    }


    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadTfLiteModel(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}
