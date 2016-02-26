package fedffm.bytewrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.List;

public class Identifier {
    private final static String LOG_TAG = "Identifier";

    /**
     * In order to more accurately compare the unknown character against any given
     * sample character, scale the unknown character to be the exact same size as the sample
     * character
     * @param sampleCharacter The bitmap for a known character from our CharacterBase
     * @param unknownCharacter The bitmap for the character we are trying to identify
     * @return A bitmap of the unknown character with the same dimensions of the sample character
     */
    private static Bitmap matchSize(Bitmap sampleCharacter, Bitmap unknownCharacter) {
        // Get the dimensions of the sample bitmap
        int w = sampleCharacter.getWidth();
        int h = sampleCharacter.getHeight();

        // Create a bitmap of the unknown character using the dimensions of the sample
        return Bitmap.createScaledBitmap(unknownCharacter, w, h, false);
    }

    /**
     * Walk through each of the bitmaps and see which pixels match, versus which do not.
     * @param sampleCharacter The bitmap for a known character from our CharacterBase
     * @param unknownCharacterScaled The bitmap for the character we are trying to identify. This
     *                               must be scaled to the exact same size as the bitmap of the
     *                               sample character
     * @return A percentage representing how similar the bitmaps are.
     */
    private static float computeSimilarity(Bitmap sampleCharacter, Bitmap unknownCharacterScaled) {
        // Ensure that the two bitmaps are the exact same size. This is critical
        if (sampleCharacter.getWidth() != unknownCharacterScaled.getWidth() ||
            sampleCharacter.getHeight() != unknownCharacterScaled.getHeight())
                return (float)0.0;

        // Store the width and height in a variable for easy reference
        int width  = sampleCharacter.getWidth();
        int height = sampleCharacter.getHeight();

        // Store how many pixels there are and how many matching pixels there are
        int blackPixels = 0;
        int matchingPixels = 0;

        // Iterate through the bitmap
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y) {
                // How many black pixels are in the sample bitmap?
                if (sampleCharacter.getPixel(x, y) == Color.BLACK) {
                    blackPixels++;
                    // If the unknown character also has a black pixel at the
                    // same location, we have found a match!
                    if  (unknownCharacterScaled.getPixel(x, y) == Color.BLACK)
                        matchingPixels++;
                }
            }

        //   0.0 == the two characters are completely different
        // 100.0 == the two characters are an identical match
        return ((float)matchingPixels / (float)blackPixels) * (float)100.00;
    }

    /**
     * Identify a single character
     * @param unknown The character to be identified
     * @return The character updated with a name and ASCII code
     */
    public static Character identify(Character unknown, Context context) {
        // Start by getting the entire character base
        List<Character> characterBase = CharacterBase.getInstance(context).getAllCharacterSamples();

        // Use these to find the greatest similarity
        float similarity;
        float greatestSimilarity = (float)0.0;
        int matchIndex = 0;

        // Compare our unknown character against every character in the CharacterBase
        for (int i = 0; i < characterBase.size(); ++i) {
            Bitmap unknownScaled = matchSize(characterBase.get(i).getBitmap(), unknown.getBitmap());
            similarity = computeSimilarity(characterBase.get(i).getBitmap(), unknownScaled);

            // Which of the two character bitmaps are most similar?
            if (similarity > greatestSimilarity) {
                greatestSimilarity = similarity;
                matchIndex = i;
            }
        }

        Log.i(LOG_TAG, "Greatest similarity: " + characterBase.get(matchIndex).getName() +
                " with a score of : " + (greatestSimilarity) + "%");
        return characterBase.get(matchIndex);
    }

    /**
     * Identify a character
     * @param unknownWord The Word to be identified
     * @return A Word consisting of identified characters
     */
    public static Word identify(Word unknownWord, Context context) {
        // Start by getting the reference to the entire character base
        List<Character> characterBase = CharacterBase.getInstance(context).getAllCharacterSamples();

        // Instantiate what will be our word
        Word word = new Word();

        // Examine each character in the word
        for (Character unknownCharacter : unknownWord.getCharacters()) {
            // Use these to find the greatest similarity
            float similarity;
            float greatestSimilarity = (float)0.0;
            int   matchIndex = 0;

            // Compare our unknown character against every character in the CharacterBase
            for (int i = 0; i < characterBase.size(); ++i) {
                Bitmap unknownScaled = matchSize(characterBase.get(i).getBitmap(), unknownCharacter.getBitmap());
                similarity = computeSimilarity(characterBase.get(i).getBitmap(), unknownScaled);

                // Which of the two character bitmaps are most similar?
                if (similarity > greatestSimilarity) {
                    greatestSimilarity = similarity;
                    matchIndex = i;
                }
            }
            // Add each character to the word we are building
            word.addCharacter(characterBase.get(matchIndex));

            // Log the similarity score for each character
            Log.i(LOG_TAG, "Greatest similarity: " + characterBase.get(matchIndex).getName() +
                           " with a score of : " + (greatestSimilarity) + "%");
        }
        return word;
    }
}
