package fedffm.ribbit;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterBase {
    // Singleton (so that we only have to instantiate
    // the character base one time
    private static CharacterBase instance = null;

    // Prevent instantiation outside of this class
    private CharacterBase(Context context) {
        // Initialize the member variables
        this.characters = new ArrayList<>();
        this.context    = context;

        // Load the assets
        createCharacters(ASSET_FOLDER);
    }

    // Return the instance of this class
    public static CharacterBase getInstance(Context context) {
        if (instance == null)
            instance = new CharacterBase(context);
        return instance;
    }

    // Folder that contains all character samples
    private final static String  ASSET_FOLDER = "characters/";
    private final static String  LOG_TAG      = "CharacterBase";
    private final static boolean LOGGING_ENABLED = true;
    private final static int     FEATURE_TYPE = 6;
    private final static int     A_ASCII      = 97;
    private final static int     Z_ASCII      = 122;
    private final static int     NUM_FILES    = 20;

    // The list of known characters
    private List<Character> characters;
    private Context context;

    /**
     * Store the path to each asset in the list
     * @param directory The parent directory of the assets
     * @return List of Strings: each String is a full path to an image
     * @throws IOException
     */
    private List<String> getAssetPaths(String directory) throws IOException {
        // Store the path to each asset as a String
        List<String> assetPaths = new ArrayList<>();

        // Get the array of folders and the array of files for each folder
        for (int i = A_ASCII; i <= Z_ASCII; ++i) {
            String path = directory + (char)i + "/";
            for (int j = 1; j <= NUM_FILES; ++j) {
                assetPaths.add(path + j + ".jpg");
            }
        }
        return assetPaths;
    }

    /**
     * Use all of the asset images to create the characters that will
     * comprise our character base
     * @param directory The parent directory of the assets
     */
    private void createCharacters(String directory) {
        // If the asset manager doesn't load our assets, who will??
        AssetManager assetManager = this.context.getAssets();
        List<String> assetPaths = new ArrayList<>();

        // Get the path to each asset
        try {
            assetPaths = getAssetPaths(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create an input stream
        InputStream inputStream = null;

        Map<java.lang.Character, Integer> featureTypes = new HashMap<>();
        int count = 0;

        // Load each file referenced in the list of asset paths
        for (int i = 0; i < assetPaths.size(); ++i) {

            // Get the name of the current file
            String fileName = assetPaths.get(i);

            // Get the name of the character
            char characterName = fileName.charAt(11);

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
            character.setAscii((int) characterName);
            addNewCharacter(character);

            if (character.getFeatureClass() == FEATURE_TYPE) {
                if (featureTypes.containsKey(characterName))
                    count++;
                else
                    count = 1;

                featureTypes.put(characterName, count);
            }
        }


        if (LOGGING_ENABLED) {
            Log.i(LOG_TAG, "FEATURE_TYPE " + FEATURE_TYPE + " characters: ");
            for (char character : featureTypes.keySet()) {
                Log.i(LOG_TAG, character + ": " + featureTypes.get(character));
            }
        }
    }

    /**
     * Load all character samples
     * @return All character samples that exist in the sample pool
     */
    public List<Character> getAllCharacterSamples() { return this.characters; }

    /**
     * Load all character samples for a single given character
     * @param name The name of the character
     * @return The sample characters associated with the specified character
     */
    public List<Character> getCharacterSamples(char name) {
        List<Character> subList = new ArrayList<>();

        // Create a new list of Characters that only includes
        // the specified character type
        for (int i = 0; i < this.size(); ++i) {
            if (name == this.characters.get(i).getName())
                subList.add(this.characters.get(i));
        }
        return subList;
    }

    /**
     * Used to grow our sample pool
     * @param newCharacter A new character that was correctly identified
     */
    public void addNewCharacter(Character newCharacter) { this.characters.add(newCharacter); }

    /**
     * How big is our sample pool?
     * @return Return the number of samples
     */
    public int size() { return this.characters.size(); }
}
