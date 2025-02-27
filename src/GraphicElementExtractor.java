import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.awt.geom.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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

        // Dimensões originais da imagem
        float imageWidth = pdImage.getWidth();
        float imageHeight = pdImage.getHeight();
        // Converte para pontos (1 ponto = 1/72 polegada)
        float widthPoints = Math.abs(ctm.getScalingFactorX());
        float heightPoints = Math.abs(ctm.getScalingFactorY());

        // Calcula os DPI corretamente
        float dpiX = (imageWidth / widthPoints) * 72;
        float dpiY = (imageHeight / heightPoints) * 72;
        int mediaDpi = Math.round((dpiX + dpiY)/2);

        float widthInPoints = imageWidth * (72f / mediaDpi);
        float heightInPoints = imageHeight * (72f / mediaDpi);

        // Usar apenas a translação da matriz (ignorar escala)
        double x = ctm.getTranslateX();
        double y = ctm.getTranslateY();

        // Criar retângulo com as dimensões corretas
        Rectangle2D bounds = new Rectangle2D.Double(
                x,
                y,
                widthInPoints,
                heightInPoints
        );

        // Adicionar à lista de elementos gráficos
        graphicElements.add(new GraphicElement(bounds, state.getNonStrokingColor(), state.getNonStrokingColorSpace()));
        updateBounds(bounds);

        // Log para depuração
        mensagens.add("Image detected on page: " + getPageNumber(currentPage) +
                " ColorSpace: " + pdImage.getColorSpace().getName() +
                " Matrix: " + ctm +
                " Bounds: " + bounds);
    }
    //  mensagens.add("Image detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + pdImage.getColorSpace().getName() + " MAtriz: " + ctm);

    @Override
    public void clip(int i) throws IOException {
        // No action needed for clipping
    }

//    @Override
//    public void showText(byte[] string) throws IOException {
//        // Decodifica os bytes para uma string usando a codificação adequada
//        String text = new String(string, "ISO-8859-1"); // ou outra codificação conforme o PDF
//        // Obtém a matriz de texto atual
//        Matrix textMatrix = getTextMatrix();
//
//        // A partir da fonte atual e do estado de texto, você pode obter
//        // as métricas (como tamanho, avanço dos glifos, altura, etc.)
//        // que ajudam a calcular o bounding box do texto.
//        // Por exemplo, utilizando getFont() e métodos relacionados:
//        PDGraphicsState state = getGraphicsState();
//        float fontSize = state.getTextState().getFontSize();
//        // A obtenção das métricas reais depende do tipo de fonte utilizada.
//        // Se for um PDType0Font ou PDTrueTypeFont, você pode utilizar métodos
//        // como getBoundingBox() da fonte, ajustados pelo fontSize.
//
//        // Calcule o bounding box real do texto usando o textMatrix e as métricas.
//        Rectangle2D bounds = computeTextBounds(text, state, textMatrix);
//
//        // Registre o elemento textual com as informações calculadas.
//        graphicElements.add(new GraphicElement(bounds, state.getNonStrokingColor(), state.getNonStrokingColorSpace()));
//
//        // Também pode registrar mensagens de depuração, se necessário.
//        mensagens.add("Show Text detected on page: " + getPageNumber(currentPage)
//                + " Text: " + text
//                + " Bounds: " + bounds
//                + " CTM: " + textMatrix);
//    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        PDGraphicsState state = getGraphicsState();
        PDColor fillColor = state.getNonStrokingColor();
        PDColorSpace fillColorSpace = state.getNonStrokingColorSpace();
        Matrix ctm = state.getCurrentTransformationMatrix();

        // Converte a Matrix do PDFBox para AffineTransform
        AffineTransform transform = new AffineTransform(
                ctm.getScaleX(), ctm.getShearY(),
                ctm.getShearX(), ctm.getScaleY(),
                ctm.getTranslateX(), ctm.getTranslateY()
        );

        // Cria um objeto Path2D para representar o caminho
        Path2D path = new Path2D.Double(windingRule);
        for (int i = 0; i < currentPathPoints.size(); i++) {
            Point2D p = currentPathPoints.get(i);
            if (i == 0) {
                path.moveTo(p.getX(), p.getY());
            } else {
                path.lineTo(p.getX(), p.getY());
            }
        }

        // Fecha o caminho se necessário
        if (!currentPathPoints.isEmpty()) {
            path.closePath();
        }

        // Aplica a transformação ao caminho
        Shape transformedShape = transform.createTransformedShape(path);

        // Calcula os bounds precisos
        Rectangle2D preciseBounds = transformedShape.getBounds2D();

        // Converte coordenadas Y (PDF -> Java2D)
        PDRectangle mediaBox = currentPage.getMediaBox();
        preciseBounds = convertPDFCoordinates(preciseBounds, mediaBox);

        // Adiciona à lista de elementos
        graphicElements.add(new GraphicElement(preciseBounds, fillColor, fillColorSpace));
        mensagens.add("Fill Path detected on page: " + getPageNumber(currentPage)
                + " ColorSpace: " + fillColorSpace
                + " Components: " + fillColor

                + " ctm: " + ctm

        );
        currentPathPoints.clear();
    }

    // Metodo para conversão de coordenadas Y
    private Rectangle2D convertPDFCoordinates(Rectangle2D bounds, PDRectangle mediaBox) {
        double javaY = mediaBox.getHeight() - bounds.getY() - bounds.getHeight();
        return new Rectangle2D.Double(
                bounds.getX(),
                javaY,
                bounds.getWidth(),
                bounds.getHeight()
        );
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
        processPathBounds(ctm);
    }

    private Rectangle2D processPathBounds(Matrix ctm) {
        if (currentPathPoints.isEmpty()) {
            return null;
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D point : currentPathPoints) {
            double x = point.getX();
            double y = point.getY();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        currentPathPoints.clear();

        double width = maxX - minX;
        double height = maxY - minY;
        return new Rectangle2D.Double(minX, minY, width, height);
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
        // Adiciona pontos de controle e final
        addTransformedPoint(x1, y1);
        addTransformedPoint(x2, y2);
        addTransformedPoint(x3, y3);
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
        PDGraphicsState state = getGraphicsState();
        Matrix ctm = state.getCurrentTransformationMatrix();

        // Transforma os pontos do retângulo
        Point2D transformedP0 = ctm.transformPoint((float) p0.getX(), (float) p0.getY());
        Point2D transformedP1 = ctm.transformPoint((float) p1.getX(), (float) p1.getY());
        Point2D transformedP2 = ctm.transformPoint((float) p2.getX(), (float) p2.getY());
        Point2D transformedP3 = ctm.transformPoint((float) p3.getX(), (float) p3.getY());

        currentPathPoints.add(transformedP0);
        currentPathPoints.add(transformedP1);
        currentPathPoints.add(transformedP2);
        currentPathPoints.add(transformedP3);
    }

    private int getPageNumber(PDPage page) {
        PDPageTree pages = document.getDocumentCatalog().getPages();
        return pages.indexOf(page) + 1;
    }

    private void updateBounds(Rectangle2D bounds) {
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();

        if (x > HX) HX = x;
        if (x < LX) LX = x;
        if (y > HY) HY = y;
        if (y < LY) LY = y;
    }



    public List<GraphicElement> getGraphicElements() {
        return graphicElements;
    }

    public List<String> getMensagens() {
        return mensagens;
    }


}