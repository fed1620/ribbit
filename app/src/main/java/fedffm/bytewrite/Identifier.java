package fedffm.bytewrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.InputMismatchException;
import java.util.List;

public class Identifier {
    private final static String LOG_TAG = "Identifier";
    private final static int    A_ASCII = 97;
    private final static int    Z_ASCII = 122;

    /**
     * In order to more accurately compare the unknown character against any given
     * sample character, scale the unknown character to be the exact same size as the sample
     * character
     * @param sample The bitmap for a known character from our CharacterBase
     * @param unknown The bitmap for the character we are trying to identify
     * @return A bitmap of the unknown character with the same dimensions of the sample character
     */
    private static Bitmap matchSize(Character sample, Character unknown) {
        // Get the dimensions of the sample bitmap
        int w = sample.getBitmap().getWidth();
        int h = sample.getBitmap().getHeight();

        // Create a bitmap of the unknown character using the dimensions of the sample
        return Bitmap.createScaledBitmap(unknown.getBitmap(), w, h, false);
    }

    /**
     * How does the width and height of the unidentified character compare with
     * the width and height of the known sample character
     * @param sample A known character from our CharacterBase
     * @param unknown The character we are attempting to identify
     * @return A float representing how similar the dimensions are
     */
    private static float dimensionalSimilarity(Character sample, Character unknown) {

        // 0.5: The character is twice as tall as it is wide
        // 1.0: The character's width and height are exactly the same
        // 2.0: The character is twice as wide as it is tall
        float sampleRatio  = (float)sample.getWidth()  / (float)sample.getHeight();
        float unknownRatio = (float)unknown.getWidth() / (float)unknown.getHeight();

        // 1.0 == The dimension ratios of each character is exactly the same
        return Math.min(sampleRatio, unknownRatio) / Math.max(sampleRatio, unknownRatio);
    }

    /**
     *
     * @param sample A known character from our CharacterBase
     * @param unknown The character we are attempting to identify
     * @return A float representing the similarity of the character's area between
     * both the unknown and the sample character
     */
    private static float areaSimilarity(Character sample, Character unknown) {
        // Get the reference to the bitmaps
        Bitmap sampleBitmap  = sample.getBitmap();
        Bitmap unknownBitmap = unknown.getBitmap();

        // How many character pixels each Character has
        float pixelsSample  = 0;
        float pixelsUnknown = 0;

        // Calculate the area of both bitmaps
        float area = (float)sampleBitmap.getWidth()  * (float)sampleBitmap.getHeight();

        // Find out how many character pixels each bitmap contains
        for (int x = 0; x < sampleBitmap.getWidth(); ++x)
            for (int y = 0; y < sampleBitmap.getHeight(); ++y) {
                if (sampleBitmap.getPixel(x, y)  == Color.BLACK)
                    ++pixelsSample;
                if (unknownBitmap.getPixel(x, y) == Color.BLACK)
                    ++pixelsUnknown;
            }

        // How much of the bitmap's area is occupied by character pixels?
        float areaSample  = pixelsSample / area;
        float areaUnknown = pixelsUnknown / area;

        // 1.0 == The character pixels of each character occupies the exact same of
        //        their respective bitmaps
        return Math.min(areaSample, areaUnknown) / Math.max(areaSample, areaUnknown);
    }

    /**
     *
     * @param sample A known character from our CharacterBase
     * @param unknown The character we are attempting to identify
     * @return A float representing how closely the pixels of each character match
     */
    private static float pixelDistributionSimilarity(Character sample, Character unknown) {
        // Get the reference to the bitmaps
        Bitmap sampleBitmap  = sample.getBitmap();
        Bitmap unknownBitmap = unknown.getBitmap();

        // Ensure that the two bitmaps are the exact same size. This ensures that
        // we do not go out of bounds
        if (sampleBitmap.getWidth()  != unknownBitmap.getWidth() ||
            sampleBitmap.getHeight() != unknownBitmap.getHeight())
            throw new InputMismatchException("Error comparing bitmaps: Bitmaps are not the same size");

        // Keep track of pixels:
        //    1. pixelsUnknown:  How many (black) pixels the unidentified character contains
        //    2. pixelsSample:   How many (black) pixels the sample character contains
        //    3. pixelsMatching: How many (black) pixels are shared by BOTH characters
        float pixelsUnknown  = 0;
        float pixelsSample   = 0;
        float pixelsMatching = 0;

        // Iterate through the bitmap
        for (int x = 0; x < sampleBitmap.getWidth(); ++x)
            for (int y = 0; y < sampleBitmap.getHeight(); ++y) {
                // Pixels encountered in the same location on both bitmaps
                // contributes to the overall accuracy score
                if (unknownBitmap.getPixel(x, y) == Color.BLACK &&
                        sampleBitmap.getPixel(x, y) == Color.BLACK) {
                    pixelsUnknown++;
                    pixelsSample++;
                    pixelsMatching++;
                }

                // Look for black pixels on an individual basis
                else {
                    if (unknownBitmap.getPixel(x, y) == Color.BLACK)
                        pixelsUnknown++;
                    if (sampleBitmap.getPixel(x, y) == Color.BLACK)
                        pixelsSample++;
                }
            }

        // 1.0 == The character pixels of each character are exactly aligned
        return pixelsMatching / pixelsSample;
    }

