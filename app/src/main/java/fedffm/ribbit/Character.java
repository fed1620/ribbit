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
     *      u, v, w, y
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
        // Determine the feature class
        int []  rowWidths  = new int[bitmap.getHeight()];

        // Map all of the pixels
        Map <Integer, List<Integer>> pixels = new HashMap<>();


        mapPixels(rowWidths, pixels);
        boolean disconnect = this.isDisconnect(rowWidths);
        boolean intersect  = this.isIntersect(rowWidths);
        boolean openTops   = this.hasOpenTop(rowWidths, pixels);

        // Assign feature class accordingly
        if (disconnect)
            this.featureClass = 3;
        else if (intersect)
            this.featureClass = 4;
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
                boolean pixelIsBlack = bitmap.getPixel(x, y) < Color.rgb(10, 10, 10);
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
        for (int i = 0; i < rowWidths.length; ++i)
            if (rowWidths[i] == 0)
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
     * @param rowWidths The width (in black pixels) of each row of the character
     * @return True if the character has an open top
     */
    private boolean hasOpenTop(int [] rowWidths, Map<Integer, List<Integer>> pixels) {
        for (int i = 0; i < rowWidths.length; ++i) {
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

        return false;
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