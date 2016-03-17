package fedffm.ribbit;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dictionary {
    private static final String LOG_TAG = "Dictionary";
    private List<String> words;


    // Singleton (there is no need for multiple instances of the same dictionary)
    private static Dictionary instance = null;

    // Prevent instantiation outside of this class
    private Dictionary(Context context) {
        this.words = new ArrayList<>();
        this.load(context);
    }

    // Return the instance of this class
    public static Dictionary getInstance(Context context) {
        if (instance == null)
            instance = new Dictionary(context);
        return instance;
    }

    /**
     * How many words are in our dictionary
     * @return Number of words in the text file
     */
    public int size() {
        return this.words.size();
    }

    /**
     * Return a random word from the dictionary
     * @return the string that was chosen at random
     */
    public String getRandomWord() {
        return this.words.get(new Random().nextInt(this.size()));
    }

    /**
     * Load the dictionary from a text file
     */
    private void load(Context context) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.dictionary)));
            String line = br.readLine();

            while (line != null) {
                this.words.add(line);
                line = br.readLine();
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 0 : Exact match
     * 1 : There is one character that differs between the words
     * 2 : Two characters differ between the two words
     *
     * @param unknownWord The word that we are attempting to identify
     * @return The lower the number, the more similar the words are
     */
    private int getDistance(String unknownWord, String dictionaryWord) {
        int numDifferences = 0;


        for (int i = 0; i < dictionaryWord.length(); ++i)
            if (dictionaryWord.charAt(i) != unknownWord.charAt(i))
                numDifferences++;


        return numDifferences;
    }

    /**
     * Search the dictionary for the string that most closely matches a given word
     * @param unknownWord The word that we are attempting to identify
     * @return Return whichever word in our dictionary is closest to the parameter
     */
    public String getClosestMatch(String unknownWord) {
        String closestMatch = null;
        int distance;
        int smallestDistance = unknownWord.length();

        // Which word in the dictionary is the unidentified word most similar to?
        for (int i = this.words.size() - 1; i >= 0; --i) {
            String word = this.words.get(i);

            if (word.length() != unknownWord.length())
                continue;

            distance = this.getDistance(unknownWord, word);

            if (distance < smallestDistance) {
                smallestDistance = distance;
                closestMatch = word;
            }
        }

        Log.i(LOG_TAG, "Original word:     " + unknownWord);

        if (smallestDistance == 0) {
            Log.i(LOG_TAG, "Exact match found: " + closestMatch + ": " + smallestDistance);
            return closestMatch;
        }

        else if (smallestDistance <= unknownWord.length() / 3) {
            Log.i(LOG_TAG, "Closest match found: " + closestMatch + ": " + smallestDistance);
            return closestMatch;
        }
        else {
            Log.i(LOG_TAG, "No close matches were found.");
            return unknownWord;
        }
    }
}
