package fedffm.bytewrite;

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
    private static final String PREPROCESSOR = "Preprocessor";
    private static final double THRESHOLD = 0.575;
    private static boolean DETAILED_LOGGING = true;

    /**
     * The goal is for each letter bitmap to have the same height and width
     * @param segment The bitmap image containing a single character
     * @return Returns the scaled bitmap image
     */
    private Bitmap scaleSegment(Bitmap segment) {
        return segment;
    }

    /**
     * Examine the bitmap containing a word, and attempt to determine
     * whether:
     *    0. Characters are cleanly separated by vertical whitespace
     *    1. Characters are crossing over or under each other without touching
     *    2. Characters are overlapping or touching
     *
     * @param bitmap The bitmap image containing the written word
     * @return Returns an integer representing the type of word detected
     *              0: Cleanly separated
     *              1: Crossing over/under
     *              2: Overlapping/touching
     */
    private static int detectCharacterDistribution(Bitmap bitmap) {
        int pixels;
        boolean whiteSpace;
        int pixelsPrevious;
        boolean whiteSpacePrevious;

        // Track the number of characters the word contains
        // as well as the number of columns (width) of each character
        Integer characterCount = 1;
        Integer    columnCount = 0;
        Map<Integer, Integer> characterWidths = new HashMap<>();

        // Iterate through each column
        for (int x = 0; x < bitmap.getWidth(); ++x) {

            // Current column
            pixels = 0;
            whiteSpace = false;

            // Previous column
            pixelsPrevious = 0;
            whiteSpacePrevious = false;


            // Iterate through each row
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                // If a black pixel is detected, increment the pixel count
                if (bitmap.getPixel(x, y) == Color.BLACK) {
                    pixels++;
                    whiteSpace = false;
                } else {
                    whiteSpace = true;
                }

                // Do the same for the previous column
                if (x > 0)
                    if (bitmap.getPixel((x - 1), y) == Color.BLACK) {
                        pixelsPrevious++;
                        whiteSpacePrevious = false;
                    } else {
                        whiteSpacePrevious = true;
                    }
            }

            // How many pixels did the column contain in total?
            if (DETAILED_LOGGING)
                Log.i(PREPROCESSOR, pixels + " pixels in column " + x);


            // The previous column contained one or more black pixels
            if (pixelsPrevious !=0)
                whiteSpacePrevious = false;

            // The current column contains one or more black pixels
            if (pixels != 0) {
                whiteSpace = false;
                columnCount++;
                Log.e(PREPROCESSOR, "Iterating column count to " + columnCount);
            }


            // If a column with black pixels is detected,
            // AND
            // the previous column contained only white pixels,
            // then a new character has been scanned
            if (!whiteSpace && whiteSpacePrevious) {
                characterWidths.put(characterCount, columnCount);
                characterCount++;
                Log.e(PREPROCESSOR, "Iterating character count to " + characterCount);
                columnCount = 0;
            }
        }

        // How many total characters were detected?
        Log.i(PREPROCESSOR, characterCount + " characters detected");

        // How wide was each character?
        int width;

        for (int i = 1; i <= characterWidths.size(); ++i) {
            Log.i(PREPROCESSOR, "Size of map: " + characterWidths.size());
            Log.i(PREPROCESSOR, "Width: " + characterWidths.get(i));
            width = characterWidths.get(i);
            Log.i(PREPROCESSOR, "Character number " + (i) + " was " + width + " pixels wide ");
        }

        return 0;
    }

    /**
     * This algorithm relies on the fact that each letter is cleanly
     * separated by at least one column that is comprised entirely of
     * white pixels.
     *
     *  _________________
     * |    |   |   |    |
     * |  W | O | R | D  |
     *  -----------------
     *
     *
     * @param bitmap The bitmap image containing the written word
     * @return A list of characters
     *
     */
    private static List<Character> verticalSegmentation(Bitmap bitmap) {
        List<Character> characters = new ArrayList<>();

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
     * @return A list of characters
     *
     */
    private static List<Character> precisionSegmentation(Bitmap bitmap) {
        List<Character> characters = new ArrayList<>();

        return characters;

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
        double distanceFromWhite = Math.sqrt(Math.pow(0xff - redValue, 2) + Math.pow(0xff - blueValue, 2) + Math.pow(0xff - greenValue, 2));
        // distance from the black extreme //this should not be computed and might be as well a function of distanceFromWhite and the whole distance
        double distanceFromBlack = Math.sqrt(Math.pow(0x00 - redValue, 2) + Math.pow(0x00 - blueValue, 2) + Math.pow(0x00 - greenValue, 2));
        // distance between the extremes //this is a constant that should not be computed :p
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
        List<Character> characters = new ArrayList<>();

        // Determine the layout of the word
        int layout = detectCharacterDistribution(bitmap);
        Log.i(PREPROCESSOR, "The word scanned is type " + layout);


        return characters;
    }

}
