package fedffm.ribbit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Character {
    private static final String LOG_TAG = "Character";
    private static final boolean LOGGING_ENABLED = false;

    private char    name;
    private int     ascii;
    private int     ratioClass;
    private int     featureClass;
    private Bitmap  bitmap;


    // Default
    public Character() {
        this.name         = '?';
        this.bitmap       = null;
    }

    // Non-default
    public Character(Bitmap bitmap) {
        this.name         = '?';
        this.bitmap       = bitmap;
        this.featureClass = -1;
        this.ratioClass   = -1;
        this.determineRatioClass();
        this.determineFeatureClass();
    }

    // Setters
    public  void setName(char name)       {this.name = name;}
    public  void setAscii(Integer ascii)  {this.ascii = ascii;}
    public  void setBitmap(Bitmap bitmap) {this.bitmap = bitmap;}

    // Getters
    public char    getName()         {return this.name;}
    public Integer getAscii()        {return this.ascii;}
    public Bitmap  getBitmap()       {return this.bitmap;}
    public int     getRatioClass()   {return this.ratioClass;}
    public int     getFeatureClass() {return this.featureClass;}


    /**
     * Is the character wide and short, tall and thin, or well-rounded?
     *          0: The character is taller than it is wide
     *          1: The character is relatively even
     *          2: The character is wider than it is tall
     */
    private void determineRatioClass() {
        float ratio  = (float)this.bitmap.getWidth() / (float)this.bitmap.getHeight();

        if (ratio > 0 && ratio < 0.80)
            this.ratioClass = 0;
        else if (ratio > 0.80 && ratio < 1.30)
            this.ratioClass = 1;
        else if (ratio > 1.30)
            this.ratioClass = 2;
    }

    /**
     * What are certain attributes of the character?
     *
     *  0: enclosed space - round
     *      a, e, o
     *
     *  1: enclosed space - pointing up
     *      b, d
     *
     *  2: enclosed space - pointing down
     *      g, p, q
     *
     *  3: disconnect
     *      i, j
     *
     *  4: intersection
     *      f, t
     *
     *  5: open top
     *      u, v, w, y, [k, x]
     *
     *  6: open bottom
     *      n, h, m
     *
     *  7: open right
     *      c, r
     *
     *  8: 'criss-cross'
     *      k, x
     *
     *  9: diagonalism
     *      s, z
     *
     *  10: solid line
     *      l
     */
    private void determineFeatureClass() {
        // Use this to record how many black pixels are in each row
        int []  rowWidths  = new int[bitmap.getHeight()];

        // Map all of the pixels
        Map <Integer, List<Integer>> pixels = new HashMap<>();
        this.mapPixels(rowWidths, pixels);

        // Determine which feature class the character falls under
        boolean disconnect = this.isDisconnect(rowWidths);
        boolean intersect  = this.isIntersect(rowWidths);
        boolean openTop    = this.hasOpenTop(pixels);

        // Assign feature class accordingly
        if (disconnect)
            this.featureClass = 3;
        else if (intersect)
            this.featureClass = 4;
        else if (openTop)
            this.featureClass = 5;
    }

    /**
     * Get the information for the number of pixels
     * in each row
     * @param rowWidths The width (in black pixels) of each row of the character
     * @param pixels A map of each row and the pixels contained in each row
     */
    private void mapPixels(int[] rowWidths, Map<Integer, List<Integer>> pixels) {
        for (int y = 0; y < bitmap.getHeight(); ++y) {
            int numBlackPixelsInRow = 0;
            List<Integer> xCoordinates = new ArrayList<>();

            // Iterate across the row from left to right
            for (int x = 0; x < bitmap.getWidth(); ++x) {
                boolean pixelIsBlack = bitmap.getPixel(x, y) < Color.rgb(25, 25, 25);
                if (pixelIsBlack) {
                    numBlackPixelsInRow++;
                    xCoordinates.add(1);
                }
                else {
                    xCoordinates.add(0);
                }
            }
            // Store how wide each row is in an array
            rowWidths[y] = numBlackPixelsInRow;
            pixels.put(y, xCoordinates);
        }
    }

    /**
     * Output an ASCII map of the character's pixels
     * @param pixels Each entry represents a row of black and white pixels
     */
    private void debug(Map<Integer, List<Integer>> pixels) {
        for (int i = 0; i < pixels.size(); ++i) {
            // Print out ASCII art for the intersect characters
            String row = "";
            for (int j = 0; j < pixels.get(i).size(); ++j) {
                row += pixels.get(i).get(j);
            }

            String rowMarker;

            if (i < 10)
                rowMarker = "row  " + i + ": ";
            else
                rowMarker = "row " + i + ": ";

            Log.i(LOG_TAG, rowMarker + row);
        }
        Log.i(LOG_TAG, "=========================================================================");
        Log.i(LOG_TAG, "=========================================================================");
    }

    /**
     * Does the character contain any area of white pixels that are completely
     * surrounded by black pixels? The characters that should meet this criteria are:
     *      1. a
     *      2. e
     *      3. o
     *
     *                     BLACK PIXELS BLACK PIXELS BLACK PIXELS
     *                 BlACK PIXELS --> WHITE PIXELS --> BLACK PIXELS
     *          BLACK PIXELS ---------> WHITE PIXELS ---------> BLACK PIXELS
     *  BLACK PIXELS -----------------> WHITE PIXELS-----------------> BLACK PIXELS
     *          BLACK PIXELS ---------> WHITE PIXELS ---------> BLACK PIXELS
     *                 BlACK PIXELS --> WHITE PIXELS --> BLACK PIXELS
     *                     BLACK PIXELS BLACK PIXELS BLACK PIXELS
     *
     * @return True if the character contains enclosed whitespace
     */
    private boolean hasEnclosedSpace() {
        return false;
    }

    /**
     * Is the character disconnected at any point? The characters that should
     * meet this criteria are:
     *      1. i
     *      2. j
     *
     * @param rowWidths The width (in black pixels) of each row of the character
     * @return True if the character is a disconnect character
     */
    private boolean isDisconnect(int [] rowWidths) {
        for (int rowWidth : rowWidths)
            if (rowWidth == 0)
                return true;
        return false;
    }

    /**
     * Is the character intersected at any point? The characters that should
     * meet this criteria are:
     *      1. f
     *      2. t
     *
     * @param rowWidths The width (in black pixels) of each row of the character
     * @return True if the character is an intersect character
     */
    private boolean isIntersect(int [] rowWidths) {
        boolean possibleIntersectChar = false;
        float   widthMultiplier       = 3.25f;
        int     numSuddenChanges      = 0;
        int     indexLastWidthChange  = 0;

        for (int i = 1; i < rowWidths.length; ++i) {
            int currentRow = rowWidths[i];

            // How many sudden changes in width are there?
            if (currentRow >= (rowWidths[i -1] + 4)) {
                if (indexLastWidthChange == 0 || i >= indexLastWidthChange + 15) {
                    indexLastWidthChange = i;
                    numSuddenChanges++;
                }
            }


            // Is the current row unusually "wide" or not?
            if (i < 12 || i > rowWidths.length - 13)
                continue;

            boolean wideRow = (rowWidths[i] >= (rowWidths[i - 12] * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i - 11] * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i - 10] * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i - 9]  * widthMultiplier) &&

                    rowWidths[i] >= (rowWidths[i + 9]  * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i + 10] * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i + 11] * widthMultiplier) &&
                    rowWidths[i] >= (rowWidths[i + 12] * widthMultiplier));

            if (wideRow)
                possibleIntersectChar = true;
        }
        // Character should suddenly widen in no more than 2 areas of the bitmap
        return possibleIntersectChar && numSuddenChanges < 3;
    }

    /**
     * Is the top of the character "open" ? The characters that should meet
     * this criteria are:
     *      1. u
     *      2. v
     *      3. w
     *      4. y
     *
     * @param pixels Each entry represents a row of black and white pixels
     * @return True if the character has an open top
     */
    private boolean hasOpenTop(Map<Integer, List<Integer>> pixels) {
        List<Integer> xCoordinates = new ArrayList<>();
        List<Integer> yCoordinates = new ArrayList<>();
        int topLeftBlackPixelX  = 0;
        int topLeftBlackPixelY  = 0;
        int topRightBlackPixelX = 0;
        int topRightBlackPixelY = 0;

        // Each iteration is a row in the bitmap
        for (int y = 0; y < pixels.size(); ++y) {
            boolean startingPointReached = false;

            // Each iteration is a pixel in the current row
            for (int x = 0; x < pixels.get(y).size(); ++x) {

                // If we are more than halfway across the bitmap...
                if (x >= pixels.get(y).size()/2 && topRightBlackPixelX == 0 && topRightBlackPixelY == 0) {

                    // If the top-right black pixel has not been set..
                    if (pixels.get(y).get(x) == 1) {
                        // Find the top-right black pixel
                        if ((x == pixels.get(y).size() - 1)) {
                            topRightBlackPixelX = x;
                            topRightBlackPixelY = y;
                        }
                        else if (pixels.get(y).get(x + 1) == 0) {
                            topRightBlackPixelX = x;
                            topRightBlackPixelY = y;
                        }
                    }
                    continue;
                }
                else if (x >= pixels.get(y).size() / 2)
                    break;

                // Find the top-left black pixel
                if ((topLeftBlackPixelX == 0 && topLeftBlackPixelY == 0) && pixels.get(y).get(x) == 1) {
                    topLeftBlackPixelX = x;
                    topLeftBlackPixelY = y;
                }

                // Record our starting point
                if (!startingPointReached         &&
                    pixels.get(y).get(x)     == 1 &&
                    pixels.get(y).get(x + 1) == 0 &&
                    pixels.get(y).get(x + 2) == 0) {
                    startingPointReached = true;
                }

                if (!startingPointReached)
                    continue;

                // Ensure the top-left-most and top-right-most pixels of the character are not
                // wide enough apart, return false
                float margin = pixels.get(y).size() / 4.25f;
                if (topLeftBlackPixelX >= margin || topRightBlackPixelX <= pixels.get(y).size() - margin)
                    return false;

                // As long as the current pixel is white, move down
                while (y < pixels.size() - 1 && x < pixels.get(y).size() - 1 && pixels.get(y).get(x) == 0) {
                    y++;

                    // If we run into a black pixel, move to the right
                    if (pixels.get(y).get(x) == 1) {
                        xCoordinates.add(x);
                        yCoordinates.add(y);
                        x++;
                    }
                }
            }
        }

        // Find where our marker left off
        if (!xCoordinates.isEmpty() && !yCoordinates.isEmpty()) {
            int xPosition = xCoordinates.get(xCoordinates.size() - 1);
            int yPosition = yCoordinates.get(yCoordinates.size() - 1);

            if (xPosition < topLeftBlackPixelX || xPosition > topRightBlackPixelX)
                return false;

            // Attempt to find a clean route back up to the top of the bitmap
            for (int i = yPosition - 1; i >= 0; i--)
                if (pixels.get(i).get(xPosition) == 1)
                    return false;
        }
        else
            return false;

        if (LOGGING_ENABLED)
            debug(pixels);

        return true;
    }

    /**
     * Calculate a scaled size value based upon the area (in pixels)
     * of the character's bitmap. This will help us estimate whether
     * or not the segment is actually a single character
     * @return Return a scaled value which represents the size of the segment
     */
    public float sizeValue() {
        assert bitmap != null;

        return ((float)bitmap.getWidth() * (float)(bitmap.getHeight()) / 1500);
    }
}