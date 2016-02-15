package fedffm.bytewrite;

import android.graphics.Bitmap;

import java.util.List;

public class Character {

    private char name;
    private Integer id;
    private Integer ascii;
    private Bitmap bitmap;
    private List<Bitmap> quadrants;

    // Default
    public Character() {
        this.name      = '?';
        this.id        = null;
        this.ascii     = null;
        this.bitmap    = null;
        this.quadrants = null;
    }

    // Non-default
    public Character(Bitmap bitmap) {
        this.name      = '?';
        this.id        = null;
        this.ascii     = null;
        this.bitmap    = bitmap;
        this.quadrants = null;
    }

    // Setters
    public void setName(char name)       {this.name = name;}
    public void setId(Integer id)        {this.id = id;}
    public void setAscii(Integer ascii)  {this.ascii = ascii;}
    public void setBitmap(Bitmap bitmap) {this.bitmap = bitmap;}

    // Getters
    public char         getName()      {return this.name;}
    public Integer      getId()        {return this.id;}
    public Integer      getAscii()     {return this.ascii;}
    public Bitmap       getBitmap()    {return this.bitmap;}
    public List<Bitmap> getQuadrants() {return this.quadrants;}


    /**
     * Add a fourth of the original character bitmap
     *
     * [1][2]
     * [3][4]
     *
     * @param quadrant Return the quadrant
     */
    public void addQuadrant(Bitmap quadrant) {
        if (quadrants.size() < 4)
            quadrants.add(quadrant);
    }

    /**
     * Calculate a scaled size value based upon the area (in pixels)
     * of the character's bitmap. This will help us estimate whether
     * or not the segment is actually a single character
     * @return Return a scaled value which represents the size of the segment
     */
    public float sizeValue() {
        assert bitmap != null;

        return ((float)bitmap.getWidth() * (float)(bitmap.getHeight()) / 1000);
    }
}