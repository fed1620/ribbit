package fedffm.bytewrite;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CharacterBase {
    // Log tag
    private final static String CHARACTER_BASE = "CharacterBase";
    private final static String ASSET_FOLDER = "characters";

    // The list of known characters
    private List<Character> characters;
    private Context context;

    public CharacterBase(Context context) {
        // Initialize the member variables
        this.characters = new ArrayList<>();
        this.context    = context;

        // Load the assets
        createCharacters(ASSET_FOLDER);
    }

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
        String[] folders = context.getAssets().list(directory);
        String[] files;

        // Loop through all the folders in the directory
        for (String folder : folders) {
            files = context.getAssets().list(directory + "/" + folder);
            // Loop through all the files in each folder
            for (String file : files) {
                assetPaths.add(directory + "/" + folder + "/" + file);
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
            character.setAscii((int)characterName);
            addNewCharacter(character);
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
    private int size() { return this.characters.size(); }
}
