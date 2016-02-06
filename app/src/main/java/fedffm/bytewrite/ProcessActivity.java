package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;


public class ProcessActivity extends ActionBarActivity {
    private static final String PROCESS_ACTIVITY = "ProcessActivity"; // Log Tag

    // Activity views
    private Bitmap bitmap;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
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

    /**
     * Load the bitmap from the device's storage
     */
    private void loadImage() {
        // Load the bitmap
        bitmap = Preprocessor.load(getIntent().getStringExtra("imagePath"));

        // Prepare the image view
        image = (ImageView) findViewById(R.id.imageViewProcess);
        image.setVisibility(View.VISIBLE);
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
        List<Character> characters = Preprocessor.segmentCharacters(bitmap);

        // Get the reference to the linear layout
        LinearLayout imageGallery = (LinearLayout) findViewById(R.id.imageGallery);

        // Display each
        for (int i = 0; i < characters.size(); ++i) {
            imageGallery.addView(getImageView(characters.get(i).getBitmap()));
        }

        image.setVisibility(View.INVISIBLE);
    }

    private View getImageView(Bitmap b) {
        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 20, 0);
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(b);

        return imageView;
    }




}
