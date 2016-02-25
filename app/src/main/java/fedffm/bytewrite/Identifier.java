package fedffm.bytewrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.List;

public class Identifier {
    private final static String LOG_TAG = "Identifier";

    /**
     * In order to more accurately compare the unknown character against any given
     * sample character, scale the unknown character to be the exact same size as the sample
     * character
     * @param sampleCharacter The bitmap for a known character from our CharacterBase
     * @param unknownCharacter The bitmap for the character we are trying to identify
     * @return A bitmap of the unknown character with the same dimensions of the sample character
     */
    private static Bitmap matchSize(Bitmap sampleCharacter, Bitmap unknownCharacter) {
        // Get the dimensions of the sample bitmap
        int w = sampleCharacter.getWidth();
        int h = sampleCharacter.getHeight();

        // Create a bitmap of the unknown character using the dimensions of the sample
        return Bitmap.createScaledBitmap(unknownCharacter, w, h, false);
    }

    /**
     * Identify a character
     * @param unknown The character to be identified
     * @return The character updated with a name and ASCII code
     */
    public static Character identify(Character unknown, Context context) {
        // Start by getting the entire character base
        List<Character> characters = CharacterBase.getInstance(context).getAllCharacterSamples();
        Bitmap scaled = null;

        // Compare against each of them
        for (int i = 0; i < characters.size(); ++i) {
            scaled = matchSize(characters.get(i).getBitmap(), unknown.getBitmap());
        }
        return new Character();
    }

}
