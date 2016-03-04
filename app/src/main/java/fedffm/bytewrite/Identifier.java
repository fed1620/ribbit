package fedffm.bytewrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.List;

public class Identifier {
    private final static String LOG_TAG = "Identifier";
    private final static int    A_ASCII = 97;
    private final static int    Z_ASCII = 122;

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
    private static float similarity(Bitmap sampleCharacter, Bitmap unknownCharacterScaled) {
        // Ensure that the two bitmaps are the exact same size. This ensures that
        // we do not go out of bounds
        if (sampleCharacter.getWidth() != unknownCharacterScaled.getWidth() ||
            sampleCharacter.getHeight() != unknownCharacterScaled.getHeight())
                return (float)0.0;

        // Store width, height, and area for later reference
        int width  = sampleCharacter.getWidth();
        int height = sampleCharacter.getHeight();
        int area   = width * height;

        // Keep track of pixels:
        //    1. pixelsUnknown:  How many (black) pixels the unidentified character contains
        //    2. pixelsSample:   How many (black) pixels the sample character contains
        //    3. pixelsMatching: How many (black) pixels are shared by BOTH characters
        int pixelsUnknown  = 0;
        int pixelsSample   = 0;
        int pixelsMatching = 0;

        // Iterate through the bitmap
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y) {
                // Pixels encountered in the same location on both bitmaps
                // contributes to the overall accuracy score
                if (unknownCharacterScaled.getPixel(x, y) == Color.BLACK &&
                    sampleCharacter.getPixel(x, y) == Color.BLACK) {
                    pixelsUnknown++;
                    pixelsSample++;
                    pixelsMatching++;
                }

                // Look for black pixels on an individual basis
                else {
                    if (unknownCharacterScaled.getPixel(x, y) == Color.BLACK)
                        pixelsUnknown++;
                    if (sampleCharacter.getPixel(x, y) == Color.BLACK)
                        pixelsSample++;
                }
            }

        // Calculate what percentage of the total bitmap is occupied
        // by (black) pixels
        float areaUnknown     = (float)pixelsUnknown / (float)area;
        float areaSample      = (float)pixelsSample /  (float)area;
        float areaSimilarity  = Math.min(areaSample, areaUnknown) / Math.max(areaSample, areaUnknown);
        float similarityScore = ((float)pixelsMatching / Math.max((float)pixelsSample, (float)pixelsUnknown)) * areaSimilarity;

        Log.i(LOG_TAG, "pixelsUnknown:  " + pixelsUnknown);
        Log.i(LOG_TAG, "pixelsSample:   " + pixelsSample);
        Log.i(LOG_TAG, "pixelsMatching: " + pixelsMatching);
        Log.i(LOG_TAG, "pixelsMatching / pixelsSample  = " + (float)pixelsMatching / (float)pixelsSample);
        Log.i(LOG_TAG, "pixelsMatching / pixelsUnknown = " + (float)pixelsMatching / (float)pixelsUnknown);
        Log.i(LOG_TAG, ".");
        Log.i(LOG_TAG, "area:            " + area);
        Log.i(LOG_TAG, "areaUnknown:     " + areaUnknown);
        Log.i(LOG_TAG, "areaSample:      " + areaSample);
        Log.i(LOG_TAG, "areaSimilarity:  " + areaSimilarity);
        Log.i(LOG_TAG, "similarityScore: " + similarityScore);
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "============================================================================");

        //   0.0 == the two characters are completely different
        // 100.0 == the two characters are an identical match
        return similarityScore;
    }

    /**
     * Identify a single character
     * @param unknown The character to be identified
     * @return The character updated with a name and ASCII code
     */
    public static Character identify(Character unknown, Context context) {
        // Start by getting the entire character base
        List<Character> characterBase;

        // For all character samples...
        float greatestSimilarity        = (float)0.0;
        float greatestTotalSimilarity   = (float)0.0;
        float greatestAverageSimilarity = (float)0.0;

        // Indexs to keep track of the greatest similarities
        int   iGreatest = 0;
        int   iGreatestAverage = 0;
        int   iGreatestTotal = 0;

        // Compare our unknown character against every character
        for (int i = A_ASCII; i <= Z_ASCII; ++i) {
            // Get the full list of samples for each different type of character/letter
            characterBase = CharacterBase.getInstance(context).getCharacterSamples((char)i);

            // Keep track of the similarity scores for each character
            float bestSimilarityCurrentChar = (float)0.0;
            float sum = 0;

            // Iterate through each sample in the list for the current character
            for (int j = 0; j < characterBase.size(); ++j) {
                // Compare the bitmap of the unknown character against the current sample
                Bitmap unknownScaled = matchSize(characterBase.get(j).getBitmap(), unknown.getBitmap());
                Log.i(LOG_TAG, "character:             " + (char)i);
                Log.i(LOG_TAG, "-----------------------");
                float similarity = similarity(characterBase.get(j).getBitmap(), unknownScaled);

                // Keep track of the best similarity for the current character
                if (similarity > bestSimilarityCurrentChar)
                    bestSimilarityCurrentChar = similarity;

                // Compute the average similarity for the given character
                sum += similarity;
            }

            // Calculate the average similarity for the character we finished iterating through
            float averageSimilarity = sum / (float)characterBase.size();
            float totalSimilarity   = (averageSimilarity + bestSimilarityCurrentChar) / (float)2.0;

//            Log.i(LOG_TAG, "character:             " + (char)i);
//            Log.i(LOG_TAG, "-----------------------");
//            Log.i(LOG_TAG, "best for current char: " + bestSimilarityCurrentChar);
//            Log.i(LOG_TAG, "average:               " + averageSimilarity);
//            Log.i(LOG_TAG, "total:                 " + totalSimilarity);
//            Log.i(LOG_TAG, "-----------------------");
//            Log.i(LOG_TAG, "-----------------------");

            // Which character has the greatest similarity
            if (bestSimilarityCurrentChar > greatestSimilarity) {
                greatestSimilarity = bestSimilarityCurrentChar;
                iGreatest = i;
            }

            // Which character has the greatest average similarity
            if (averageSimilarity > greatestAverageSimilarity) {
                greatestAverageSimilarity = averageSimilarity;
                iGreatestAverage = i;

            }

            // Which character has the greatest similarity total (average + highest)
            if (totalSimilarity > greatestTotalSimilarity) {
                greatestTotalSimilarity = totalSimilarity;
                iGreatestTotal = i;
            }
        }

        // Log
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "Greatest similarity:         " + (char)iGreatest);
        Log.i(LOG_TAG, "Greatest average similarity: " + (char)iGreatestAverage);
        Log.i(LOG_TAG, "Greatest total similarity:   " + (char)iGreatestTotal);
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "============================================================================");

        // Set the character name and ascii code
        unknown.setName((char)iGreatestTotal);
        unknown.setAscii(iGreatestTotal);

        return unknown;
    }

    /**
     * Identify a word
     * @param unknownWord The Word to be identified
     * @return A Word consisting of identified characters
     */
    public static Word identify(Word unknownWord, Context context) {
        // Instantiate what will be our word
        Word word = new Word();

        // Examine each character in the word
        for (Character unknownCharacter : unknownWord.getCharacters()) {
            // Identify each character and add it to our word
            word.addCharacter(identify(unknownCharacter,context));
        }
        return word;
    }
}
