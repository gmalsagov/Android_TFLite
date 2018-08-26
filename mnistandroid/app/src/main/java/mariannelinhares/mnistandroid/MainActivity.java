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
import java.util.Random;
//encapsulates a classified image
//public interface to the classification class, exposing a name and the recognize function
import mariannelinhares.mnistandroid.models.Classification;
import mariannelinhares.mnistandroid.models.Classifier;
//contains logic for reading labels, creating classifier, and classifying
import mariannelinhares.mnistandroid.models.TensorFlowClassifier;

import static android.content.ContentValues.TAG;

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

            String line = null;
            String sentence = null;
            String label_true = null;
            String[] labels = new String[7];
            HashMap<String, Integer> map = new HashMap();

            try {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(
                        getAssets().open("dictionary.txt")));

                BufferedReader br2 = new BufferedReader(new InputStreamReader(
                        getAssets().open("isear_test.csv")));


                BufferedReader br3 = new BufferedReader(new InputStreamReader(
                        getAssets().open("labels.txt")));

                int num = 0;

//               Read labels file and store in array
                while((line=br3.readLine())!=null) {
                    labels[num] = line;
                    num++;
                }

//                Read dictionary into a hashmap
                while((line=br1.readLine())!=null) {
                    String str[] = line.split(",");
                    map.put(str[0], Integer.valueOf(str[1]));

                }

//                Generate random number within 0-1508 limit
                Random rand = new Random();
                int  n = rand.nextInt(1508);

                int i = 0;
                line = null;

//                Skip through lines before n
                for (int j = 0; j < n; j++){
                    line = br2.readLine();
                }

//                Store label and sentence into variables
                String str[] = line.split(",");
                label_true = str[0];
                sentence = str[1];

//                Close buffer readers
                br1.close();
                br2.close();
                br3.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            Split sentence into words and store in array
            String[] words = sentence.split(" ");
            int[] encoded_words = new int[40];

//            Encode words into int ids from dictionary
            for (int i = 0; i < words.length; i++) {

                if (i < encoded_words.length) {
                    Integer value = map.get(words[i]);

                    int id = 0;
                    if (value != null) {
                        id = Integer.parseInt(value.toString());
                    }

                    encoded_words[i] = id;
                } else {
                    break;
                }
            }

            Context context = this;
            Activity activity = (Activity) context;
            long[][] result = new long[1][1];

//            Initialize TensorflowClassifier class
            TensorFlowClassifier classifier = null;
            try {
//                Pass activity to classifier object
                classifier = new TensorFlowClassifier(activity);

//                Run inference
                result = classifier.predictEmotion(encoded_words);

            } catch (IOException e) {
                e.printStackTrace();
            }

//            Cast result into integer
            int index = (int) result[0][0];
//            Print sentence and emotion on screen
            typeHere.setText(sentence);
            resText.setText(labels[index]);
        }
    }

}