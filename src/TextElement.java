import org.apache.pdfbox.pdmodel.graphics.color.PDColor;

import java.util.Arrays;

public class TextElement {
    private float x;
    private float y;
    private PDColor color;
    private String fontName;
    private float fontSize;
    private String bounds;

    public TextElement(float x, float y, PDColor color, String fontName, float fontSize) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.fontName = fontName;
        this.fontSize = fontSize;
        bounds = (String.valueOf(x) + String.valueOf(y));
    }

    public class GraphicElement {


        @Override
        public String toString() {

            return "GraphicElement{" +
                    "bounds=" + bounds +
                    ", color=" + Arrays.toString(color.getComponents()) +
                    '}';
        }
    }

    // Getters (mantenha os existentes)

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public float getFontSize() {
        return fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public PDColor getColor() {
        return color;
    }
}