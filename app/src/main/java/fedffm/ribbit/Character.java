package fedffm.ribbit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Character {
    private static final String  LOG_TAG         = "Character";
    private static final boolean LOGGING_ENABLED = true;
    private static final boolean DEBUG           = true;

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
     *      u, v, w, y, [k]
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
        boolean openRight  = this.hasOpenRightSide(pixels);
        boolean openTop    = this.hasOpenTop(pixels);
        boolean openBottom = this.hasOpenBottom(pixels);
        boolean enclosedSpace = this.hasEnclosedSpace(pixels);

        // Assign feature class accordingly
        if (disconnect)
            this.featureClass = 3;
        else if (intersect)
            this.featureClass = 4;
        else if (openRight)
            this.featureClass = 7;
        else if (openTop)
            this.featureClass = 5;
        else if (openBottom)
            this.featureClass = 6;
        else if (enclosedSpace)
            this.featureClass = 1;
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
    private void display(Map<Integer, List<Integer>> pixels) {
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
    private boolean hasEnclosedSpace(Map<Integer, List<Integer>> pixels) {
        for (int y = 0; y < pixels.size(); ++y) {
            for (int x = 0; x < pixels.get(0).size(); ++x) {
                // So that we don't go out of bounds
                if (y < 1 || x  >= pixels.get(0).size() - 2)
                    break;

                // White pixel with a black pixel above (three in a row)
                if (pixels.get(y).get(x)     == 0 && pixels.get(y - 1).get(x)     == 1 &&
                    pixels.get(y).get(x + 1) == 0 && pixels.get(y - 1).get(x + 1) == 1 &&
                    pixels.get(y).get(x + 2) == 0 && pixels.get(y - 1).get(x + 2) == 1) {

                    // Iterate to the right as far as we can
                    while (x < pixels.get(0).size() - 1  &&
                           y < pixels.size()        - 1  &&
                           pixels.get(y).get(x)     == 0) {

                        pixels.get(y).set(x, 3);
                        if (pixels.get(y).get(x + 1) != 1)
                            ++x;

                        // If we run into a black pixel, move down
                        if (y < pixels.size() - 1        &&
                            x < pixels.get(0).size() - 1 &&
                            pixels.get(y).get(x + 1) == 1 ) {

                            pixels.get(y).set(x, 4);
                            if (pixels.get(y + 1).get(x) != 1)
                                ++y;
                        }
                    }

                    // Iterate down as far as we can
                    while (y < pixels.size() - 1 && pixels.get(y).get(x) == 0) {
                        pixels.get(y).set(x,5);
                        ++y;

                        // If we run into a black pixel, move to the left
                        if (x < pixels.get(0).size() - 1 && pixels.get(y).get(x) == 1) {
                            --x;
                            pixels.get(y).set(x,5);
                        }
                    }
                }
            }
        }
        display(pixels);

        return false;
    }
//    private boolean hasEnclosedSpace(Map<Integer, List<Integer>> pixels) {
//        int [] columnPositions = new int[3];
//        int [] rowPositions    = new int[3];
//
//        int thirdOfColumn   = pixels.size() / 3;
//        int middleOfColumn  = pixels.size() / 2;
//        int twoThirdsColumn = (int)(pixels.size() * (2.0f/3.0f));
//        int thirdOfRow      = pixels.get(0).size() / 3;
//        int middleOfRow     = pixels.get(0).size() / 2;
//        int twoThirdsRow    = (int)(pixels.get(0).size() * (2.0f/3.0f));
//
//        columnPositions[0] = thirdOfColumn;
//        columnPositions[1] = middleOfColumn;
//        columnPositions[2] = twoThirdsColumn;
//        rowPositions[0]    = thirdOfRow;
//        rowPositions[1]    = middleOfRow;
//        rowPositions[2]    = twoThirdsRow;
//
//
//        int numPossibilities = 0;
//
//        for (int i = 0; i < columnPositions.length; ++i) {
//            for (int j = 0; j < rowPositions.length; ++j) {
//                // Only evaluate characters where we start in open space
//                if (pixels.get(columnPositions[i]).get(rowPositions[j]) != 0)
//                    continue;
//
//                boolean pathToTop    = true;
//                boolean pathToBottom = true;
//                boolean pathToRight  = true;
//                boolean pathToLeft   = true;
//
//                // Can we make it to the top of the bitmap?
//                for (int k = columnPositions[i]; k >= 0; --k) {
//                    if (pixels.get(k).get(rowPositions[j]) == 1) {
//                        pathToTop = false;
//                        break;
//                    }
//                    if (DEBUG)
//                        pixels.get(k).set(rowPositions[j], 2);
//                }
//
//                // Can we make it to the bottom of the bitmap?
//                for (int k = columnPositions[i]; k < pixels.size(); ++k) {
//                    if (pixels.get(k).get(rowPositions[j]) == 1) {
//                        pathToBottom = false;
//                        break;
//                    }
//                    if (DEBUG)
//                        pixels.get(k).set(rowPositions[j], 3);
//                }
//
//                // Can we make it to the right side of the bitmap?
//                for (int k = rowPositions[i]; k < pixels.get(0).size(); ++k) {
//                    if (pixels.get(columnPositions[j]).get(k) == 1) {
//                        pathToRight = false;
//                        break;
//                    }
//                    if (DEBUG)
//                        pixels.get(columnPositions[j]).set(k, 4);
//                }
//
//                // Can we make it to the left side of the bitmap?
//                for (int k = rowPositions[i]; k >= 0; --k) {
//                    if (pixels.get(columnPositions[j]).get(k) == 1) {
//                        pathToLeft = false;
//                        break;
//                    }
//                    if (DEBUG)
//                        pixels.get(columnPositions[j]).set(k, 5);
//                }
//
//                if (!pathToTop && !pathToBottom && !pathToRight && !pathToLeft)
//                    numPossibilities++;
//            }
//        }
//
//        if (numPossibilities < 3)
//            return false;
//
//        if (LOGGING_ENABLED)
//            display(pixels);
//
//        return true;
//    }

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
        boolean startingPointReached = false;

        // Each iteration is a row in the bitmap
        for (int y = 0; y < pixels.size(); ++y) {

            // Each iteration is a pixel in the current row
            for (int x = 0; x < pixels.get(y).size(); ++x) {
                boolean topLeftPixelSet  = !(topLeftBlackPixelX  == 0 && topLeftBlackPixelY  == 0);
                boolean topRightPixelSet = !(topRightBlackPixelX == 0 && topRightBlackPixelY == 0);

                ///////////////////////////////////////////////////////////////////////////////////
                // Find the top-left black pixel
                //
                ////////////////////////////////////////////////////////////////////////////////////
                if (!topLeftPixelSet && pixels.get(y).get(x) == 1 && x <= pixels.get(y).size()/3) {
                    topLeftBlackPixelX = x;
                    topLeftBlackPixelY = y;
                }

                // Record our starting point
                if (x <= pixels.get(y).size()/2 && topLeftPixelSet && !startingPointReached &&
                        pixels.get(y).get(x) == 1 && pixels.get(y).get(x + 1) == 0 && pixels.get(y).get(x + 2) == 0) {
                    startingPointReached = true;
                }

                 ///////////////////////////////////////////////////////////////////////////////////
                //                                                    Find the top-right black pixel
                //
                ////////////////////////////////////////////////////////////////////////////////////
                if (!topRightPixelSet && x >= pixels.get(y).size()/2) {
                    // Only evaluate black pixels
                    if (pixels.get(y).get(x) == 1) {
                        // If the black pixel is at the very edge of the bitmap,
                        // it qualifies as the top-right pixel
                        if ((x == pixels.get(y).size() - 1)) {
                            topRightBlackPixelX = x;
                            topRightBlackPixelY = y;
                        }
                        // If it is the last black pixel in the row, it also
                        // qualifies as the top-right pixel
                        else if (pixels.get(y).get(x + 1) == 0) {
                            topRightBlackPixelX = x;
                            topRightBlackPixelY = y;
                        }
                    }
                    continue;
                }

                // We can't begin further evaluation until we establish our starting
                // and stopping points
                if (!topLeftPixelSet || !startingPointReached || !topRightPixelSet)
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

        return true;
    }

    /**
     * Is the bottom of the character "open" ? The characters that should meet
     * this criteria are:
     *      1. n
     *      2. h
     *      3. m
     *
     * @param pixels Each entry represents a row of black and white pixels
     * @return True if the character has an open top
     */
    private boolean hasOpenBottom(Map<Integer, List<Integer>> pixels) {
        List<Integer> xCoordinates = new ArrayList<>();
        List<Integer> yCoordinates = new ArrayList<>();
        int bottomLeftBlackPixelX  = 0;
        int bottomLeftBlackPixelY  = 0;
        int bottomRightBlackPixelX = 0;
        int bottomRightBlackPixelY = 0;
        boolean startingPointReached = false;

        // Each iteration is a row in the bitmap
        for (int y = pixels.size() - 1; y >= 0; --y) {

            // Each iteration is a pixel in the current row
            for (int x = 0; x < pixels.get(y).size(); ++x) {
                boolean bottomLeftPixelSet  = !(bottomLeftBlackPixelX  == 0 && bottomLeftBlackPixelY  == 0);
                boolean bottomRightPixelSet = !(bottomRightBlackPixelX == 0 && bottomRightBlackPixelY == 0);

                ////////////////////////////////////////////////////////////////////////////////////
                //
                // Find the bottom-left black pixel
                ////////////////////////////////////////////////////////////////////////////////////
                if (!bottomLeftPixelSet && pixels.get(y).get(x) == 1 && x <= pixels.get(y).size()/3) {
                    bottomLeftBlackPixelX = x;
                    bottomLeftBlackPixelY = y;
                }

                // Record our starting point
                if (x <= pixels.get(y).size()/2 && bottomLeftPixelSet && !startingPointReached &&
                        pixels.get(y).get(x) == 1 && pixels.get(y).get(x + 1) == 0 && pixels.get(y).get(x + 2) == 0) {
                    startingPointReached = true;
                }

                ////////////////////////////////////////////////////////////////////////////////////
                //
                //                                                 Find the bottom-right black pixel
                ////////////////////////////////////////////////////////////////////////////////////
                if (!bottomRightPixelSet && x >= pixels.get(y).size()/2) {
                    // Only evaluate black pixels
                    if (pixels.get(y).get(x) == 1) {
                        // If the black pixel is at the very edge of the bitmap,
                        // it qualifies as the top-right pixel
                        if ((x == pixels.get(y).size() - 1)) {
                            bottomRightBlackPixelX = x;
                            bottomRightBlackPixelY = y;
                        }
                        // If it is the last black pixel in the row, it also
                        // qualifies as the top-right pixel
                        else if (pixels.get(y).get(x + 1) == 0) {
                            bottomRightBlackPixelX = x;
                            bottomRightBlackPixelY = y;
                        }
                    }
                    continue;
                }

                // We can't begin further evaluation until we establish our starting
                // and stopping points
                if (!bottomLeftPixelSet || !startingPointReached || !bottomRightPixelSet)
                    continue;

                // Ensure the top-left-most and top-right-most pixels of the character are not
                // wide enough apart, return false
                float margin = pixels.get(y).size() / 3.0f;
                if (bottomLeftBlackPixelX >= margin || bottomRightBlackPixelX <= pixels.get(y).size() - margin)
                    return false;

                // As long as the current pixel is white, move up
                while (y > 0 && x < pixels.get(y).size() - 1 && pixels.get(y).get(x) == 0) {
                    y--;

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

            if (xPosition < bottomLeftBlackPixelX || xPosition > bottomRightBlackPixelX)
                return false;

            // In order to be considered an "open-bottom" character, the character
            // must exhibit adequate depth
            if (yPosition >= pixels.size() / 2)
                return false;

            // Attempt to find a clean route back down to the bottom of the bitmap
            for (int i = yPosition + 1; i < pixels.size(); ++i) {
                if (pixels.get(i).get(xPosition) == 1)
                    return false;
            }
        }
        else
            return false;

        return true;
    }

    /**
     * Is the right side of the character "open" ? The characters that should meet
     * this criteria are:
     *      1. c
     *      2. r
     *
     * @param pixels Each entry represents a row of black and white pixels
     * @return True if the character is "open" at the right side
     */
    private boolean hasOpenRightSide(Map<Integer, List<Integer>> pixels) {

        // The index of the right-most edge of the bitmap
        int lastColumnIndex = pixels.get(0).size() - 1;

        // Until proven otherwise, assume that each section
        // only has pixels at the left of the bitmap
        boolean upperThirdIsLeftSkewed = true;
        boolean middleIsLeftSkewed     = true;
        boolean lowerThirdIsLeftSkewed = true;

        // Starting at the right-most edge of the bitmap,
        // iterate until we reach the left-most edge
        for (int i = lastColumnIndex; i >= 0; --i) {

            // Detect black pixels close to the right edge
            // of the upper-third section of the bitmap
            if (pixels.get(pixels.size() / 3).get(i) == 1)
                if (i >= lastColumnIndex / 1.9f)
                    upperThirdIsLeftSkewed = false;

            // Detect black pixels close to the right edge
            // of the middle section of the bitmap
            if (pixels.get(pixels.size() / 2).get(i) == 1)
                if (i >= lastColumnIndex / 1.9f)
                    middleIsLeftSkewed = false;

            // Detect black pixels close to the right edge
            // of the lower-third section of the bitmap
            if (pixels.get((int)(pixels.size() * (2.0f/3.0f))).get(i) == 1)
                if (i >= lastColumnIndex / 1.9f)
                    lowerThirdIsLeftSkewed = false;

        }

        return !(!upperThirdIsLeftSkewed || !middleIsLeftSkewed || !lowerThirdIsLeftSkewed);
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