package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;


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

        // Convert it to greyscale
        bitmap = Preprocessor.greyscale(bitmap);

        // Put it into the Image View
        image = (ImageView) findViewById(R.id.imageViewProcess);
        image.setImageBitmap(bitmap);
        image.setVisibility(View.VISIBLE);
    }
}
