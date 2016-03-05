package fedffm.bytewrite;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**********************************************************************************************
 *                                   Main Activity
 **********************************************************************************************/

public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String LOG_TAG = "MainActivity";  // Log tag
    private static boolean DETAILED_LOGGING = false;

    // Main Activity views
    private Bitmap    bitmap;
    private EditText  textBox;
    private ImageView image;
    private TextView  instructions;
    private Button    processButton;
    private Button    retakeButton;
    private Button    cameraButton;

    // Timing views
    private TextView greyscale;
    private TextView binarize;
    private TextView crop;
    private TextView segment;
    private TextView identify;
    private TextView total;

    private TextView timeGreyscale;
    private TextView timeBinarize;
    private TextView timeCrop;
    private TextView timeSegment;
    private TextView timeIdentify;
    private TextView timeTotal;

    // Character base
    private CharacterBase characterBase;
    private boolean loadingCharBase = false;
    private String    imagePath = "";


    /**********************************************************************************************
     *                       ASYNC TASK: CharacterBase Loader
     **********************************************************************************************/

    /**
     * Because loading the CharacterBase may take a while, do it on an AsyncTask
     */
    class CharacterBaseLoader extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            // Make sure we're pulling characters from the character base
            List<Character> characters;

            try {
                loadingCharBase = true;

                // Instantiate the singleton character base
                characterBase = CharacterBase.getInstance(MainActivity.this);
                characters = characterBase.getAllCharacterSamples();

            } catch (Exception e) {
                Log.e("LongOperation", "Interrupted", e);
                return "Interrupted";
            }
            return characters.size() + " / " + CharacterBase.getInstance(MainActivity.this).size() + " samples loaded";
        }

        @Override
        protected void onPostExecute(String result) {
            // Tell the activity that we have finished
            Log.i(LOG_TAG, result);
            loadingCharBase = false;
        }
    }

    /**********************************************************************************************
     *                       ASYNC TASK: Processor
     **********************************************************************************************/

    /**
     * Because loading the CharacterBase may take a while, do it on an AsyncTask
     */
    class Processor extends AsyncTask<String, Void, String> {
        // Get references to the loading elements
        TextView loading = (TextView) findViewById(R.id.loading);
        TextView wait    = (TextView) findViewById(R.id.wait);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            loading.setText("processing...");
            loading.setVisibility(View.VISIBLE);
            wait.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Begin the image processing
                process();

            } catch (Exception e) {
                Log.e("LongOperation", "Interrupted", e);
                return "Interrupted";
            }
            return "Processing complete";
        }

        @Override
        protected void onPostExecute(String result) {
            // Tell the activity that we have finished
            loading.setVisibility(View.INVISIBLE);
            wait.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Log.i(LOG_TAG, result);
        }
    }

