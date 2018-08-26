package mariannelinhares.mnistandroid;

/*
   Copyright 2016 Narrative Nights Inc. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   From: https://raw.githubusercontent
   .com/miyosuda/TensorFlowAndroidMNIST/master/app/src/main/java/jp/narr/tensorflowmnist
   /DrawModel.java
*/

//An activity is a single, focused thing that the user can do. Almost all activities interact with the user,
//so the Activity class takes care of creating a window for you in which you can place your UI with setContentView(View)
import android.app.Activity;
//PointF holds two float coordinates
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PointF;
//A mapping from String keys to various Parcelable values (interface for data container values, parcels)
import android.os.Bundle;
//Object used to report movement (mouse, pen, finger, trackball) events.
// //Motion events may hold either absolute or relative movements and other data, depending on the type of device.
import android.util.Log;
import android.view.MotionEvent;
//This class represents the basic building block for user interface components.
// A View occupies a rectangular area on the screen and is responsible for drawing
import android.view.View;
//A user interface element the user can tap or click to perform an action.
import android.widget.Button;
//A user interface element that displays text to the user. To provide user-editable text, see EditText.
import android.widget.TextView;
//Resizable-array implementation of the List interface. Implements all optional list operations, and permits all elements,
// including null. In addition to implementing the List interface, this class provides methods to
// //manipulate the size of the array that is used internally to store the list.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
// basic list
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
//encapsulates a classified image
//public interface to the classification class, exposing a name and the recognize function
import mariannelinhares.mnistandroid.models.Classification;
import mariannelinhares.mnistandroid.models.Classifier;
//contains logic for reading labels, creating classifier, and classifying
import mariannelinhares.mnistandroid.models.TensorFlowClassifier;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int SENTENCE_LENGTH = 40;

    protected Context context;

    // ui elements
    private Button clearBtn, classBtn;
    private TextView resText;
    private List<Classifier> mClassifiers = new ArrayList<>();

    // views
    private TextView typeHere;

    @Override
    // In the onCreate() method, you perform basic application startup logic that should happen
    //only once for the entire life of the activity.
    protected void onCreate(Bundle savedInstanceState) {
        //initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get drawing view from XML (where the finger writes the number)
        typeHere = (TextView) findViewById(R.id.typeHere);
        //get the model object

        // give it a touch listener to activate when the user taps
        typeHere.setOnClickListener(clickListener);

        //clear button
        //clear the drawing when the user taps
        clearBtn = (Button) findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);

        //class button
        //when tapped, this performs classification on the drawn image
        classBtn = (Button) findViewById(R.id.btn_class);
        classBtn.setOnClickListener(this);

        // res text
        //this is the text that shows the output of the classification
        resText = (TextView) findViewById(R.id.Recognize);

        // tensorflow
        //load up our saved model to perform inference from local storage
//        loadModel();
    }

    private View.OnClickListener clickListener= new View.OnClickListener() {
        public void onClick(View v) {
            typeHere.setText("");
        }
    };

    //the activity lifecycle

    @Override
    //OnResume() is called when the user resumes his Activity which he left a while ago,
    // //say he presses home button and then comes back to app, onResume() is called.
    protected void onResume() {
        super.onResume();
    }

    @Override
    //OnPause() is called when the user receives an event like a call or a text message,
    // //when onPause() is called the Activity may be partially or completely hidden.
    protected void onPause() {
        super.onPause();
    }
    //creates a model object in memory using the saved tensorflow protobuf model file
    //which contains all the learned weights
