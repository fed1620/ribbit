package fedffm.bytewrite;

import android.content.Context;
import android.graphics.Bitmap;
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
        assert (sampleCharacter.getWidth()  == unknownCharacterScaled.getWidth());
        assert (sampleCharacter.getHeight() == unknownCharacterScaled.getHeight());
        int width  = sampleCharacter.getWidth();
        int height = sampleCharacter.getHeight();

        // Store how many pixels there are and how many matching pixels there are
        int pixels = width * height;
        int matchingPixels = 0;

        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y) {
                if (sampleCharacter.getPixel(x, y) == unknownCharacterScaled.getPixel(x, y))
                    matchingPixels++;
            }
        Log.i(LOG_TAG, "Out of " + pixels + " pixels, " + matchingPixels + " are a match");
        Log.i(LOG_TAG, "Similarity: " + ((float)matchingPixels / (float)pixels));
        return ((float)matchingPixels / (float)pixels);
    }

    /**
     * Identify a character
     * @param unknown The character to be identified
     * @return The character updated with a name and ASCII code
     */
    public static Character identify(Character unknown, Context context) {
        // Start by getting the entire character base
        List<Character> characters = CharacterBase.getInstance(context).getAllCharacterSamples();
        Bitmap unknownScaled;

        // Use these to find the greatest similarity
        float similarity         = (float)0.0;
        float greatestSimilarity = (float)0.0;

        // Compare against each of them
        for (int i = 0; i < characters.size(); ++i) {
            unknownScaled = matchSize(characters.get(i).getBitmap(), unknown.getBitmap());
            similarity = computeSimilarity(characters.get(i).getBitmap(), unknownScaled);

            // Which of the two character bitmaps are most similar?
            if (similarity > greatestSimilarity)
                greatestSimilarity = similarity;
        }

        //TODO: This returns the very last similarity score, not the greatest...
        Log.i(LOG_TAG, "Greatest similarity: " + (similarity * 100.00) + "%");
        return new Character();
    }

}