/**********************************************************************************************
 *                                   Main Activity
 **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "onCreate() fired");

        // If an image exists, load it
        if (loadImage())
            confirm();
        else
            getViewReferences();

        // Load the character base as an async task
        if (!loadingCharBase)
            new CharacterBaseLoader().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            loadImage();
            confirm();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "MainActivity has been destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "MainActivity has been paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "MainActivity has been stopped");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "MainActivity has been started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "MainActivity has been resumed");
    }

    /**
     * When the activity starts up, get the references to each view
     */
    private void getViewReferences() {
        // Get references to our views
        image         = (ImageView)findViewById(R.id.imageView);
        cameraButton  = (Button)findViewById(R.id.button);
        processButton = (Button)findViewById(R.id.processButton);
        retakeButton  = (Button)findViewById(R.id.retakeButton);
        instructions  = (TextView)findViewById(R.id.textView);
        textBox       = (EditText)findViewById(R.id.content);

        // References to the timing views
        greyscale           = (TextView)findViewById(R.id.greyscale);
        binarize            = (TextView)findViewById(R.id.binarize);
        crop                = (TextView)findViewById(R.id.crop);
        segment             = (TextView)findViewById(R.id.seg);
        identify            = (TextView)findViewById(R.id.identify);
        total               = (TextView)findViewById(R.id.total);
        timeGreyscale       = (TextView)findViewById(R.id.timeGreyscale);
        timeBinarize        = (TextView)findViewById(R.id.timeBinarize);
        timeCrop            = (TextView)findViewById(R.id.timeCrop);
        timeSegment         = (TextView)findViewById(R.id.timeSeg);
        timeIdentify        = (TextView)findViewById(R.id.timeIdentify);
        timeTotal           = (TextView)findViewById(R.id.timeTotal);
    }

    /**
     * Use the device's built in camera to capture an image
     */
    public void launchCamera(View view) {
        // Create a file to store the image and get the directory/path
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = timeStamp + ".jpg";
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imagePath = directory.getAbsolutePath() + "/" + fileName;
        File file = new File(imagePath);

        // Start the camera intent
        Uri outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Load the bitmap from the device's storage
     */
    private boolean loadImage() {
        // Make sure an image has been created
        if (imagePath.equals(""))
            return false;

        // Log the path to the image
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "The path to the image is: " + imagePath);

        // Load the bitmap
        bitmap = Preprocessor.load(imagePath);

        // Put it into the Image View
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageBitmap(bitmap);
        image.setVisibility(View.VISIBLE);
        return true;
    }

    /**
     * Prompt the user to either process or retake the picture
     */
    private void confirm() {
        // Visible views
        processButton.setVisibility(View.VISIBLE);
        retakeButton.setVisibility(View.VISIBLE);

        // Invisible views
        cameraButton.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.INVISIBLE);
    }

    /**
     * Go to the Process Activity
     */
    private void process() {
        // Time for each step in the process
        float timeStart;
        float timeEnd;

        // Convert to greyscale
        timeStart = System.nanoTime();
        bitmap = Preprocessor.greyscale(bitmap);
        timeEnd = System.nanoTime();
        final float secondsGreyscale = (timeEnd - timeStart) / 1000000000;

        // Binarize
        timeStart = System.nanoTime();
        bitmap = Preprocessor.binarize(bitmap);
        timeEnd = System.nanoTime();
        final float secondsBinarize = (timeEnd - timeStart) / 1000000000;

        // Crop
        timeStart = System.nanoTime();
        bitmap = Preprocessor.crop(bitmap);
        timeEnd = System.nanoTime();
        final float secondsCrop = (timeEnd - timeStart) / 1000000000;

        // Segment the characters
        timeStart = System.nanoTime();
        List<Character> characters = Preprocessor.segmentCharacters(bitmap);
        timeEnd = System.nanoTime();
        final float secondsSeg = (timeEnd - timeStart) / 1000000000;

        // Identify
        timeStart = System.nanoTime();
        final Word word = Identifier.identify(new Word(characters), this);
        timeEnd = System.nanoTime();
        final float secondsId = (timeEnd - timeStart) / 1000000000;

        // Total
        final float secondsTotal = secondsGreyscale + secondsBinarize + secondsCrop + secondsSeg + secondsId;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            // Display each time
            greyscale.setText("greyscale conversion");
            binarize.setText("binarization");
            crop.setText("cropping");
            segment.setText("segmentation");
            identify.setText("identification");
            total.setText("total");
            timeGreyscale.setText(String.format("%.2f", secondsGreyscale) + "s");
            timeBinarize.setText(String.format("%.2f" , secondsBinarize)  + "s");
            timeCrop.setText(String.format("%.2f"     , secondsCrop)      + "s");
            timeSegment.setText(String.format("%.2f"  , secondsSeg)       + "s");
            timeIdentify.setText(String.format("%.2f" , secondsId)        + "s");
            timeTotal.setText(String.format("%.2f"    , secondsTotal)     + "s");

            // Display the word
            textBox.setText(word.getString());
            textBox.setVisibility(View.VISIBLE);
            textBox.setSelection(textBox.getText().length());
            }
        });
    }

    /**
     * Start the processing AsyncTask
     * @param view The button that was pressed
     */
    public void startProcessing(View view) {
        image.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.INVISIBLE);
        processButton.setVisibility(View.INVISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);

        new Processor().execute();
    }
}