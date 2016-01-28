package fedffm.bytewrite;

import android.content.Intent;
import android.graphics.Bitmap;
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


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String MAIN_ACTIVITY = "MainActivity";  // Log tag

    private ImageView image;
    private TextView instructions;
    private TextView confirmation;
    private Button yesButton;
    private Button noButton;
    private Button cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(MAIN_ACTIVITY, "onCreate() fired");

        // Get references to our views
        cameraButton = (Button)findViewById(R.id.button);
        yesButton = (Button)findViewById(R.id.yes);
        noButton = (Button)findViewById(R.id.no);
        instructions = (TextView)findViewById(R.id.textView);
        confirmation = (TextView)findViewById(R.id.confirmation);

        // Set the visibilities
        cameraButton.setVisibility(View.VISIBLE);
        yesButton.setVisibility(View.INVISIBLE);
        noButton.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.VISIBLE);
        confirmation.setVisibility(View.INVISIBLE);
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
        image = (ImageView)findViewById(R.id.imageView);
        image.setVisibility(View.VISIBLE);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);
            confirm();
        }
    }

    /**
     * Use the device's built in camera to capture an image
     */
    public void launchImageCaptureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void confirm() {
        // Get references to our views
        cameraButton = (Button)findViewById(R.id.button);
        yesButton = (Button)findViewById(R.id.yes);
        noButton = (Button)findViewById(R.id.no);
        instructions = (TextView)findViewById(R.id.textView);
        confirmation = (TextView)findViewById(R.id.confirmation);

        // Toggle visibility
        cameraButton.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.INVISIBLE);
        yesButton.setVisibility(View.VISIBLE);
        noButton.setVisibility(View.VISIBLE);
        confirmation.setVisibility(View.VISIBLE);
    }

    public void yes(View view) {
        // Get references to our views
        cameraButton = (Button)findViewById(R.id.button);
        yesButton = (Button)findViewById(R.id.yes);
        noButton = (Button)findViewById(R.id.no);
        instructions = (TextView)findViewById(R.id.textView);
        confirmation = (TextView)findViewById(R.id.confirmation);

        // Set the visibilities
        cameraButton.setVisibility(View.INVISIBLE);
        yesButton.setVisibility(View.INVISIBLE);
        noButton.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.INVISIBLE);
        confirmation.setVisibility(View.INVISIBLE);
    }

    public void no(View view) {
        // Get references to our views
        image = (ImageView)findViewById(R.id.imageView);
        cameraButton = (Button)findViewById(R.id.button);
        yesButton = (Button)findViewById(R.id.yes);
        noButton = (Button)findViewById(R.id.no);
        instructions = (TextView)findViewById(R.id.textView);
        confirmation = (TextView)findViewById(R.id.confirmation);

        // Set the visibilities
        image.setVisibility(View.INVISIBLE);
        cameraButton.setVisibility(View.VISIBLE);
        yesButton.setVisibility(View.INVISIBLE);
        noButton.setVisibility(View.INVISIBLE);
        instructions.setVisibility(View.VISIBLE);
        confirmation.setVisibility(View.INVISIBLE);
    }
}
