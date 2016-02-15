package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessActivity extends ActionBarActivity {
    private static final String PROCESS_ACTIVITY = "ProcessActivity"; // Log Tag

    private Bitmap bitmap;

    // Activity views
    private ImageView image;
    private LinearLayout imageGallery;
    private Button segmentButton;
    private Button precisionButton;

    // List of characters
    private List<Character> characters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        setupViews();
        loadImage();
        processImage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_process, menu);
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

    private void setupViews() {
        // Prepare the image view
        image = (ImageView) findViewById(R.id.imageViewProcess);
        image.setVisibility(View.VISIBLE);

        // Image Gallery
        imageGallery = (LinearLayout) findViewById(R.id.imageGallery);
        imageGallery.setVisibility(View.INVISIBLE);

        // "Segment" button
        segmentButton = (Button) findViewById(R.id.segmentButton);
        segmentButton.setVisibility(View.VISIBLE);

        // "Precision Segment" button
        precisionButton = (Button) findViewById(R.id.precisionButton);
        precisionButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Load the bitmap from the device's storage
     */
    private void loadImage() {
        // Load the bitmap
        bitmap = Preprocessor.load(getIntent().getStringExtra("imagePath"));
    }

    /**
     * Use our Preprocessor class to call the appropriate methods
     */
    private void processImage() {
        // Convert it to greyscale
        bitmap = Preprocessor.greyscale(bitmap);

        // Binarize it
        bitmap = Preprocessor.binarize(bitmap);

        // Crop it
        bitmap = Preprocessor.crop(bitmap);
        image.setImageBitmap(bitmap);
    }

    public void segment(View view) {
        // Get the list of unidentified characters
        characters = Preprocessor.segmentCharacters(bitmap);

        // Display each character segment image in the linear layout
        for (int i = 0; i < characters.size(); ++i) {
            imageGallery.addView(getImageView(characters.get(i).getBitmap()));
        }

        // Make sure the image gallery is visible
        imageGallery.setVisibility(View.VISIBLE);

        // And the "Precision Segment" button
        precisionButton.setVisibility(View.VISIBLE);

        // Hide the original bitmap view
        // as well as the "Segment" button
        image.setVisibility(View.INVISIBLE);
        segmentButton.setVisibility(View.INVISIBLE);
    }

    private View getImageView(Bitmap b) {
        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 20, 0);
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(b);

        return imageView;
    }

    public void precisionSegment(View view) {
        assert characters != null;

        // Test with a connected segment
        Bitmap segment = characters.get(0).getBitmap();            // Hard-coded for the moment

        // Make sure the image gallery goes away
        imageGallery.setVisibility(View.INVISIBLE);

        // Display it in the image view
        image.setImageBitmap(segment);
        image.setVisibility(View.VISIBLE);

        // Apply precision segmentation to the bitmap
        Map<String, Integer> dimensions = Preprocessor.getAdjacentCharacterDimensions(segment);

        int x = dimensions.get("x");
        int y = dimensions.get("y");
        int w = dimensions.get("w");
        int h = dimensions.get("h");

        Log.i(PROCESS_ACTIVITY, "Subimage begins at (" + x + ", " + y + ")");
        Log.i(PROCESS_ACTIVITY, "w: " + w);
        Log.i(PROCESS_ACTIVITY, "h: " + h);
    }



}
