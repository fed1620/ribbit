package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;


public class Character {

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
//        this.determineFeatureClass();
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


        //____________________________________ENCLOSED SPACE______________________________________//

        ////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                        //
        //                                                                                        //
        //                                                                                        //
        //                        BLACK PIXELS BLACK PIXELS BLACK PIXELS                          //
        //                    BlACK PIXELS --> WHITE PIXELS --> BLACK PIXELS                      //
        //             BLACK PIXELS ---------> WHITE PIXELS ---------> BLACK PIXELS               //
        //     BLACK PIXELS -----------------> WHITE PIXELS-----------------> BLACK PIXELS        //
        //             BLACK PIXELS ---------> WHITE PIXELS ---------> BLACK PIXELS               //
        //                    BlACK PIXELS --> WHITE PIXELS --> BLACK PIXELS                      //
        //                        BLACK PIXELS BLACK PIXELS BLACK PIXELS                          //
        //                                                                                        //
        //                                                                                        //
        ////////////////////////////////////////////////////////////////////////////////////////////


        //____________________________________DISCONNECT__________________________________________//
        int [] rowWidths = new int[bitmap.getHeight()];
        for (int y = 0; y < bitmap.getHeight(); ++y) {
            int numBlackPixelsInRow = 0;

            // Iterate across the row from left to right
            for (int x = 0; x < bitmap.getWidth(); ++x) {
                boolean pixelIsBlack = bitmap.getPixel(x, y) < Color.rgb(10, 10, 10);
                if (pixelIsBlack) {
                    numBlackPixelsInRow++;
                }
            }

            rowWidths[y] = numBlackPixelsInRow;

            if (numBlackPixelsInRow == 0)
                this.featureClass = 3;
        }

        for (int i = 0; i < rowWidths.length; ++i) {
            if (i < 12 || i > rowWidths.length - 12)
                continue;

            boolean wideRow = (rowWidths[i] >= (rowWidths[i - 12] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 11] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 10] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 9] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 8] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 7] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 6] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 5] * 3) &&
                               rowWidths[i] >= (rowWidths[i - 4] * 3) &&

                               rowWidths[i] >= (rowWidths[i + 4] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 5] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 6] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 7] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 8] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 9] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 10] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 11] * 3) &&
                               rowWidths[i] >= (rowWidths[i + 12] * 3));

            if (wideRow)
                this.featureClass = 4;
        }
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