//    private void loadModel() {
//        //The Runnable interface is another way in which you can implement multi-threading other than extending the
//        // //Thread class due to the fact that Java allows you to extend only one class. Runnable is just an interface,
//        // //which provides the method run.
//        // //Threads are implementations and use Runnable to call the method run().
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //add 2 classifiers to our classifier arraylist
//                    //the tensorflow classifier and the keras classifier
//                    mClassifiers.add(
//                            TensorFlowClassifier.create(getAssets(), "TensorFlow",
//                                    "frozen_model.pb", "labels.txt", SENTENCE_LENGTH,
//                                    "input_x", "output/predictions", true));
//                    mClassifiers.add(
//                            TensorFlowClassifier.create(getAssets(), "Keras",
//                                    "opt_model.pb", "labels.txt", SENTENCE_LENGTH,
//                                    "input_x", "output/predictions", false));
//                } catch (final Exception e) {
//                    //if they aren't found, throw an error!
//                    throw new RuntimeException("Error initializing classifiers!", e);
//                }
//            }
//        }).start();
//    }

    @Override
    public void onClick(View view) {
        //when the user clicks something
        if (view.getId() == R.id.btn_clear) {
            //if its the clear button
            //clear the drawing
            typeHere.setText("");
            typeHere.invalidate();
            //empty the text view
            resText.setText("");
        }
        else if (view.getId() == R.id.btn_class) {
            //if the user clicks the classify button
            //get the pixel data and store it in an array
//            String sentence = typeHere.getText().toString();


            String sentence = "seeing a friend making love to a high school girl i accidentally was" +
                    " dragged into this room where the happenings had occurred i was disgusted at the reality";

//            String sentence = "getting a good mark for a subject i had worked hard at but expected only a moderate mark";

            String line = null;
            HashMap<String, Integer> map = new HashMap();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        getAssets().open("dictionary.txt")));


                while((line=br.readLine())!=null){
                    String str[] = line.split(",");
                    map.put(str[0], Integer.valueOf(str[1]));

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//        System.out.print(map);
            String[] words = sentence.split(" ");
            int[] encoded_words = new int[40];

            for (int i = 0; i < words.length; i++) {

                Integer value = map.get(words[i]);
                int id = 0;
                if (value != null) {
                    id = Integer.parseInt(value.toString());
                }

                encoded_words[i] = id;

            }
//            for (int i = encoded_words.length; i < 40; i++){
//                encoded_words[i] = 0;
//            }

            Log.v("result: ", Arrays.toString(encoded_words));

            Context context = this;
            Activity activity = (Activity) context;
            long[][] result = new long[1][1];
//            float[][] result = new float[1][7];

            TensorFlowClassifier classifier = null;
            try {
                classifier = new TensorFlowClassifier(activity);
                result = classifier.predictEmotion(encoded_words);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v("result: ", Arrays.deepToString(result));

//            //init an empty string to fill with the classification output
//            String text = "";
//            //for each classifier in our array
//            for (Classifier classifier : mClassifiers) {
//                //perform classification on the image
//                final Classification res = classifier.recognize(encoded_words);
//                //if it can't classify, output a question mark
//                if (res.getLabel() == null) {
//                    text += classifier.name() + ": ?\n";
//                } else {
//                    //else output its name
//                    text += String.format("%s: %s, %f\n", classifier.name(), res.getLabel(),
//                            res.getConf());
//                }
//            }
            resText.setText(Arrays.deepToString(result));
        }
    }

//    @Override
//    //this method detects which direction a user is moving
//    //their finger and draws a line accordingly in that
//    //direction
//    public boolean onTouch(View v, MotionEvent event) {
//        //get the action and store it as an int
//        int action = event.getAction() & MotionEvent.ACTION_MASK;
//        //actions have predefined ints, lets match
//        //to detect, if the user has touched, which direction the users finger is
//        //moving, and if they've stopped moving
//
//        //if touched
//        if (action == MotionEvent.ACTION_DOWN) {
//            //begin drawing line
//            processTouchDown(event);
//            return true;
//            //draw line in every direction the user moves
//        } else if (action == MotionEvent.ACTION_MOVE) {
//            processTouchMove(event);
//            return true;
//            //if finger is lifted, stop drawing
//        } else if (action == MotionEvent.ACTION_UP) {
//            processTouchUp();
//            return true;
//        }
//        return false;
//    }

    //draw line down

//    private void processTouchDown(MotionEvent event) {
//        //calculate the x, y coordinates where the user has touched
//        mLastX = event.getX();
//        mLastY = event.getY();
//        //user them to calcualte the position
//        drawView.calcPos(mLastX, mLastY, mTmpPiont);
//        //store them in memory to draw a line between the
//        //difference in positions
//        float lastConvX = mTmpPiont.x;
//        float lastConvY = mTmpPiont.y;
//        //and begin the line drawing
//        drawModel.startLine(lastConvX, lastConvY);
//    }

    //the main drawing function
    //it actually stores all the drawing positions
    //into the drawmodel object
    //we actually render the drawing from that object
    //in the drawrenderer class
//    private void processTouchMove(MotionEvent event) {
//        float x = event.getX();
//        float y = event.getY();
//
//        drawView.calcPos(x, y, mTmpPiont);
//        float newConvX = mTmpPiont.x;
//        float newConvY = mTmpPiont.y;
//        drawModel.addLineElem(newConvX, newConvY);
//
//        mLastX = x;
//        mLastY = y;
//        drawView.invalidate();
//    }

//    private void processTouchUp() {
//        drawModel.endLine();
//    }
}