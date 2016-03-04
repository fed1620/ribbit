package fedffm.bytewrite;

import android.graphics.Bitmap;
import android.graphics.Color;


public class Character {

    private char    name;
    private Integer ascii;
    private Integer width;
    private Integer height;
    private Bitmap  bitmap;

    // Default
    public Character() {
        this.name      = '?';
        this.ascii     = null;
        this.width     = null;
        this.height    = null;
        this.bitmap    = null;
    }

    // Non-default
    public Character(Bitmap bitmap) {
        this.name      = '?';
        this.ascii     = null;
        this.bitmap    = bitmap;
        this.setDimensions();
    }

    // Setters
    public  void setName(char name)       {this.name = name;}
    public  void setAscii(Integer ascii)  {this.ascii = ascii;}
    public  void setBitmap(Bitmap bitmap) {this.bitmap = bitmap;}
    private void setWidth(int width)      {this.width  = width;}
    private void setHeight(int height)    {this.height = height;}

    // Getters
    public char    getName()   {return this.name;}
    public Integer getAscii()  {return this.ascii;}
    public Integer getWidth()  {return this.width;}
    public Integer getHeight() {return this.height;}
    public Bitmap  getBitmap() {return this.bitmap;}


    /**
     * Find out how wide a character is, as well as how tall a character is
     */
    private void setDimensions() {
        int xStart = this.bitmap.getWidth();
        int yStart = this.bitmap.getHeight();
        int xEnd   = 0;
        int yEnd   = 0;

        // Start by iterating through the bitmap
        for (int x = 0; x < this.bitmap.getWidth(); ++x)
            for (int y = 0; y < this.bitmap.getHeight(); ++y)

                // We only care about black pixels (the character's pixels)
                if (this.bitmap.getPixel(x, y) == Color.BLACK) {
                    // Find out where the character begins and ends on the x axis
                    if (x < xStart)
                        xStart = x;
                    if (x > xEnd)
                        xEnd = x;

                    // Find out where the character begins and ends on the y axis
                    if (y < yStart)
                        yStart = y;
                    if (y > yEnd)
                        yEnd = y;
                }

        // Set the appropriate dimensions
        this.setWidth(xEnd - xStart);
        this.setHeight(yEnd - yStart);
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