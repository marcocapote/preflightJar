import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GraphicElementExtractor extends PDFGraphicsStreamEngine {
    private final PDPage currentPage;
    private final PDDocument document;
    private List<GraphicElement> graphicElements = new ArrayList<>();
    private List<Point2D> currentPathPoints = new ArrayList<>();
    private List<String> mensagens = new ArrayList<>();
    private float HY;
    private float LY;
    private float HX;
    private float LX;

    public GraphicElementExtractor(PDPage page, PDDocument document) {
        super(page);
        this.currentPage = page;
        this.document = document;
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        PDGraphicsState state = getGraphicsState();
        Matrix ctm = state.getCurrentTransformationMatrix();

        // Transforma as coordenadas da imagem
        Point2D.Float point = new Point2D.Float(0, 0);
        Point2D transformed = ctm.transformPoint(point.x, point.y);

        // Obtém as dimensões da imagem
        float imageWidth = pdImage.getWidth();
        float imageHeight = pdImage.getHeight();

        // Calcula o bounding box da imagem
        Rectangle2D bounds = new Rectangle2D.Float(
                (float) transformed.getX(),
                (float) transformed.getY(),
                imageWidth * ctm.getScaleX(),
                imageHeight * ctm.getScaleY()
        );

        // Adiciona o elemento gráfico à lista
        graphicElements.add(new GraphicElement(bounds, state.getNonStrokingColor(), state.getNonStrokingColorSpace()));
        mensagens.add("Image detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + pdImage.getColorSpace().getName());
        // Atualiza as coordenadas máximas e mínimas
        updateBounds(bounds);
    }

    @Override
    public void clip(int i) throws IOException {
        // No action needed for clipping
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        PDGraphicsState state = getGraphicsState();
        PDColor fillColor = state.getNonStrokingColor();
        PDColorSpace fillColorSpace = state.getNonStrokingColorSpace();
        Matrix ctm = state.getCurrentTransformationMatrix();

        //  System.out.println("Fill Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + fillColorSpace + " Components: " + fillColor);
        mensagens.add("Fill Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + fillColorSpace + " Components: " + fillColor);

        // Processa os bounds do caminho
        processPathBounds(false, ctm);
    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {
        // No action needed for fill and stroke
    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {
        // No action needed for shading fill
    }

    @Override
    public void strokePath() throws IOException {
        PDGraphicsState state = getGraphicsState();
        PDColor strokeColor = state.getStrokingColor();
        PDColorSpace strokeColorSpace = state.getStrokingColorSpace();
        Matrix ctm = state.getCurrentTransformationMatrix();


        // System.out.println("Stroke Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + strokeColorSpace + " Components: " + strokeColor);
        mensagens.add("Stroke Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + strokeColorSpace + " Components: " + strokeColor);


        // Processa os bounds do caminho
        processPathBounds(true, ctm);
    }

    private void processPathBounds(boolean isStroke, Matrix ctm) {
        if (!currentPathPoints.isEmpty()) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            for (Point2D point : currentPathPoints) {
                // Transforma o ponto usando a matriz CTM
                Point2D transformed = ctm.transformPoint((float) point.getX(), (float) point.getY());
                double x = transformed.getX();
                double y = transformed.getY();
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }

            if (minX != Double.MAX_VALUE) {
                double width = maxX - minX;
                double height = maxY - minY;
                Rectangle2D bounds = new Rectangle2D.Double(minX, minY, width, height);

                // Adiciona o elemento gráfico à lista
                PDGraphicsState state = getGraphicsState();
                PDColor color = isStroke ? state.getStrokingColor() : state.getNonStrokingColor();
                PDColorSpace colorSpace = isStroke ? state.getStrokingColorSpace() : state.getNonStrokingColorSpace();
                graphicElements.add(new GraphicElement(bounds, color, colorSpace));

                // Atualiza as coordenadas máximas e mínimas
                updateBounds(bounds);
            }
        }
        currentPathPoints.clear();
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        addTransformedPoint(x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        addTransformedPoint(x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        addTransformedPoint(x3, y3); // Apenas o ponto final para simplificar
    }

    private void addTransformedPoint(float x, float y) {
        PDGraphicsState state = getGraphicsState();
        Matrix ctm = state.getCurrentTransformationMatrix();
        Point2D.Float point = new Point2D.Float(x, y);
        Point2D transformed = ctm.transformPoint(point.x, point.y);
        currentPathPoints.add(transformed);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return new Point2D.Float(0, 0);
    }

    @Override
    public void closePath() throws IOException {
        // No action needed for closing paths
    }

    @Override
    public void endPath() throws IOException {
        // No action needed for ending paths
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        // Processa retângulos explicitamente
        currentPathPoints.add(p0);
        currentPathPoints.add(p1);
        currentPathPoints.add(p2);
        currentPathPoints.add(p3);
    }

    private int getPageNumber(PDPage page) {
        PDPageTree pages = document.getDocumentCatalog().getPages();
        return pages.indexOf(page) + 1;
    }

    private void updateBounds(Rectangle2D bounds) {
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        float width = (float) bounds.getWidth();
        float height = (float) bounds.getHeight();

        if (x > HX) HX = x;
        if (x < LX) LX = x;
        if (y > HY) HY = y;
        if (y < LY) LY = y;
    }

    public void extractElements(File file, int PAGE_LIMIT) {
        try (PDDocument document = PDDocument.load(file)) {
            PDPageTree pages = document.getDocumentCatalog().getPages();
            Scanner scanner = new Scanner(System.in);
            int currentIndex = 0;

            while (currentIndex < pages.getCount()) {
                int endIndex = Math.min(currentIndex + PAGE_LIMIT, pages.getCount());

                for (int i = currentIndex; i < endIndex; i++) {
                    PDPage page = pages.get(i);
                    GraphicElementExtractor extractor = new GraphicElementExtractor(page, document);
                    extractor.processPage(page);
                }

                currentIndex = endIndex;

                if (currentIndex < pages.getCount()) {
                    System.out.println("Digite 'next' para processar as próximas " + PAGE_LIMIT + " páginas ou 'exit' para sair:");
                    String input = scanner.nextLine().trim().toLowerCase();

                    if ("exit".equals(input)) {
                        break;
                    } else if (!"next".equals(input)) {
                        System.out.println("Comando inválido. Use 'next' para continuar ou 'exit' para sair.");
                    }
                }
            }

            System.out.println("Processamento concluído.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<GraphicElement> getGraphicElements() {
        return graphicElements;
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public float getHX() {
        return HX;
    }

    public float getHY() {
        return HY;
    }

    public float getLX() {
        return LX;
    }

    public float getLY() {
        return LY;
    }
}