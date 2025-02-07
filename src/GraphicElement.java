import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import java.awt.geom.Rectangle2D;

public class GraphicElement {
    private Rectangle2D bounds;
    private PDColor color;
    private PDColorSpace colorSpace;

    public GraphicElement(Rectangle2D bounds, PDColor color, PDColorSpace colorSpace) {
        this.bounds = bounds;
        this.color = color;
        this.colorSpace = colorSpace;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public PDColor getColor() {
        return color;
    }

    public PDColorSpace getColorSpace() {
        return colorSpace;
    }
}