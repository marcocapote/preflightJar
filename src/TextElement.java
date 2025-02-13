import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class TextElement {
    private float x;
    private float y;
    private PDColor color;
    private String fontName;
    private float fontSize;
    private String bounds;
    private String text; // Armazena o conteúdo do texto

    public TextElement(float x, float y, PDColor color, String fontName, float fontSize, String text) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.text = text;
        this.bounds = String.valueOf(x) + y;
    }

    public Rectangle2D.Float getBounds() {
        // Defina a largura como um valor aproximado (pode ser ajustado conforme necessário)
        float textWidth = text.length() * (fontSize * 0.5f); // Ajuste conforme necessário
        float textHeight = fontSize;

        // Retorna o retângulo delimitador do texto
        return new Rectangle2D.Float(x, y - textHeight, textWidth, textHeight);
    }

    @Override
    public String toString() {
        return "TextElement{" +
                "x=" + x +
                ", y=" + y +
                ", fontSize=" + fontSize +
                ", fontName='" + fontName + '\'' +
                ", color=" + Arrays.toString(color.getComponents()) +
                '}';
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getFontSize() { return fontSize; }
    public String getFontName() { return fontName; }
    public PDColor getColor() { return color; }
    public String getText() { return text; }

    public class GraphicElement {
        @Override
        public String toString() {
            return "GraphicElement{" +
                    "bounds=" + bounds +
                    ", color=" + Arrays.toString(color.getComponents()) +
                    '}';
        }
    }
}
