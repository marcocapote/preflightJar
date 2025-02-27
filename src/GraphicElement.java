import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class GraphicElement {
    private Rectangle2D bounds;
    private PDColor color;
    private PDColorSpace colorSpace;
    private List<PathElement> path;
    private String operation;

    // Construtor original (usando bounds, color, colorSpace)
    public GraphicElement(Rectangle2D bounds, PDColor color, PDColorSpace colorSpace) {
        this.bounds = bounds;
        this.color = color;
        this.colorSpace = colorSpace;
    }



    // Método para calcular bounds a partir do path
    private Rectangle2D calculateBoundsFromPath() {
        if (path == null || path.isEmpty()) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        boolean hasPoints = false;

        for (PathElement element : path) {
            float[] coords = element.getCoordinates();
            switch (element.getType()) {
                case MOVE_TO:
                case LINE_TO:
                    if (coords.length >= 2) {
                        double x = coords[0];
                        double y = coords[1];
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                        hasPoints = true;
                    }
                    break;
                case RECTANGLE:
                    if (coords.length >= 4) {
                        double x = coords[0];
                        double y = coords[1];
                        double w = coords[2];
                        double h = coords[3];
                        // Calcula os limites do retângulo
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x + w);
                        maxY = Math.max(maxY, y + h);
                        hasPoints = true;
                    }
                    break;
                case CURVE_TO:
                    if (coords.length >= 6) {
                        // Considera todos os pontos da curva (aproximação)
                        for (int i = 0; i < 6; i += 2) {
                            double x = coords[i];
                            double y = coords[i + 1];
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                        hasPoints = true;
                    }
                    break;
                case CLOSE_PATH:
                    // Não afeta os limites
                    break;
            }
        }

        if (!hasPoints) {
            return null;
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    // Getters (mantenha os existentes)
    public Rectangle2D getBounds() {
        return bounds;
    }

    public PDColor getColor() {
        return color;
    }

    public PDColorSpace getColorSpace() {
        return colorSpace;
    }

    public List<PathElement> getPath() {
        return path;
    }

    public String getOperation() {
        return operation;
    }

    // Método toString unificado
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (operation != null) {
            // Exibe informações da nova versão
            sb.append("Operação: ").append(operation).append("\n");
            if (path != null) {
                for (PathElement element : path) {
                    sb.append("  ").append(element).append("\n");
                }
            }
        } else {
            // Exibe informações da versão original
            sb.append("Bounds: ").append(bounds).append("\n");
            sb.append("Cor: ").append(color).append("\n");
            sb.append("Espaço de Cor: ").append(colorSpace).append("\n");
        }
        return sb.toString();
    }
}