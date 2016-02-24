package fedffm.bytewrite;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SamplePool {
    // Log tag
    private final static String SAMPLE_POOL = "SamplePool";
    private final static String ASSET_FOLDER = "characters";
    private final static boolean LOGGING_ENABLED = true;

    private List<String>    assetPaths;
    private List<Character> characters;

    private Context context;

    public SamplePool(Context context) {
        // Initialize the member variables
        this.assetPaths   = new ArrayList<>();
        this.characters   = new ArrayList<>();
        this.context = context;

        // Load the assets
        importAssets(ASSET_FOLDER);
    }

    /**
     * How big is our sample pool?
     * @return Return the number of samples
     */
    private int size() {
        return 0;
    }

    /**
     * Store the path to each asset in the list
     */
    //TODO: Maybe instead of recursion, get ALL names of directories/files
    //TODO: and only add it to the assetPath list if it ends in '.jpg' ?
    private boolean getAssetPaths(String path) throws IOException {
        String [] list = context.getAssets().list(path);

        // Recurse through directories
        if (list.length > 0) {
            for (int i = 0; i < list.length; ++i) {
                if (!getAssetPaths(path + "/" + list[i]))
                    return false;
            }
        } else {
            // Add files
            assetPaths.add(path);
        }

    return true;
    }

    /**
     * Load all of the sample image assets
     */
    private void importAssets(String directory) {
        // If the asset manager doesn't load our assets, who will??
        AssetManager assetManager = this.context.getAssets();

        // Get the path to each asset
        try {
            getAssetPaths(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create an input stream
        InputStream inputStream = null;

        // Load each file referenced in the list of asset paths
        for (int i = 0; i < this.assetPaths.size(); ++i) {
            // Get the name of the current file
            String fileName = this.assetPaths.get(i);

            // Get the name of the character
            char characterName = fileName.charAt(11);

            Log.i(SAMPLE_POOL, "Filename: " + fileName);

            // Create an inputstream in order to open the asset
            try {
                inputStream = assetManager.open(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create a bitmap from the input stream, and add use it to create a new character
            // object
            Character character = new Character(BitmapFactory.decodeStream(inputStream));
            character.setName(characterName);
            character.setAscii((int)characterName);
            characters.add(character);
        }
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
        return this.characters;
    }


    /**
     * Used to grow our sample pool
     * @param newCharacter A new character that was correctly identified
     */
    public void addNewCharacter(Character newCharacter) {

    }
}
