package fedffm.ribbit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preprocessor {
    private static final String LOG_TAG = "Preprocessor";
    private static boolean DETAILED_LOGGING = false;
    private static final double THRESHOLD = 0.6;

    /**
     * Determine whether a set of coordinates are within the bounds of a given bitmap
     * @param x the column in the bitmap
     * @param y the row in the bitmap
     * @param bitmap the image
     * @return False if the coordinates are out of range
     */
    private static boolean withinBounds(int x, int y, Bitmap bitmap) {
        return (x < bitmap.getWidth()  && x >= 0 && y < bitmap.getHeight() && y >= 0);
    }

    /**
     * Examine the bitmap containing a word and perform segmentation using the
     * whitespace inbetween each character. Calculate the dimensions of each
     * individual character, and use those dimensions to generate a bitmap
     * which will then be associated with an unidentified character
     *
     *  _________________
     * |    |   |   |    |
     * |  W | O | R | D  |
     *  -----------------
     *
     * @param bitmap The bitmap image containing the written word
     * @return Returns an list of unidentified characters (image segments)
     */
    private static List<Character> preliminarySegmentation(Bitmap bitmap) {
        List<Character> characters = new ArrayList<>();
        int pixels;
        int pixelsPrevious;
        int highestRow = bitmap.getHeight();
        int lowestRow = 0;
        boolean whiteSpace;
        boolean whiteSpacePrevious;
        boolean lastColumn;

        // Track the number of characters the word contains
        // as well as the number of columns (width) of each character
        Integer characterCount   = 0;
        Integer    columnCount   = 0;
        Map<Integer, Integer> characterX      = new HashMap<>();
        Map<Integer, Integer> characterY      = new HashMap<>();
        Map<Integer, Integer> characterWidth  = new HashMap<>();
        Map<Integer, Integer> characterHeight = new HashMap<>();

        // Iterate through each column
        for (int x = 0; x < bitmap.getWidth(); ++x) {

            // Is this the last column?
            lastColumn = x == bitmap.getWidth() - 1;

            // Current column
            pixels = 0;
            whiteSpace = true;

            // Previous column
            pixelsPrevious = 0;
            whiteSpacePrevious = false;

            // Iterate through each row
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                // If a black pixel is detected, increment the pixel count
                if (bitmap.getPixel(x, y) == Color.BLACK) {
                    // Any time we start scanning a new character, we need to store
                    // the x coordinate where the new character begins
                    if (characterX.size() == characterCount)
                        characterX.put(characterCount, x);

                    // Keep track of the highest point of the character
                    if (y < highestRow) {
                        highestRow = y;
                    }

                    // And the lowest point...
                    if (y > lowestRow)
                        lowestRow = y;

                    whiteSpace = false;
                    pixels++;
                } else
                    whiteSpace = true;


                // Count how many pixels the entire previous column had
                if (x > 0)
                    if (bitmap.getPixel((x - 1), y) == Color.BLACK) {
                        pixelsPrevious++;
                        whiteSpacePrevious = false;
                    } else
                        whiteSpacePrevious = true;

            }

            // The previous column contained one or more black pixels
            if (pixelsPrevious != 0)
                whiteSpacePrevious = false;

            // The current column contains one or more black pixels
            if (pixels != 0) {
                whiteSpace = false;
                columnCount++;
            }

            // Log pixel count and column count
            if (DETAILED_LOGGING) {
                if (columnCount != 0)
                    Log.e(LOG_TAG, "Iterating column count to " + columnCount);
                Log.i(LOG_TAG, pixels + " pixels in column " + x);
            }

            // If a column of white pixels is detected
            // AND
            // the previous column contained black pixels...
            // OR
            // If this was the last column...
            // then we have finished scanning a new character
            if (whiteSpace && !whiteSpacePrevious) {
                characterY.put(characterCount, highestRow);
                characterHeight.put(characterCount, lowestRow);
                characterWidth.put(characterCount, columnCount);
                characterCount++;
                columnCount = 0;
                lowestRow = 0;
                highestRow = bitmap.getHeight();
            } else if (lastColumn) {
                characterY.put(characterCount, highestRow);
                characterHeight.put(characterCount, lowestRow);
                characterWidth.put(characterCount, columnCount);
            }
        }

        // Log:
        //    1. # of characters
        //    2. Size of map (should match # of characters)
        //    3. Width of each character (obtained from map)
        if (DETAILED_LOGGING) {
            Log.i(LOG_TAG, (characterCount + 1) +  " characters detected");
            Log.i(LOG_TAG, "Size of map: " + characterWidth.size());

            for (int i = 0; i <= characterCount; ++i) {
                Log.i(LOG_TAG, "-------------------------------------------------------------");
                Log.i(LOG_TAG, "-------------------------------------------------------------");
                Log.i(LOG_TAG, "Character number " + (i + 1) + ":");
                Log.i(LOG_TAG, "began at (x = " + characterX.get(i) + ", (y = " + characterY.get(i) + ")");
                Log.i(LOG_TAG, "was " + characterWidth.get(i) + " pixels wide");
                Log.i(LOG_TAG, "and " + characterHeight.get(i) + " pixels tall");
            }
        }

        // Create a subimage for each separate character, create a new unidentified
        // Character object associated with each image, and add it to the list of
        // Characters
        for (int i = 0; i <= characterCount; ++i) {
            Bitmap b = Bitmap.createBitmap(bitmap,
                                           characterX.get(i),
                                           characterY.get(i),
                                           characterWidth.get(i),
                                           (characterHeight.get(i)) - characterY.get(i));

            Character c = new Character(b);
            characters.add(c);
        }
        return characters;
    }

    /**
     * This algorithm is applied to a word which contains characters that
     * are not directly touching each other, but characters that cross over
     * each other's vertical space, thus preventing vertical segmentation.
     *
     * An example might be the characters: "og" , in the event that the 'tail'
     * of the "g" (without touching) cross under the "o" character
     *
     * @param bitmap The bitmap image containing the written word
     * @return A map containing the necessary bitmap dimensions to create a sub image
     *
     */
    private static Map<String, Integer> getAdjacentCharacterDimensions(Bitmap bitmap) {
        // What are the dimensions of the bitmap?
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "The size of the segment is: " + bitmap.getWidth() + " x " + bitmap.getHeight());

        // Keep track of the dimensions
        Map<String, Integer> dimensions = new HashMap<>();

        //****************************************************************************************
        // TOP-LEFT:
        //    Attempt to extract a quadrilateral portion of the bitmap by
        //    beginning in the top-left corner and iterating to the right
        //    and iterating downward
        //****************************************************************************************
        //    ^ ^ ^ ^ ^ ^ ^
        //    | | | | | | |
        //<---  | | | | | |
        //<-----  | | | | |
        //<-------  | | | |
        //<---------  | | |
        //<-----------  | |
        //<-------------  |
        //<---------------

        // Keep track of the coordinates
        List<Integer> xCoordinates = new ArrayList<>();
        List<Integer> yCoordinates = new ArrayList<>();

        // Begin iterating through the columns
        for (int x = 0; x < bitmap.getWidth(); ++x) {
            // Every time we begin iterating down a new column, we
            // ought to start with an empty list of y coordinates
            yCoordinates.clear();

            // Log which column we are preparing to iterate down
            if (DETAILED_LOGGING)
                Log.e(LOG_TAG, "> " + x + ",0");

            // Move down the column one row at a time
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                // Each time we begin iterating back through a new row,
                // we should start with an empty list of x coordinates
                xCoordinates.clear();
                yCoordinates.add(y);

                // Create a copy of our current column position so that
                // we don't mess up the outer-most loop
                int xCopy = x;

                // Log which row we are preparing to back-trace through
                if (DETAILED_LOGGING)
                    Log.i(LOG_TAG, "v " + x + "," + y);

                // If we encounter a black pixel while iterating down this column,
                // simply move on to the next column
                if (bitmap.getPixel(xCopy, y) == Color.BLACK) {
                    if (DETAILED_LOGGING)
                        bitmap.setPixel(xCopy, y, Color.MAGENTA);
                    break;
                }

                // For each row that we move down, backtrack to the first column
                while (xCopy > 0 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    xCoordinates.add(xCopy);
                    --xCopy;
                }

                // If we are able to freely back-track all the way to the first column
                // (the left-most edge of the bitmap), make sure that we've captured
                // black pixels
                if (xCopy == 0 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    int numBlackPixels = 0;
                    xCoordinates.add(xCopy);

                    // Count the number of black pixels contained within the area
                    // that we've recorded
                    for (int i = 0; i < xCoordinates.size(); ++i) {
                        for (int j = 0; j < yCoordinates.size(); ++j) {
                            // For ever black pixel encountered within the area, increment the count
                            if (bitmap.getPixel(xCoordinates.get(i), yCoordinates.get(j)) == Color.BLACK) {
                                numBlackPixels++;
                                if (DETAILED_LOGGING)
                                    bitmap.setPixel(xCoordinates.get(i), yCoordinates.get(j), Color.DKGRAY);
                            }
                        }
                    }

                    // Calculate what percent of the bitmap's area is occupied by
                    // the pixels that have been captured
                    float areaOfSegment = bitmap.getWidth() * bitmap.getHeight();
                    float areaOfPixels = numBlackPixels / areaOfSegment * 100;

                    // Continue iterating if the section of the bitmap:
                    //    1. Doesn't contain any black pixels at all
                    //    2. Contains an extremely small amount of pixels
                    //       (Prevent the dot of the 'i' from being segmented
                    //        as a separate character)
                    if (numBlackPixels == 0 || areaOfPixels < 2.0)
                        continue;

                    if (DETAILED_LOGGING) {
                        Log.i(LOG_TAG, "There were " + numBlackPixels + " pixels in the captured area");
                        Log.i(LOG_TAG, "Great success!");
                        Log.i(LOG_TAG, "The captured pixels comprise " + areaOfPixels + "% of the image's area");

                        // Draw the column first
                        for (int i = 0; i < yCoordinates.size(); ++i) {
                            // Draw a line so that we can see where the cropping occurs
                            if (withinBounds(x, yCoordinates.get(i), bitmap))
                                bitmap.setPixel(x, yCoordinates.get(i), Color.RED);
                        }
                        // Now draw the row
                        for (int i = 0; i < xCoordinates.size(); ++i) {
                            // Draw a line so that we can see where the cropping occurs
                            if (withinBounds(xCoordinates.get(i), y, bitmap))
                                bitmap.setPixel(xCoordinates.get(i), y, Color.RED);
                        }
                    }

                    // Return the starting point of the sub-image (x, y)
                    // as well as the width (w), and the height (h)
                    dimensions.put("x", 0);
                    dimensions.put("y", 0);
                    dimensions.put("w", x);
                    dimensions.put("h", y);

                    return dimensions;
                }
            }
        }

        //******************************************************************************************
        // TOP-RIGHT:
        //    Attempt to extract a quadrilateral portion of the bitmap by
        //    beginning in the top-right corner and iterating to the left
        //    and iterating downward
        //******************************************************************************************
        //    ^ ^ ^ ^ ^ ^ ^
        //    | | | | | | |
        //    | | | | | |  --->
        //    | | | | |  ----->
        //    | | | |  ------->
        //    | | |  --------->
        //    | |  ----------->
        //    |  ------------->
        //     --------------->

        // Keep track of the coordinates
        xCoordinates = new ArrayList<>();
        yCoordinates = new ArrayList<>();

        // Begin iterating through the columns
        for (int x = bitmap.getWidth() - 1; x >= 0; --x) {
            // Every time we begin iterating down a new column, we
            // ought to start with an empty list of y coordinates
            yCoordinates.clear();

            // Log which column we are preparing to iterate down
            if (DETAILED_LOGGING)
                Log.e(LOG_TAG, "< " + x + ",0");

            // Move down the column one row at a time
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                // Each time we begin iterating back through a new row,
                // we should start with an empty list of x coordinates
                xCoordinates.clear();
                yCoordinates.add(y);

                // Create a copy of our current column position so that
                // we don't mess up the outer-most loop
                int xCopy = x;

                // Log which row we are preparing to back-trace through
                if (DETAILED_LOGGING)
                    Log.i(LOG_TAG, "v " + x + "," + y);

                // If we encounter a black pixel while iterating down this column,
                // simply move on to the next column
                if (bitmap.getPixel(xCopy, y) == Color.BLACK) {
                    if (DETAILED_LOGGING)
                        bitmap.setPixel(xCopy, y, Color.MAGENTA);
                    break;
                }

                // For each row that we move down, backtrack to the first column
                while (xCopy < bitmap.getWidth() - 1 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    // Log which row we are preparing to back-trace through
                    if (DETAILED_LOGGING)
                        Log.i(LOG_TAG, "> " + x + "," + y);

                    xCoordinates.add(xCopy);
                    ++xCopy;
                }

                // If we are able to freely back-track all the way to the first column
                // (the left-most edge of the bitmap), make sure that we've captured
                // black pixels
                if (xCopy == bitmap.getWidth() - 1 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    int numBlackPixels = 0;
                    xCoordinates.add(xCopy);

                    // Count the number of black pixels contained within the area
                    // that we've recorded
                    for (int i = 0; i < xCoordinates.size(); ++i) {
                        for (int j = 0; j < yCoordinates.size(); ++j) {
                            // For ever black pixel encountered within the area, increment the count
                            if (bitmap.getPixel(xCoordinates.get(i), yCoordinates.get(j)) == Color.BLACK) {
                                numBlackPixels++;
                                if (DETAILED_LOGGING)
                                    bitmap.setPixel(xCoordinates.get(i), yCoordinates.get(j), Color.DKGRAY);
                            }
                        }
                    }

                    // Calculate what percent of the bitmap's area is occupied by
                    // the pixels that have been captured
                    float areaOfSegment = bitmap.getWidth() * bitmap.getHeight();
                    float areaOfPixels = numBlackPixels / areaOfSegment * 100;

                    // Continue iterating if the section of the bitmap:
                    //    1. Doesn't contain any black pixels at all
                    //    2. Contains an extremely small amount of pixels
                    //       (Prevent the dot of the 'i' from being segmented
                    //        as a separate character)
                    if (numBlackPixels == 0 || areaOfPixels < 2.0)
                        continue;

                    if (DETAILED_LOGGING) {
                        Log.i(LOG_TAG, "There were " + numBlackPixels + " pixels in the captured area");
                        Log.i(LOG_TAG, "Great success!");
                        Log.i(LOG_TAG, "The captured pixels comprise " + areaOfPixels + "% of the image's area");

                        // Draw the column first
                        for (int i = 0; i < yCoordinates.size(); ++i) {
                            // Draw a line so that we can see where the cropping occurs
                            if (withinBounds(x, yCoordinates.get(i), bitmap))
                                bitmap.setPixel(x, yCoordinates.get(i), Color.RED);
                        }
                        // Now draw the row
                        for (int i = 0; i < xCoordinates.size(); ++i) {
                            // Draw a line so that we can see where the cropping occurs
                            if (withinBounds(xCoordinates.get(i), y, bitmap))
                                bitmap.setPixel(xCoordinates.get(i), y, Color.RED);
                        }
                    }

                    // Return the starting point of the sub-image (x, y)
                    // as well as the width (w), and the height (h)
                    dimensions.put("x", x);
                    dimensions.put("y", 0);
                    dimensions.put("w", bitmap.getWidth() - x);
                    dimensions.put("h", y);

                    return dimensions;
                }
            }
        }
        return dimensions;
    }

    /**
     * This algorithm is applied to a word which contains characters that
     * are not directly touching each other, but characters that cross over
     * each other's vertical space, thus preventing vertical segmentation.
     *
     * An example might be the characters: "og" , in the event that the 'tail'
     * of the "g" (without touching) cross under the "o" character
     *
     * @param original The bitmap image containing two characters
     * @return A list of two unidentified characters
     *
     */
    private static List<Character> precisionSegmentation(Bitmap original) {
        List<Character> characters = new ArrayList<>();

        // Start by getting the dimensions of one of the characters
        Map<String, Integer> dimensions = getAdjacentCharacterDimensions(original);

        // If the dimensions map is empty, the bitmap
        // cannot be precision-segmented
        if (dimensions.isEmpty())
            return characters;

            // Get the dimensions from the map
            int x = dimensions.get("x");
            int y = dimensions.get("y");
            int w = dimensions.get("w");
            int h = dimensions.get("h");

            // Prepare two new bitmaps that will be derived from the original
            Bitmap characterOne;
            Bitmap characterTwo;

            // Create the bitmap for the first character
            characterOne = Bitmap.createBitmap(original, x, y, w, h);

            // Now remove the character from the original bitmap
            for (int i = x; i < (x+ w); ++i)
                for (int j = y; j < (y + h); ++j)
                    original.setPixel(i, j, Color.WHITE);

            // Crop out the excess whitespace that was just created
            characterOne = crop(characterOne);
            characterTwo = crop(original);


            // Determine the order of the segmented characters
            if (x == 0) {
                characters.add(new Character(characterOne));
                characters.add(new Character(characterTwo));
            }
            else {
                characters.add(new Character(characterTwo));
                characters.add(new Character(characterOne));
            }
        return characters;
    }

    /**
     * This algorithm will attempt to approximate a
     * point at which to divide the segment in half
     * (in hopes of producing two characters)
     *
     *  ______              ___  ____
     * |  |  |   ------>   |  |  |  |
     * | RD  |   ------>   | R|  |D |
     *  ------             ----  ----
     *
     * @param bitmap The bitmap image containing the written word
     * @return A list of characters
     *
     */
    private static List<Character> divisionSegmentation(Bitmap bitmap) {
        List<Character> characters = new ArrayList<>();

        // Create the first sub-image
        int x = 0;
        int y = 0;
        int w = bitmap.getWidth() / 2;
        int h = bitmap.getHeight();
        characters.add(new Character(Bitmap.createBitmap(bitmap, x, y, w, h)));

        // Create the second sub-image
        x = bitmap.getWidth() / 2;
        y = 0;
        w = bitmap.getWidth() / 2;
        h = bitmap.getHeight();
        characters.add(new Character(Bitmap.createBitmap(bitmap, x, y, w, h)));

        return characters;
    }

    /**
     * Determine whether a pixel should be black or white
     * @param pixel The pixel in question
     * @return true if the pixel should be black
     */
    private static boolean shouldBeBlack(int pixel) {
        int alpha = Color.alpha(pixel);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);

        if(alpha == 0x00) // Transparent pixels will be white
            return false;

        // distance from the white extreme
        double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) +
                                   Math.pow(0xff - blueValue, 2) +
                                   Math.pow(0xff - greenValue, 2));

        // distance from the black extreme
        // this should not be computed and might be as well
        // a function of distanceFromWhite and the whole distance
        double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) +
                                   Math.pow(0x00 - blueValue, 2) +
                                   Math.pow(0x00 - greenValue, 2));

        // distance between the extremes
        // this is a constant that should not be computed
        double distance = distanceFromBlack + distanceFromWhite;

        // distance between the extremes
        return ((distanceFromWhite/distance) > THRESHOLD);
    }

    /**
     * Load the bitmap image
     * @param imagePath The path where the image is located
     * @return Returns the image
     */
    public static Bitmap load(String imagePath) {
        // Create a file for the bitmap
        File imageFile = new File(imagePath);
        Bitmap bitmap = null;

        // Scale the image down to 1/4, and generate the bitmap
        if (imageFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inMutable = true;
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            // Log the information
            if (DETAILED_LOGGING)
                Log.i(LOG_TAG, "Scaled height: " + bitmap.getHeight() + " Scaled width: " + bitmap.getWidth());
        }
        return bitmap;
    }

    /**
     * Convert a colored image to greyscale
     * @param source The original bitmap image
     * @return The greyscaled bitmap
     */
    public static Bitmap greyscale(Bitmap source) {
        // Get the dimensions
        int width  = source.getWidth();
        int height = source.getHeight();

        // Create a blank bitmap with the same dimensions and configuration as
        // the original
        Bitmap greyscaled = Bitmap.createBitmap(width, height, source.getConfig());

        // Make a canvas using the new blank bitmap
        Canvas c = new Canvas(greyscaled);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        // Create a no-saturation filter
        cm.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);

        // Draw the bitmap onto the canvas using the original image and the new filter
        c.drawBitmap(source, 0, 0, paint);

        return greyscaled;
    }

    /**
     * Convert all pixels to either black or white
     * @param bitmap The greyscaled bitmap image
     * @return The binarized bitmap image
     */
    public static Bitmap binarize(Bitmap bitmap) {
        // Iterate through the bitmap
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {

                // Determine whether the pixel should be black
                // or white
                if (shouldBeBlack(bitmap.getPixel(x, y)))
                    bitmap.setPixel(x, y, Color.BLACK);
                else
                    bitmap.setPixel(x, y, Color.WHITE);
            }
        }
        return bitmap;
    }

    /**
     * Crop out all of the excess white background
     * @param bitmap The original bitmap
     * @return The cropped bitmap
     */
    public static Bitmap crop(Bitmap bitmap) {
        // Coordinates
        int xFirst = 0;
        int xLast  = 0;
        int yFirst = bitmap.getHeight();
        int yLast  = 0;


        // Get the column range
        for (int x = 0; x < bitmap.getWidth(); ++x) {
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                if (bitmap.getPixel(x, y) == Color.BLACK) {
                    // Get the first column
                    if (xFirst == 0)
                        xFirst = x;

                    // And the last column
                    xLast = x;
                }
            }
        }

        // Log the info
        if (DETAILED_LOGGING) {
            Log.i(LOG_TAG, "First column: " + xFirst);
            Log.i(LOG_TAG, "Last column:  " + xLast);
        }

        // Get the row range
        for (int x = xFirst; x < xLast; ++x) {
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                if (bitmap.getPixel(x, y) == Color.BLACK) {
                    // Find the top-most black pixel
                    if (y < yFirst)
                        yFirst = y;

                    // And the bottom-most pixel
                    if (y > yLast)
                        yLast = y;
                }
            }
        }

        // Log the info
        if (DETAILED_LOGGING){
            Log.i(LOG_TAG, "First row: " + yFirst);
            Log.i(LOG_TAG, "Last row:  " + yLast);
        }

        // Calculate the dimensions of the subimage
        int x = xFirst;
        int y = yFirst;
        int w = xLast - xFirst;
        int h = yLast - yFirst;

        // Create the bitmap
        bitmap = Bitmap.createBitmap(bitmap, x, y, w, h);

        return bitmap;
    }

    /**
     * Attempt to parse characters out of the bitmap image
     * @param bitmap The bitmap to be examined
     * @return A list of bitmaps, each representing an
     * unidentified Character
     */
    public static List<Character> segmentCharacters(Bitmap bitmap) {
        // Get the initial set of segments, and keep track of their sizes
        List<Character> characters = new ArrayList<>();
        List<Float> segmentSizes = new ArrayList<>();

        // Attempt preliminary segmentation
        try {
            characters = preliminarySegmentation(bitmap);
        } catch (IllegalArgumentException iae){
            Log.e(LOG_TAG, "Error: Unable to segment image.");
            iae.printStackTrace();
        }

        // Variables we will use in order to track the average segment size
        int   numSegments = 0;
        float sum = 0;
        float averageSegmentSize;

        // Examine each segment and determine the size of each
        for (int i = 0; i < characters.size(); ++i) {
            // Compute sum of each segment size, and keep track
            // of the number of segments
            sum += characters.get(i).sizeValue();
            numSegments++;

            // Add the size of each segment to a list
            segmentSizes.add(characters.get(i).sizeValue());
        }

        // Get the average segment size
        averageSegmentSize = sum / numSegments;
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "Average segment size:    " + averageSegmentSize + '\n');

        // What constitutes an "unusually big" segment size?
        float sizeThreshold = (float)(averageSegmentSize * 1.75);
        if (DETAILED_LOGGING)
            Log.i(LOG_TAG, "Size threshold:          " + sizeThreshold);

        // Are any of the segments unusually big? If so, the segment
        // probably contains more than one character
        for (int i = 0; i < segmentSizes.size(); ++i) {
            // If an unusually big segment is detected, we pass it off to
            // be precision-segmented, and update the list of characters
            // with the newly-returned sub-segments
            if (segmentSizes.get(i) > sizeThreshold) {
                if (DETAILED_LOGGING)
                    Log.e(LOG_TAG, "The size of segment " + (i + 1) + " is " + segmentSizes.get(i));

                // Perform precision segmentation on the large segment
                List <Character> segmentedCharacters = precisionSegmentation(characters.get(i).getBitmap());

                // Detect unsuccessful precision segmentation
                if (segmentedCharacters.isEmpty()) {
                    Log.e(LOG_TAG, "Error: precision segmentation failed");
                    continue;
                }

                // Because we are turning one segment into two smaller segments,
                // we need to update our list of characters and segment sizes
                characters.remove(i);
                segmentSizes.remove(i);

                // Replace the large segment with the precision-segmented sub-segments
                for (int j = 0; j < segmentedCharacters.size(); ++j) {
                    characters.add((i + j), segmentedCharacters.get(j));
                    segmentSizes.add((i + j), characters.get(j).sizeValue());
                }
            }
            else if (DETAILED_LOGGING)
                Log.i(LOG_TAG, "The size of segment " + (i + 1) + " is " + segmentSizes.get(i));
        }
        if (DETAILED_LOGGING) {
            Log.i(LOG_TAG, "-------------------------------------------------------------");
            Log.i(LOG_TAG, "-------------------------------------------------------------");
        }
        return characters;
    }
}