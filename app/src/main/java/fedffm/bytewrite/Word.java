package fedffm.bytewrite;

import java.util.ArrayList;
import java.util.List;

public class Word {
    private List<Character> characters;
    private int size;

    // Setters
    public void setSize(int size) {this.size = size;}

    // Getters
    public List<Character> getCharacters() {return this.characters;}

    // Default
    Word() {
        this.characters = new ArrayList<>();
        this.size = 0;
    }

    // Non-default
    Word(List<Character> characters) {
        this.characters = characters;
        this.size = characters.size();
    }

    /**
     * Add a character to our list
     * @param character The new character
     */
    public void addCharacter(Character character) {
        this.characters.add(character);
        setSize(this.characters.size());
    }

    /**
     * How many characters the word contains
     * @return The number of characters
     */
    public int length() {
        return this.size;
    }

    /**
     * Display the word
     */
    public String getString() {
        String string = "";

        // Add each character to the string
        for (Character character: this.characters) {
            string += character.getName();
        }

        return string;
    }
}