    /**
     * Walk through each of the bitmaps and see which pixels match, versus which do not.
     * @param sample a known character from our CharacterBase
     * @param unknown the character we are trying to identify.
     * @return A percentage representing how similar the characters are
     */
    private static float similarity(Character sample, Character unknown) {
        // Similarity array
        float [] similarity = {
                                dimensionalSimilarity(sample, unknown)       * 100,
                                areaSimilarity(sample, unknown)              * 100 ,
                                pixelDistributionSimilarity(sample, unknown) * 100
                              };

        // Combine these values to produce a "similarity score"
        float similarityScore = ((similarity[0] + similarity[1] + similarity[2]) / 3);

//        Log.i(LOG_TAG, "unknown w: " + unknown.getWidth() + " unknown h: " + unknown.getHeight());
//        Log.i(LOG_TAG, "sample w:  " + sample.getWidth()  + " sample h:  " + sample.getHeight());
//        Log.i(LOG_TAG, "dimensionalSimilarity:       " + similarity[0]);
//        Log.i(LOG_TAG, "areaSimilarity:              " + similarity[1]);
//        Log.i(LOG_TAG, "pixelDistributionSimilarity: " + similarity[2]);
//        Log.i(LOG_TAG, "similarityScore:             " + similarityScore);
//        Log.i(LOG_TAG, "============================================================================");
//        Log.i(LOG_TAG, "============================================================================");

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
        float greatestSimilarity = (float)0.0;
        float greatestAverage    = (float)0.0;
        float greatestCombined   = (float)0.0;

        // Indexs to keep track of the greatest similarities
        int   iGreatest         = 0;
        int   iGreatestAverage  = 0;
        int   iGreatestCombined = 0;

        // Compare our unknown character against every character
        for (int i = A_ASCII; i <= Z_ASCII; ++i) {
            // Get the full list of samples for each different type of character/letter
            characterBase = CharacterBase.getInstance(context).getCharacterSamples((char)i);

            // Keep track of the similarity scores for each character
            float bestSimilarityCurrentChar = (float)0.0;
            float sum = 0;

            // Iterate through each sample in the list for the current character
            for (int j = 0; j < characterBase.size(); ++j) {
//                Log.i(LOG_TAG, "character:             " + (char)i);
//                Log.i(LOG_TAG, "------------------------");

                // Compare the bitmap of the unknown character against the current sample
                unknown.setBitmap(matchSize(characterBase.get(j), unknown));
                float similarity = similarity(characterBase.get(j), unknown);

                // Keep track of the best similarity for the current character
                if (similarity > bestSimilarityCurrentChar)
                    bestSimilarityCurrentChar = similarity;

                // Compute the average similarity for the given character
                sum += similarity;
            }

            // Calculate the average similarity for the character we finished iterating through
            float averageSimilarity  = sum / (float)characterBase.size();
            float combinedSimilarity = (averageSimilarity + bestSimilarityCurrentChar) / (float)2.0;

//            Log.i(LOG_TAG, "character:             " + (char)i);
//            Log.i(LOG_TAG, "-----------------------");
//            Log.i(LOG_TAG, "best for current char: " + bestSimilarityCurrentChar);
//            Log.i(LOG_TAG, "average :              " + averageSimilarity);
//            Log.i(LOG_TAG, "combined:              " + combinedSimilarity);
//            Log.i(LOG_TAG, "-----------------------");
//            Log.i(LOG_TAG, "-----------------------");

            // Which character has the greatest similarity
            if (bestSimilarityCurrentChar > greatestSimilarity) {
                greatestSimilarity = bestSimilarityCurrentChar;
                iGreatest = i;
            }

            // Which character has the greatest average similarity
            if (averageSimilarity > greatestAverage) {
                greatestAverage = averageSimilarity;
                iGreatestAverage = i;

            }

            // Which character has the greatest similarity total (average + highest)
            if (combinedSimilarity > greatestCombined) {
                greatestCombined = combinedSimilarity;
                iGreatestCombined = i;
            }
        }

        // Log
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "Greatest similarity:          " + (char)iGreatest + " - " + greatestSimilarity);
        Log.i(LOG_TAG, "Greatest average similarity:  " + (char)iGreatestAverage + " - " + greatestAverage);
        Log.i(LOG_TAG, "Greatest combined similarity: " + (char)iGreatestCombined + " - " + greatestCombined);
        Log.i(LOG_TAG, "============================================================================");
        Log.i(LOG_TAG, "============================================================================");

        // Set the character name and ascii code
        unknown.setName((char)iGreatestCombined);
        unknown.setAscii(iGreatestCombined);

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
