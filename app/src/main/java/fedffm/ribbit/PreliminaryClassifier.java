package fedffm.ribbit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreliminaryClassifier {
    private static final String LOG_TAG = "PreliminaryClassifier";
    /**
     * Are we in bounds or out of bounds?
     * @param pixels The map of pixels
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private static boolean outOfBounds(Map<Integer, List<Integer>> pixels, int x, int y) {
        return (x >= pixels.get(0).size() - 1 || x <= 0 || y >= pixels.size() - 1 || y <= 0);
    }

    /**
     * Is the character wide and short, tall and thin, or well-rounded?
     *          0: The character is taller than it is wide
     *          1: The character is relatively even
     *          2: The character is wider than it is tall
     */
    public static void determineRatioClass(Character c) {
        float ratio  = (float)c.getBitmap().getWidth() / (float)c.getBitmap().getHeight();

        if (ratio > 0 && ratio < 0.80)
            c.setRatioClass(0);
        else if (ratio > 0.80 && ratio < 1.30)
            c.setRatioClass(1);
        else if (ratio > 1.30)
            c.setRatioClass(2);
    }

    /**
     * What are certain attributes of the character?
     *
     *  0: default
     *      k, x, s, z, l
     *
     *  1: disconnect
     *      i, j
     *
     *  2: intersection
     *      f, t
     *
     *  3: open right
     *      c, r
     *
     *  4: open top
     *      u, v, w, y, [k]
     *
     *  5: open bottom
     *      n, h, m
     *
     *  6: enclosed space - round
     *      a, e, o
     *
     *  7: enclosed space - pointing up
     *      b, d
     *
     *  8: enclosed space - pointing down
     *      g, p, q
     *
     */
    public static void determineFeatureClass(Character c) {
        // Use this to record how many black pixels are in each row
        int [] rowWidths  = new int[c.getBitmap().getHeight()];

        // Map all of the pixels
        Map <Integer, List<Integer>> pixels = new HashMap<>();
        mapPixels(c.getBitmap(), rowWidths, pixels);

        // Determine which feature class the character falls under
        boolean disconnect    = isDisconnect(rowWidths);
        boolean intersect     = isIntersect(rowWidths);
        boolean openRight     = hasOpenRightSide(pixels);
        boolean openTop       = hasOpenTop(pixels);
        boolean openBottom    = hasOpenBottom(pixels);
        int     enclosedSpace = hasEnclosedSpace(c.getBitmap(), pixels);

        // Assign feature class accordingly
        if (disconnect)
            c.setFeatureClass(1);
        else if (intersect)
            c.setFeatureClass(2);
        else if (openRight)
            c.setFeatureClass(3);
        else if (openTop)
            c.setFeatureClass(4);
        else if (openBottom)
            c.setFeatureClass(5);
        else if (enclosedSpace == 0)
            c.setFeatureClass(6);
        else if (enclosedSpace == 1)     // pointing up
            c.setFeatureClass(7);
        else if (enclosedSpace == 2)     // pointing down
            c.setFeatureClass(8);
        else
            c.setFeatureClass(0);        // default
    }

    /**
     * Get the information for the number of pixels
     * in each row
     * @param bitmap The bitmap of the character
     * @param rowWidths The width (in black pixels) of each row of the character
     * @param pixels A map of each row and the pixels contained in each row
     */
    private static void mapPixels(Bitmap bitmap, int[] rowWidths, Map<Integer, List<Integer>> pixels) {
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
    private static void display(Map<Integer, List<Integer>> pixels) {
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
    private static int hasEnclosedSpace(Bitmap bitmap, Map<Integer, List<Integer>> pixels) {
        // Use this list of coordinates to keep track of our position in the bitmap
        List<String> coordinates = new ArrayList<>();

        // The outer-most loop is to help us find our starting point
        for (int y = 0; y < pixels.size(); ++y) {
            for (int x = 0; x < pixels.get(0).size(); ++x) {

                // White pixel with a black pixel above (three in a row)
                if (y < 1 || x  >= pixels.get(0).size() - 2)
                    break;

                if (pixels.get(y).get(x)     == 0 && pixels.get(y - 1).get(x)     == 1 &&
                        pixels.get(y).get(x + 1) == 0 && pixels.get(y - 1).get(x + 1) == 1 &&
                        pixels.get(y).get(x + 2) == 0 && pixels.get(y - 1).get(x + 2) == 1) {

                    coordinates = new ArrayList<>();

                    // Iterate to the right as far as we can
                    //                 .
                    //.................;;.
                    //::::::::::::::::;;;;.
                    //::::::::::::::::::;;:'
                    //                 :'
                    //
                    while (x < pixels.get(0).size() - 1  && y < pixels.size() - 1  && pixels.get(y).get(x) == 0) {

                        // Move down if need be
                        while (pixels.get(y).get(x) == 0 && pixels.get(y).get(x + 1) == 1 && y < pixels.size() - 1) {
//                            pixels.get(y).set(x, 2);
                            coordinates.add(x + ":" + y);
                            y++;

                            if (outOfBounds(pixels, x, y))
                                return -1;
                        }

                        // Move to the right
//                        pixels.get(y).set(x, 3);
                        coordinates.add(x + ":" + y);
                        x++;
                        if (outOfBounds(pixels, x, y))
                            return -1;

                        // Prepare for the next loop
                        if (pixels.get(y).get(x) == 1) {
                            x--;
                            pixels.get(y).set(x, 0);
                            break;
                        }
                    }

                    // Iterate down as far as we can
                    //   ;;;;;
                    //   ;;;;;
                    //   ;;;;;
                    //   ;;;;;
                    //   ;;;;;
                    // ..;;;;;..
                    //  ':::::'
                    //    ':`
                    //
                    while (x > 0  && y < pixels.size() - 1  && pixels.get(y).get(x) == 0) {

                        // Move to the left if need be
                        while (pixels.get(y).get(x) == 0 && pixels.get(y + 1).get(x) == 1 && x > 0) {
//                            pixels.get(y).set(x, 4);
                            coordinates.add(x + ":" + y);
                            x--;

                            if (outOfBounds(pixels, x, y))
                                return -1;
                        }

                        // Move down
//                        pixels.get(y).set(x, 5);
                        coordinates.add(x + ":" + y);
                        y++;

                        if (outOfBounds(pixels, x, y))
                            return -1;

                        // Prepare for the next loop
                        if (pixels.get(y).get(x) == 1) {
                            y--;
                            pixels.get(y).set(x, 0);
                            break;
                        }
                    }

                    // Iterate left as far as we can
                    //     .
                    //   .;;..................
                    // .;;;;::::::::::::::::::
                    //  ':;;::::::::::::::::::
                    //    ':
                    //
                    while (x > 0  && y > 0 && pixels.get(y).get(x) == 0) {

                        // Move up if need be
                        while (pixels.get(y).get(x) == 0 && pixels.get(y).get(x - 1) == 1 && y > 0) {
//                            pixels.get(y).set(x, 6);
                            coordinates.add(x + ":" + y);
                            y--;

                            if (outOfBounds(pixels, x, y))
                                return -1;
                        }

                        // Move to the left
//                        pixels.get(y).set(x, 7);
                        coordinates.add(x + ":" + y);
                        x--;

                        if (outOfBounds(pixels, x, y))
                            return -1;

                        // Prepare for the next loop
                        if (pixels.get(y).get(x) == 1) {
                            x++;
                            pixels.get(y).set(x, 0);
                            break;
                        }
                    }

                    // Iterate up as far as we can
                    //      .
                    //    .:;:.
                    //  .:;;;;;:.
                    //    ;;;;;
                    //    ;;;;;
                    //    ;;;;;
                    //    ;;;;;
                    //    ;;;;;
                    //
                    while (x < pixels.get(0).size() && y > 0 && pixels.get(y).get(x) == 0) {

                        // Move to the right if need be
                        while (pixels.get(y).get(x) == 0 && pixels.get(y - 1).get(x) == 1 && x < pixels.get(0).size() - 1) {

//                            pixels.get(y).set(x, 8);
                            if (coordinates.contains(x + ":" + y)) {
                                if (isPointingUp(pixels))
                                    return 1;
                                else if (isPointingDown(bitmap, pixels, y))
                                    return 2;
                                return 0;
                            }
                            x++;
                            coordinates.add(x + ":" + y);


                            if (outOfBounds(pixels, x, y))
                                return -1;
                        }

                        // Move up
//                        pixels.get(y).set(x, 9);
                        if (coordinates.contains(x + ":" + y)) {
                            if (isPointingUp(pixels))
                                return 1;
                            else if (isPointingDown(bitmap, pixels, y))
                                return 2;
                            return 0;
                        }
                        y--;
                        coordinates.add(x + ":" + y);


                        if (outOfBounds(pixels, x, y))
                            return -1;

//                        if (pixels.get(y).get(x) == 1) {
//                            y++;
//                            pixels.get(y).set(x, 0);
//                            break;
//                        }
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Is the character pointing upward? The characters that should
     * meet this criteria are:
     *      1. b
     *      2. d
     * @param pixels A map of the pixels
     * @return True if the character has a stem that points upward
     */
    private static boolean isPointingUp(Map<Integer, List<Integer>> pixels) {
        boolean leftSkewed = true;
        boolean rightSkewed = true;

        for (int y = 0; y < pixels.size(); ++y) {
            for (int x = 0; x < pixels.get(y).size(); ++x) {
                // First fifth rows
                if (y <= pixels.get(y).size() / 5) {

                    // Last two-third columns
                    if (x >= pixels.get(y).size() / 3) {
                        if (pixels.get(y).get(x) == 1)
                            leftSkewed = false;
                    }
                    // First two-third columns
                    else if (x <= (int)(pixels.get(y).size() * (2.0f/3.0f))) {
                        if (pixels.get(y).get(x) == 1)
                            rightSkewed = false;
                    }
                }
            }
        }
        return ((leftSkewed && !rightSkewed) || (!leftSkewed && rightSkewed));
    }

    /**
     * Is the character pointing downward? The characters that should
     * meet this criteria are:
     *      1. g
     *      2. p
     *      3. q
     * @param pixels A map of the pixels
     * @return True if the character has a stem that points downward
     */
    private static boolean isPointingDown(Bitmap bitmap, Map<Integer, List<Integer>> pixels, int startingPoint) {

        if (startingPoint >= pixels.size() / 2)
            return false;

        if (bitmap.getHeight() <= bitmap.getWidth())
            return false;

        for (int y = startingPoint; y < pixels.size(); ++y) {
            for (int x = 0; x < pixels.get(y).size() - 1; ++x) {
                if (pixels.get(y).get(x) == 0 && pixels.get(y).get(x + 1) == 1)
                    ;
            }
        }


        return true;
//        boolean leftSkewed = true;
//        boolean rightSkewed = true;
//
//        for (int y = (int)(pixels.size() * (3.0f / 4.0f)); y < (int)(pixels.size() * (4.0f/5.0f)); ++y) {
//            for (int x = 0; x < pixels.get(y).size(); ++x) {
//                // Last two-third columns
//                if (x >= (int)(pixels.get(y).size() / 3.0f)) {
//                    if (pixels.get(y).get(x) == 1)
//                        leftSkewed = false;
//                }
//                // First two-third columns
//                else if (x <= (int)(pixels.get(y).size() * (2.0f / 3.0f))) {
//                    if (pixels.get(y).get(x) == 1)
//                        rightSkewed = false;
//                }
//            }
//        }
//
////        display(pixels);
//        return ((leftSkewed && !rightSkewed) || (!leftSkewed && rightSkewed));
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
    private static boolean isDisconnect(int [] rowWidths) {
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
    private static boolean isIntersect(int [] rowWidths) {
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
    private static boolean hasOpenTop(Map<Integer, List<Integer>> pixels) {
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
    private static boolean hasOpenBottom(Map<Integer, List<Integer>> pixels) {
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
    private static boolean hasOpenRightSide(Map<Integer, List<Integer>> pixels) {

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
}
