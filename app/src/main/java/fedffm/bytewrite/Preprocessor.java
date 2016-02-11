package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.webkit.WebHistoryItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preprocessor {
    private static final String PREPROCESSOR = "Preprocessor";
    private static final double THRESHOLD = 0.6;
    private static boolean LOGGING_ENABLED = true;
    private static boolean DETAILED_LOGGING = false;

    /**
     * The goal is for each letter bitmap to have the same height and width
     * @param segment The bitmap image containing a single character
     * @return Returns the scaled bitmap image
     */
    private Bitmap scaleSegment(Bitmap segment) {
        return segment;
    }

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
                    Log.e(PREPROCESSOR, "Iterating column count to " + columnCount);
                Log.i(PREPROCESSOR, pixels + " pixels in column " + x);
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
        if (LOGGING_ENABLED) {
            Log.i(PREPROCESSOR, (characterCount + 1) +  " characters detected");
            Log.i(PREPROCESSOR, "Size of map: " + characterWidth.size());

            for (int i = 0; i <= characterCount; ++i) {
                Log.i(PREPROCESSOR, "-------------------------------------------------------------");
                Log.i(PREPROCESSOR, "-------------------------------------------------------------");
                Log.i(PREPROCESSOR, "Character number " + (i + 1) + ":");
                Log.i(PREPROCESSOR, "began at (x = " + characterX.get(i) + ", (y = " + characterY.get(i) + ")");
                Log.i(PREPROCESSOR, "was " + characterWidth.get(i) + " pixels wide");
                Log.i(PREPROCESSOR, "and " + characterHeight.get(i) + " pixels tall");
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
     * @return A list of strings (each string representing a pair of coordinates)
     *
     */
    //TODO: public only for testing purposes
    public static List<String> plinko(Bitmap bitmap) {
        // What are the dimensions of the bitmap?
        Log.i(PREPROCESSOR, "The dimensions of the segment are: " + bitmap.getWidth() + " x " + bitmap.getHeight());
        List<String> coordinates = new ArrayList<>();
        String currentCoordinates;

        // RIGHT TO LEFT
        // TOP TO BOTTOM
        //                <-----
        //               |
        //               v
        //         <-----
        //        |
        //        v
        //  <-----
        // |
        // v
        // Iterate through each column, beginning at the right-most column
        for (int x = bitmap.getWidth() - 1; x >= 0; --x) {

            // Make sure that when switching to the next column,
            // the row position is reset back to the first row
            int y = 0;

            // Because we had to reset back to the first row, don't
            // include any of the previous coordinates
            coordinates.clear();

            // Add the current position to the list of coordinates
            currentCoordinates = x + "," + y;
            coordinates.add(currentCoordinates);

            // Log our current position for debugging purposes
            if (DETAILED_LOGGING)
                Log.i(PREPROCESSOR, "Cursor position: " + currentCoordinates);

            // Starting in the current column, iterate the currentCoordinates as far down as we can
            while (y < bitmap.getHeight() && bitmap.getPixel(x, y) == Color.WHITE) {
                ++y;

                // If the currentCoordinates runs into a black pixel, move to the left
                // until we are back in whitespace
                if (y < bitmap.getHeight() && x > 0 && bitmap.getPixel(x, y) == Color.BLACK)
                    --x;

                // Add the current position to the list of coordinates
                currentCoordinates = x + "," + y;
                coordinates.add(currentCoordinates);

                // Log our current position for debugging purposes
                if (DETAILED_LOGGING)
                    Log.i(PREPROCESSOR, "Cursor position: " + currentCoordinates);

                // Return if the end of the bitmap is reached
                if (y == bitmap.getHeight()) {

                    // Log the fact that we found a path to the bottom of the segment
                    if (LOGGING_ENABLED) {
                        Log.i(PREPROCESSOR, "Final currentCoordinates position: " + currentCoordinates);
                        Log.i(PREPROCESSOR, "Great success!");
                    }

                    // What was the path?
                    for (int i = 0; i < coordinates.size(); ++i) {
                        // Parse the coordinates out
                        String[] parts = coordinates.get(i).split(",");
                        int xCoord = Integer.parseInt(parts[0]);
                        int yCoord = Integer.parseInt(parts[1]);

                        // Draw a line so that we can see where the cropping occurs
                        if (withinBounds(xCoord, yCoord, bitmap))
                            bitmap.setPixel(xCoord, yCoord, Color.RED);
                    }
                    return coordinates;
                }
            }
        }

        // RIGHT TO LEFT
        // BOTTOM TO TOP
        // <-----
        //       ^
        //       |
        //        <-----
        //              ^
        //              |
        //               <-----
        //                     ^
        //                     |
        // Iterate through each column, beginning at the right-most column
        for (int x = bitmap.getWidth() - 1; x >= 0; --x) {

            // Make sure that when switching to the next column,
            // the row position is reset back to the last row
            int y = bitmap.getHeight() - 1;

            // Because we had to reset back to the last row, don't
            // include any of the previous coordinates
            coordinates.clear();

            // Add the current position to the list of coordinates
            currentCoordinates = x + "," + y;
            coordinates.add(currentCoordinates);

            // Log our current position for debugging purposes
            if (DETAILED_LOGGING)
                Log.i(PREPROCESSOR, "Cursor position: " + currentCoordinates);

            // Starting in the current column, iterate the currentCoordinates as far down as we can
            while (y > 0 && bitmap.getPixel(x, y) == Color.WHITE) {
                --y;

                // If the currentCoordinates runs into a black pixel, move to the left
                // until we are back in whitespace
                if (y > 0 && x > 0 && bitmap.getPixel(x, y) == Color.BLACK)
                    --x;

                // Add the current position to the list of coordinates
                currentCoordinates = x + "," + y;
                coordinates.add(currentCoordinates);

                // Log our current position for debugging purposes
                if (DETAILED_LOGGING)
                    Log.i(PREPROCESSOR, "Cursor position: " + currentCoordinates);

                // Return if the top of the bitmap has been reached
                if (y == 0) {

                    // Log the fact that we found a path to the bottom of the segment
                    if (LOGGING_ENABLED) {
                        Log.i(PREPROCESSOR, "Final currentCoordinates position: " + currentCoordinates);
                        Log.i(PREPROCESSOR, "Great success!");
                    }

                    // What was the path?
                    for (int i = 0; i < coordinates.size(); ++i) {
                        // Parse the coordinates out
                        String[] parts = coordinates.get(i).split(",");
                        int xCoord = Integer.parseInt(parts[0]);
                        int yCoord = Integer.parseInt(parts[1]);

                        // Draw a line so that we can see where the cropping occurs
                        if (withinBounds(xCoord, yCoord, bitmap))
                            bitmap.setPixel(xCoord, yCoord, Color.RED);
                    }
                    return coordinates;
                }
            }
        }

        //    ^ ^ ^ ^ ^ ^ ^
        //    | | | | | | |
        //<---  | | | | | |
        //<-----  | | | | |
        //<-------  | | | |
        //<---------  | | |
        //<-----------  | |
        //<-------------  |
        //<---------------

        // TOP-LEFT:
        //    Attempt to extract a portion of the segment using a 90 degree angle

        boolean encounteredBlack = false;
        boolean traversedThroughBlack = false;
        int numBlackPixels = 0;

        List<Integer> xCoordinates = new ArrayList<>();
        List<Integer> yCoordinates = new ArrayList<>();

        // Begin iterating through the columns
        for (int x = 25; x < bitmap.getWidth(); ++x) {
            // Start with an empty list of y coordinates
            yCoordinates.clear();

            // Log our column position
            Log.i(PREPROCESSOR, "> " + x + ",0");

            // Move down the bitmap one row at a time
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                // Start with an empty list of x coordinates
                xCoordinates.clear();

                // Create a copy of our current column position so that
                // we don't mess up the outer-most loop
                int xCopy = x;

                // Log our row position
                Log.i(PREPROCESSOR, "v " + x + "," + y);
                yCoordinates.add(y);

                // If we encounter a black pixel while iterating down this column,
                // move on to the next column
                if (bitmap.getPixel(xCopy, y) == Color.BLACK) {
                    break;
                }

                // For each row that we move down, backtrack 'x' to the first column
                while (xCopy > 0 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    Log.i(PREPROCESSOR, "< " + xCopy + "," + y);
                    xCoordinates.add(xCopy);
                    --xCopy;
                }

                if (xCopy == 0 && bitmap.getPixel(xCopy, y) == Color.WHITE) {
                    xCoordinates.add(xCopy);

                    // Count the number of black pixels contained within the area
                    // that we've recorded
                    for (int i = 0; i < xCoordinates.size(); ++i) {
                        for (int j = 0; j < yCoordinates.size(); ++j) {
//                            bitmap.setPixel(xCoordinates.get(i), yCoordinates.get(j), Color.YELLOW);
                            if (bitmap.getPixel(xCoordinates.get(i), yCoordinates.get(j)) == Color.BLACK)
                                numBlackPixels++;
                        }
                    }

                    // Ensure that we've captured a character
                    if (numBlackPixels == 0)
                        break;

                    Log.i(PREPROCESSOR, "Great success!");

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
                    return coordinates;
                }
            }
        }
//
//        //    ^ ^ ^ ^ ^ ^ ^
//        //    | | | | | | |
//        //<---  | | | | | |
//        //<-----  | | | | |
//        //<-------  | | | |
//        //<---------  | | |
//        //<-----------  | |
//        //<-------------  |
//        //<---------------
//
//        // TOP-LEFT:
//        //    Attempt to extract a portion of the segment using a 90 degree angle
//
//        // Begin iterating through the columns
//        for (int x = 20; x < bitmap.getWidth(); ++x) {
//            // When we encounter a column of white pixels,
//            // we will need to change the value of x in order
//            // to iterate through the row and make sure that
//            // all of the pixels are white.
//            // We need a different variable so that we won't
//            // mess up the flow of the outer-most loop
//            int xCopy = x;
//            int y = xCopy;
//
//            // Start with a fresh list of coordinates
//            coordinates.clear();
//
//            // Try to make it to the top of the bitmap without
//            // encountering a black pixel
//            while (y > 0 && y < bitmap.getHeight()) {
//                // Add the current position to the list of coordinates
//                if (bitmap.getPixel(x, y) == Color.WHITE) {
//                    coordinates.add(x + "," + y);
//                    // Move up one row
//                    --y;
//                }
//                else
//                    break;
//            }
//
//            // If 'y' == 0, then we are at positioned at the very first row
//            // In other words, this is how we know that we've reached the
//            // top of the bitmap
//            if (y == 0) {
//                // Because the pixel in the top row isn't included in
//                // the previous while loop, we can't forget to add it
//                // to our list of coordinates
//                if (bitmap.getPixel(x, y) == Color.WHITE)
//                    coordinates.add(x + "," + y);
//
//                // Reset the position of y to be x
//                y = xCopy;
//
//                // Backtrace back through the columns and see whether or
//                // not the entire row is free of black pixels
//                while (xCopy > 0 && xCopy < bitmap.getWidth()) {
//                    if (bitmap.getPixel(xCopy, y) == Color.WHITE) {
//                        // Add the current position to the list of coordinates
//                        coordinates.add(xCopy + "," + y);
//                        // Move left one column
//                        --xCopy;
//                    }
//                    else
//                        break;
//                }
//
//                // If the value of 'xCopy' (x) is 0, then we are positioned in
//                // the very first column. In other words, we've reached the
//                // left-most edge of the bitmap
//                if (xCopy == 0) {
//                    // Because the pixel in the left column isn't included in
//                    // the previous while loop, we can't forget to add it
//                    // to our list of coordinates
//                    if (bitmap.getPixel(xCopy, y) == Color.WHITE)
//                        coordinates.add(xCopy + "," + y);
//
//                    // What was the path?
//                    for (int i = 0; i < coordinates.size(); ++i) {
//                        // Parse the coordinates out
//                        String[] parts = coordinates.get(i).split(",");
//                        int xCoord = Integer.parseInt(parts[0]);
//                        int yCoord = Integer.parseInt(parts[1]);
//
//                        // Draw a line so that we can see where the cropping occurs
//                        if (withinBounds(xCoord, yCoord, bitmap))
//                            bitmap.setPixel(xCoord, yCoord, Color.RED);
//                    }
//                    // What was the path?
//                    for (int i = 0; i < coordinates.size(); ++i) {
//                        // Parse the coordinates out
//                        String[] parts = coordinates.get(i).split(",");
//                        int xCoord = Integer.parseInt(parts[0]);
//                        int yCoord = Integer.parseInt(parts[1]);
//
//                        // Draw a line so that we can see where the cropping occurs
//                        if (withinBounds(xCoord, yCoord, bitmap))
//                            bitmap.setPixel(xCoord, yCoord, Color.RED);
//                    }
//                    return coordinates;
//                }
//            }
//        }


        //    ^ ^ ^ ^ ^ ^ ^
        //    | | | | | | |
        //    | | | | | |  --->
        //    | | | | |  ----->
        //    | | | |  ------->
        //    | | |  --------->
        //    | |  ----------->
        //    |  ------------->
        //     --------------->

        // TOP-RIGHT:
        //    Attempt to extract a portion of the segment using a 90 degree angle

        int margin = 45;

        // Begin iterating through the columns
        for (int x = bitmap.getWidth() - margin, y = margin; x >= 0; --x) {
            // When we encounter a column of white pixels,
            // we will need to change the value of x in order
            // to iterate through the row and make sure that
            // all of the pixels are white.
            // We need a different variable so that we won't
            // mess up the flow of the outer-most loop
            int xCopy = x;
            int yCopy = y;
            y++;

            // Log
            if (DETAILED_LOGGING)
              Log.i(PREPROCESSOR, "Starting iteration at position: " + x + "," + y);

            // Start with a fresh list of coordinates
            coordinates.clear();

            // Try to make it to the top of the bitmap without
            // encountering a black pixel
            if (DETAILED_LOGGING)
                Log.i(PREPROCESSOR, "Iterating up the column...");
            while (yCopy > 0 && yCopy < bitmap.getHeight()) {
                // Add the current position to the list of coordinates
                if (bitmap.getPixel(x, yCopy) == Color.WHITE) {
                    coordinates.add(x + "," + yCopy);

                    // Log
                    if (DETAILED_LOGGING)
                        Log.i(PREPROCESSOR, "^ (" + xCopy + "," + yCopy + ")");

                    // Move up one row
                    --yCopy;
                }
                else
                    break;
            }

            // If 'y' == 0, then we are at positioned at the very first row
            // In other words, this is how we know that we've reached the
            // top of the bitmap
            if (yCopy == 0) {
                // Because the pixel in the top row isn't included in
                // the previous while loop, we can't forget to add it
                // to our list of coordinates
                if (bitmap.getPixel(x, yCopy) == Color.WHITE)
                    coordinates.add(x + "," + yCopy);

                // Reset the position of yCopy to be y
                yCopy = y;

                // Backtrace back through the columns and see whether or
                // not the entire row is free of black pixels
                if (DETAILED_LOGGING)
                    Log.i(PREPROCESSOR, "Iterating through the row...");
                while (xCopy > 0 && xCopy < bitmap.getWidth() - 1) {
                    if (bitmap.getPixel(xCopy, yCopy) == Color.WHITE) {
                        // Add the current position to the list of coordinates
                        coordinates.add(xCopy + "," + yCopy);

                        if (DETAILED_LOGGING)
                        Log.i(PREPROCESSOR, "--> (" + xCopy + "," + yCopy + ")");
                        // Move right one column
                        ++xCopy;
                    }
                    else
                        break;
                }

                // If the value of 'xCopy' (x) is bitmap.getWidth() - 1, then we are
                // positioned at the very last column. In other words, we've reached the
                // right-most edge of the bitmap
                if (xCopy == bitmap.getWidth() - 1) {
                    // Because the pixel in the left column isn't included in
                    // the previous while loop, we can't forget to add it
                    // to our list of coordinates
                    if (bitmap.getPixel(xCopy, yCopy) == Color.WHITE)
                        coordinates.add(xCopy + "," + yCopy);

                    // What was the path?
                    for (int i = 0; i < coordinates.size(); ++i) {
                        // Parse the coordinates out
                        String[] parts = coordinates.get(i).split(",");
                        int xCoord = Integer.parseInt(parts[0]);
                        int yCoord = Integer.parseInt(parts[1]);

                        // Draw a line so that we can see where the cropping occurs
                        if (withinBounds(xCoord, yCoord, bitmap))
                            bitmap.setPixel(xCoord, yCoord, Color.RED);
                    }
                    // What was the path?
                    for (int i = 0; i < coordinates.size(); ++i) {
                        // Parse the coordinates out
                        String[] parts = coordinates.get(i).split(",");
                        int xCoord = Integer.parseInt(parts[0]);
                        int yCoord = Integer.parseInt(parts[1]);

                        // Draw a line so that we can see where the cropping occurs
                        if (withinBounds(xCoord, yCoord, bitmap))
                            bitmap.setPixel(xCoord, yCoord, Color.RED);
                    }
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * In the event that a word contains characters which are overlapping
     * or otherwise directly touching each other, this algorithm will attempt to
     * approximate the point at which to divide the segment in half
     * (in hopes of producing two characters)
     *
     *  _________________
     * |    |   |  |    |
     * |  W | O | RD    |
     *  -----------------
     *
     * @param bitmap The bitmap image containing the written word
     * @return A list of characters
     *
     */
    private static List<Character> divisionSegmentation(Bitmap bitmap) {
        List<Character> characters = new ArrayList<>();

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
                Log.i(PREPROCESSOR, "Scaled height: " + bitmap.getHeight() + " Scaled width: " + bitmap.getWidth());
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
     * @param b The greyscaled bitmap image
     * @return The binarized bitmap image
     */
    public static Bitmap binarize(Bitmap b) {
        for (int x = 0; x < b.getWidth(); x++) {
            for (int y = 0; y < b.getHeight(); y++) {
                int pixel = b.getPixel(x, y);

                if (shouldBeBlack(pixel))
                    b.setPixel(x, y, Color.BLACK);
                else
                    b.setPixel(x, y, Color.WHITE);
            }
        }
        return b;
    }

    /**
     * Crop out all of the excess white background
     * @param b The original bitmap
     * @return The cropped bitmap
     */
    public static Bitmap crop(Bitmap b) {
        // Coordinates
        int xFirst = 0;
        int xLast  = 0;
        int yFirst = b.getHeight();
        int yLast  = 0;


        // Get the column range
        for (int x = 0; x < b.getWidth(); ++x) {
            for (int y = 0; y < b.getHeight(); ++y) {
                if (b.getPixel(x, y) == Color.BLACK) {
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
            Log.i(PREPROCESSOR, "First column: " + xFirst);
            Log.i(PREPROCESSOR, "Last column:  " + xLast);
        }

        // Get the row range
        for (int x = xFirst; x < xLast; ++x) {
            for (int y = 0; y < b.getHeight(); ++y) {
                if (b.getPixel(x, y) == Color.BLACK) {
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
            Log.i(PREPROCESSOR, "First row: " + yFirst);
            Log.i(PREPROCESSOR, "Last row:  " + yLast);
        }

        // Calculate the dimensions of the subimage
        int x = xFirst;
        int y = yFirst;
        int w = xLast - xFirst;
        int h = yLast - yFirst;

        // Create the bitmap
        b = Bitmap.createBitmap(b, x, y, w, h);

        return b;
    }

    /**
     * Attempt to parse characters out of the bitmap image
     * @param bitmap The bitmap to be examined
     * @return A list of bitmaps, each representing an
     * unidentified Character
     */
    public static List<Character> segmentCharacters(Bitmap bitmap) {
        List<Character> characters;

        // Determine the layout of the word
        characters = preliminarySegmentation(bitmap);

        return characters;
    }

}