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

public class Preprocessor {
    private static final String PREPROCESSOR = "Preprocessor";
    private static final double THRESHOLD = 0.75;

    /**
     * The goal is for each letter bitmap to have the same height and width
     * @param segment The bitmap image containing a single character
     * @return Returns the scaled bitmap image
     */
    private Bitmap scaleSegment(Bitmap segment) {
        return segment;
    }

    /**
     * Use white background pixels to find a path from the top
     * of the image to the bottom
     * @param word The bitmap image containing the written word
     * @return Returns a representation of the coordinates that
     * comprise the shortest path
     */
    private String findShortestPath(Bitmap word) {
        return "";
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
     *
     * @param greyscaled
     * @return
     */
    public static Bitmap binarize(Bitmap greyscaled) {
        for (int x = 0; x < greyscaled.getWidth(); x++) {
            for (int y = 0; y < greyscaled.getHeight(); y++) {
                int pixel = greyscaled.getPixel(x, y);

                if (shouldBeBlack(pixel))
                    greyscaled.setPixel(x, y, Color.BLACK);
                else
                    greyscaled.setPixel(x, y, Color.WHITE);
            }
        }
        return greyscaled;
    }

}
