package fedffm.ribbit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.List;

public class Identifier {
    private final static boolean LOGGING_ENABLED = true;
    private final static boolean DETAILED_LOGGING = false;
    private final static String LOG_TAG = "Identifier";
    private final static int    A_ASCII = 97;
    private final static int    Z_ASCII = 122;

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
        float sampleRatio  = (float)sample.getBitmap().getWidth()  / (float)sample.getBitmap().getHeight();
        float unknownRatio = (float)unknown.getBitmap().getWidth() / (float)unknown.getBitmap().getHeight();

        // 1.0 == The dimension ratios of each character is exactly the same
        return Math.min(sampleRatio, unknownRatio) / Math.max(sampleRatio, unknownRatio);
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

        // Get the smaller dimensions
        int smallerWidth  = Math.min(sampleBitmap.getWidth() , unknownBitmap.getWidth());
        int smallerHeight = Math.min(sampleBitmap.getHeight(), unknownBitmap.getHeight());

        // Keep track of pixels:
        //    1. pixelsUnknown:  How many (black) pixels the unidentified character contains
        //    2. pixelsSample:   How many (black) pixels the sample character contains
        //    3. pixelsMatching: How many (black) pixels are shared by BOTH characters
        float pixelsUnknown  = 0;
        float pixelsSample   = 0;
        float pixelsMatching = 0;

        // Iterate through the bitmap
        for (int x = 0; x < smallerWidth; ++x)
            for (int y = 0; y < smallerHeight; ++y) {
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
        // Similarity measurements
        float dimensionalSimilarity       = dimensionalSimilarity(sample, unknown) * 100;
        float pixelDistributionSimilarity = pixelDistributionSimilarity(sample, unknown) * 100;

        // Apply weights
//        dimensionalSimilarity       *= .5;
//        pixelDistributionSimilarity *= .9;

        // Combine these values to produce a "similarity score"
        float similarityScore = (dimensionalSimilarity + pixelDistributionSimilarity) / 2;

        if (DETAILED_LOGGING) {
            Log.i(LOG_TAG, "unknown w: " + unknown.getBitmap().getWidth() + " unknown h: " + unknown.getBitmap().getHeight());
            Log.i(LOG_TAG, "sample w:  " + sample.getBitmap().getWidth() + " sample h:  " + sample.getBitmap().getHeight());
            Log.i(LOG_TAG, "dimensionalSimilarity:       " + dimensionalSimilarity);
            Log.i(LOG_TAG, "pixelDistributionSimilarity: " + pixelDistributionSimilarity);
            Log.i(LOG_TAG, "similarityScore:             " + similarityScore);
            Log.i(LOG_TAG, "============================================================================");
        }

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

        // How many samples total did we compare against?
        int sampleCount;
        int totalSampleCount = 0;

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

            sampleCount = 0;

            // Keep track of the similarity scores for each character
            float bestSimilarityCurrentChar = (float)0.0;
            float sum = 0;

            // Iterate through each sample in the list for the current character
            for (int j = 0; j < characterBase.size(); ++j) {
                // Compare the bitmap of the unknown character against the current sample
                if (unknown.getRatioClass()   != characterBase.get(j).getRatioClass() ||
                    unknown.getFeatureClass() != characterBase.get(j).getFeatureClass())
                    continue;

                // Log which character
                if (DETAILED_LOGGING) {
                    Log.i(LOG_TAG, "character:             " + (char)i);
                    Log.i(LOG_TAG, "------------------------");
                }

                float similarity = similarity(characterBase.get(j), unknown);

                // Keep track of the best similarity for the current character
                if (similarity > bestSimilarityCurrentChar)
                    bestSimilarityCurrentChar = similarity;

                // Compute the average similarity for the given character
                sum += similarity;
                sampleCount++;
                totalSampleCount++;
            }

            if (sampleCount == 0)
                continue;

            // Calculate the average similarity for the character we finished iterating through
            float averageSimilarity  = sum / (float)sampleCount;
            float combinedSimilarity = (averageSimilarity + bestSimilarityCurrentChar) / (float)2.0;

            if (LOGGING_ENABLED) {
                Log.i(LOG_TAG, "character:             " + (char)i);
                Log.i(LOG_TAG, sampleCount + " total samples");
                Log.i(LOG_TAG, "-----------------------");
                Log.i(LOG_TAG, "best for current char: " + bestSimilarityCurrentChar);
                Log.i(LOG_TAG, "average :              " + averageSimilarity);
                Log.i(LOG_TAG, "combined:              " + combinedSimilarity);
                Log.i(LOG_TAG, "-----------------------");
            }

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

        // Which was the most commonly occuring measurement?
        int index;

        if (iGreatest == iGreatestAverage || iGreatest == iGreatestCombined)
            index = iGreatest;
        else
            index = iGreatestCombined;


        // Set the character name and ascii code
        unknown.setName((char)index);
        unknown.setAscii(index);

        // Log
        if (LOGGING_ENABLED) {
            Log.i(LOG_TAG, "============================================================================");
            Log.i(LOG_TAG, "============================================================================");
            Log.i(LOG_TAG, "Greatest similarity:          " + (char)iGreatest + " - " + greatestSimilarity);
            Log.i(LOG_TAG, "Greatest average similarity:  " + (char)iGreatestAverage + " - " + greatestAverage);
            Log.i(LOG_TAG, "Greatest combined similarity: " + (char)iGreatestCombined + " - " + greatestCombined);
            Log.i(LOG_TAG, "character " + unknown.getName() + " was compared against " + totalSampleCount + " samples");
            Log.i(LOG_TAG, "============================================================================");
            Log.i(LOG_TAG, "============================================================================");
        }
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
