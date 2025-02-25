import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomGraphicsExtractor extends PDFStreamEngine {
    private List<PathElement> currentPath = new ArrayList<PathElement>();
    private List<GraphicElement> graphicalElements = new ArrayList<>();

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        PDGraphicsState state = getGraphicsState();

        // Processar operadores de construção de caminho
        switch (operation) {
            case "m": // MoveTo
                processMoveTo(operands);
                break;
            case "l": // LineTo
                processLineTo(operands);
                break;
            case "re": // Append Rectangle
                processRectangle(operands);
                break;
            case "c": // CurveTo (Bezier)
                processCurveTo(operands);
                break;
            case "h": // ClosePath
                currentPath.add(new PathElement(PathElement.Type.CLOSE_PATH, new float[0]));
                break;
            case "f": // Fill path
                processPaintingOperator(operation, state);
            case "S": // Stroke path
                processPaintingOperator(operation, state);
                break;
        }

        super.processOperator(operator, operands);
    }

    private void processPaintingOperator(String operation, PDGraphicsState state) {
        if (!currentPath.isEmpty()) {
            // Captura a cor atual
            PDColor color = state.getNonStrokingColor(); // Cor de preenchimento
            if ("S".equals(operation)) {
                color = state.getStrokingColor(); // Cor de traçado
            }

            // Calcula os bounds do caminho
            Rectangle2D bounds = calculateBoundsFromPath(currentPath);

            // Cria o GraphicElement com a cor e bounds
            graphicalElements.add(new GraphicElement(bounds, color, color.getColorSpace()));

            // Limpa o caminho atual
            currentPath.clear();
        }
    }

    // Método para calcular bounds a partir do path
    private Rectangle2D calculateBoundsFromPath(List<PathElement> path) {
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

    private void processMoveTo(List<COSBase> operands) {
        if (operands.size() >= 2) {
            float x = ((COSNumber) operands.get(0)).floatValue();
            float y = ((COSNumber) operands.get(1)).floatValue();
            currentPath.add(new PathElement(PathElement.Type.MOVE_TO, new float[]{x, y}));
        }
    }

    private void processLineTo(List<COSBase> operands) {
        if (operands.size() >= 2) {
            float x = ((COSNumber) operands.get(0)).floatValue();
            float y = ((COSNumber) operands.get(1)).floatValue();
            currentPath.add(new PathElement(PathElement.Type.LINE_TO, new float[]{x, y}));
        }
    }

    private void processRectangle(List<COSBase> operands) {
        if (operands.size() >= 4) {
            float x = ((COSNumber) operands.get(0)).floatValue();
            float y = ((COSNumber) operands.get(1)).floatValue();
            float w = ((COSNumber) operands.get(2)).floatValue();
            float h = ((COSNumber) operands.get(3)).floatValue();
            currentPath.add(new PathElement(PathElement.Type.RECTANGLE, new float[]{x, y, w, h}));
        }
    }

    private void processCurveTo(List<COSBase> operands) {
        if (operands.size() >= 6) {
            float x1 = ((COSNumber) operands.get(0)).floatValue();
            float y1 = ((COSNumber) operands.get(1)).floatValue();
            float x2 = ((COSNumber) operands.get(2)).floatValue();
            float y2 = ((COSNumber) operands.get(3)).floatValue();
            float x3 = ((COSNumber) operands.get(4)).floatValue();
            float y3 = ((COSNumber) operands.get(5)).floatValue();
            currentPath.add(new PathElement(PathElement.Type.CURVE_TO, new float[]{x1, y1, x2, y2, x3, y3}));
        }
    }



    private List<PathElement> applyCtmToPath(List<PathElement> path, Matrix ctm) {
        List<PathElement> transformed = new ArrayList<>();
        for (PathElement element : path) {
            float[] coords = element.getCoordinates().clone();
            for (int i = 0; i < coords.length; i += 2) {
                float x = coords[i];
                float y = coords[i + 1];
                // Transforma o ponto usando a CTM
                java.awt.geom.Point2D.Float transformedPoint = ctm.transformPoint(x, y);
                // Extrai as coordenadas transformadas
                coords[i] = transformedPoint.x;
                coords[i + 1] = transformedPoint.y;
            }
            transformed.add(new PathElement(element.getType(), coords));
        }
        return transformed;
    }

    // Método para acessar os elementos processados
    public List<GraphicElement> getGraphicalElements() {
        return graphicalElements;
    }
}