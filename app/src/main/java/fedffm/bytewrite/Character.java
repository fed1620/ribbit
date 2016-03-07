package fedffm.bytewrite;

import android.graphics.Bitmap;


public class Character {

    private char    name;
    private Integer ascii;
    private Integer ratioClass;
    private Integer featureClass;
    private Bitmap  bitmap;


    // Default
    public Character() {
        this.name         = '?';
        this.ascii        = null;
        this.ratioClass   = null;
        this.featureClass = null;
        this.bitmap       = null;
    }

    // Non-default
    public Character(Bitmap bitmap) {
        this.name         = '?';
        this.ascii        = null;
        this.featureClass = null;
        this.bitmap       = bitmap;
        this.determineRatioClass();
    }

    // Setters
    public  void setName(char name)       {this.name = name;}
    public  void setAscii(Integer ascii)  {this.ascii = ascii;}
    public  void setBitmap(Bitmap bitmap) {this.bitmap = bitmap;}

    // Getters
    public char    getName()       {return this.name;}
    public Integer getAscii()      {return this.ascii;}
    public Bitmap  getBitmap()     {return this.bitmap;}
    public int     getRatioClass() {return this.ratioClass;}


    /**
     * Is the character wide and short, tall and thin, or well-rounded?
     *          1: The character is taller than it is wide
     *          0: The character is relatively even
     *         -1: The character is wider than it is tall
     */
    private void determineRatioClass() {
        float ratio  = (float)this.bitmap.getWidth() / (float)this.bitmap.getHeight();

        if (ratio > 0 && ratio < 0.80)
            this.ratioClass = 1;
        else if (ratio > 0.80 && ratio < 1.30)
            this.ratioClass = 0;
        else if (ratio > 1.30)
            this.ratioClass = -1;
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