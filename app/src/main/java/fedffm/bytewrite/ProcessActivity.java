package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class ProcessActivity extends ActionBarActivity {
    private static final String PROCESS_ACTIVITY = "ProcessActivity"; // Log Tag

    // The captured image
    private Bitmap bitmap;

    // Activity views
    private ImageView image;
    private LinearLayout imageGallery;
    private Button segmentButton;

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
        // Convert to greyscale, binarize, and crop
        bitmap = Preprocessor.greyscale(bitmap);
        bitmap = Preprocessor.binarize(bitmap);
        bitmap = Preprocessor.crop(bitmap);
        image.setImageBitmap(bitmap);
    }

    /**
     * Split the bitmap into a list of characters
     * @param view The "Segment" button
     */
    public void segment(View view) {
        // Get the list of unidentified characters
        List<Character> characters = Preprocessor.segmentCharacters(bitmap);
        saveSegments(characters);

        // Display each character segment image in the linear layout
        for (int i = 0; i < characters.size(); ++i) {
            imageGallery.addView(getImageView(characters.get(i).getBitmap()));
        }

        // Make sure the image gallery is visible
        imageGallery.setVisibility(View.VISIBLE);

        // Hide the original bitmap view
        // as well as the "Segment" button
        image.setVisibility(View.INVISIBLE);
        segmentButton.setVisibility(View.INVISIBLE);
    }

    private View getImageView(Bitmap b) {
        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                     LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 20, 0);
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(b);

        return imageView;
    }

    /**
     * Generate our character pool
     */
    public void saveSegments(List<Character> characters) {
        for (int i = 0; i < characters.size(); ++i) {
            saveImage(characters.get(i).getBitmap());
        }
    }

    /**
     * Save a bitmap
     * @param finalBitmap The bitmap to be saved
     */
    private void saveImage(Bitmap finalBitmap) {
        // Get the directory to the download folder
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/bytewrite");
        myDir.mkdir();

        String fname = new Random().nextInt(1000000) + ".jpg";

        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
