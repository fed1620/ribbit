package fedffm.bytewrite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String MAIN_ACTIVITY = "MainActivity";  // Log tag

    // Main Activity views
    private String imagePath = "";
    private Bitmap    bitmap;
    private ImageView image;
    private TextView  instructions;
    private Button processButton;
    private Button retakeButton;
    private Button    cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(MAIN_ACTIVITY, "onCreate() fired");

        // Get references to our views
        image        = (ImageView)findViewById(R.id.imageView);
        cameraButton = (Button)findViewById(R.id.button);
        processButton = (Button)findViewById(R.id.processButton);
        retakeButton = (Button)findViewById(R.id.retakeButton);
        instructions = (TextView)findViewById(R.id.textView);

        // Visible views
        cameraButton.setVisibility(View.VISIBLE);
        instructions.setVisibility(View.VISIBLE);

        // Invisible views
        image.setVisibility(View.INVISIBLE);
        processButton.setVisibility(View.INVISIBLE);
        retakeButton.setVisibility(View.INVISIBLE);
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
    protected void onDestroy() {
        super.onDestroy();
        Log.i(MAIN_ACTIVITY, "MainActivity has been destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(MAIN_ACTIVITY, "MainActivity has been paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(MAIN_ACTIVITY, "MainActivity has been stopped");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(MAIN_ACTIVITY, "MainActivity has been started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MAIN_ACTIVITY, "MainActivity has been resumed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Get the bitmap image
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                Log.i(MAIN_ACTIVITY, "Scaled height: " + bitmap.getHeight() + " Scaled width: " + bitmap.getWidth());
            }

            // Put it into the Image View
            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
            confirm();
        }
    }

    /**
     * Use the device's built in camera to capture an image
     */
    public void launchCamera(View view) {
        // Create a file to store the image and get the directory/path
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    public void process(View view) {
        // Start a new intent
        Intent intent = new Intent(this, ProcessActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivity(intent);
    }
}
