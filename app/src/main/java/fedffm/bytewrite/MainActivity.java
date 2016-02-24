package fedffm.bytewrite;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String MAIN_ACTIVITY = "MainActivity";  // Log tag
    private static boolean DETAILED_LOGGING = false;

    // Main Activity views
    private String    imagePath = "";
    private Bitmap    bitmap;
    private ImageView image;
    private TextView  instructions;
    private Button    processButton;
    private Button    retakeButton;
    private Button    cameraButton;

    // Sample views
    private Button    sampleButton;
    private ImageView sampleImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (DETAILED_LOGGING)
            Log.i(MAIN_ACTIVITY, "onCreate() fired");

        // If an image exists, load it
        if (loadImage())
            confirm();
        else
            setupViews();
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
            Log.i(MAIN_ACTIVITY, "MainActivity has been destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DETAILED_LOGGING)
            Log.i(MAIN_ACTIVITY, "MainActivity has been paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DETAILED_LOGGING)
            Log.i(MAIN_ACTIVITY, "MainActivity has been stopped");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DETAILED_LOGGING)
            Log.i(MAIN_ACTIVITY, "MainActivity has been started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DETAILED_LOGGING)
            Log.i(MAIN_ACTIVITY, "MainActivity has been resumed");
    }

    /**
     * When the activity starts up, display all of the necessary views
     */
    private void setupViews() {
        // Get references to our views
        image         = (ImageView)findViewById(R.id.imageView);
        cameraButton  = (Button)findViewById(R.id.button);
        processButton = (Button)findViewById(R.id.processButton);
        retakeButton  = (Button)findViewById(R.id.retakeButton);
        instructions  = (TextView)findViewById(R.id.textView);
        sampleButton  = (Button)findViewById(R.id.sampleButton);
        sampleImage   = (ImageView)findViewById(R.id.sampleImage);

        // Visible views
        cameraButton.setVisibility(View.VISIBLE);
        instructions.setVisibility(View.VISIBLE);
        sampleButton.setVisibility(View.VISIBLE);

        // Invisible views
        image.setVisibility(View.INVISIBLE);
        processButton.setVisibility(View.INVISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
        sampleImage.setVisibility(View.INVISIBLE);
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
            Log.i(MAIN_ACTIVITY, "The path to the image is: " + imagePath);

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
     * @param view The button that was pressed
     */
    public void process(View view) {
        // Start a new intent
        Intent intent = new Intent(this, ProcessActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivity(intent);
    }

    /**
     * Load a character from the sample base
     */
    public void loadSamplePool(View view) {
        // Instantiate a sample pool
        SamplePool pool = new SamplePool(this);

        List<Character> characters = pool.getAllCharacterSamples();

        for (int i = 0; i < characters.size(); ++i) {
            Log.i(MAIN_ACTIVITY, "Character: " + characters.get(i).getName() + " ASCII: " + characters.get(i).getAscii());
        }
    }
}
