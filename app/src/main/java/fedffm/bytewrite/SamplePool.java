package fedffm.bytewrite;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SamplePool {
    private List<Character> characters;
    private List<Integer>   characterIDs;

    private int count;

    /**
     * How big is our sample pool?
     * @return Return the number of samples
     */
    private int size() {
        return 0;
    }

    /**
     * Load all of the sample image assets
     */
    private void loadAssetFiles(String directory) {
        File f = new File(directory);
        File[] files = f.listFiles();

        if (files != null)
            for (int i = 0; i < files.length; i++) {
                count++;
                File file = files[i];

                if (file.isDirectory())
                    loadAssetFiles(file.getAbsolutePath());
            }
        System.out.println("There are " + count + " files in " + directory);
    }

    /**
     * Load all of the sample image assets
     */
    private void importImageAssets(Context context) {
        AssetManager assetManager = context.getAssets();

        // Get the name of the file
        String fileName = "characters/t/2.jpg";

        // Create an input stream
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

    }

    /**
     * Load all character samples for a single given character
     * @param name The name of the character
     * @return The sample characters associated with the specified character
     */
    public List<Character> getCharacterSamples(char name) {
        return new ArrayList<>();
    }

    /**
     * Load all character samples
     * @return All character samples that exist in the sample pool
     */
    public List<Character> getAllCharacterSamples() {
        return new ArrayList<>();
    }


    /**
     * Used to grow our sample pool
     * @param newCharacter A new character that was correctly identified
     */
    public void addNewCharacter(Character newCharacter) {

    }
}
