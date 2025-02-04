import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class GraphicElementExtractor extends PDFGraphicsStreamEngine {
    private final PDPage currentPage;
    private final PDDocument document;

    public GraphicElementExtractor(PDPage page, PDDocument document) {
        super(page);
        this.currentPage = page;
        this.document = document;
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        System.out.println("Image detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + pdImage.getColorSpace().getName());
    }

    @Override
    public void clip(int i) throws IOException {

    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        PDGraphicsState state = getGraphicsState();
        PDColor fillColor = state.getNonStrokingColor();
        PDColorSpace fillColorSpace = state.getNonStrokingColorSpace();
        System.out.println("Fill Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + fillColorSpace + " Components: " + fillColor);
    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {

    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {

    }

    @Override
    public void strokePath() throws IOException {
        PDGraphicsState state = getGraphicsState();
        PDColor strokeColor = state.getStrokingColor();
        PDColorSpace strokeColorSpace = state.getStrokingColorSpace();
        System.out.println("Stroke Path detected on page: " + getPageNumber(currentPage) + " ColorSpace: " + strokeColorSpace + " Components: " + strokeColor);
    }

    private int getPageNumber(PDPage page) {
        PDPageTree pages = document.getDocumentCatalog().getPages();
        return pages.indexOf(page) + 1;
    }

    public void extractElements(File file, int PAGE_LIMIT){
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
                    } else {
                        break;
                    }
                }
            }

            System.out.println("Processamento concluído.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveTo(float x, float y) throws IOException {}
    @Override
    public void lineTo(float x, float y) throws IOException {}
    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {}
    @Override
    public Point2D getCurrentPoint() throws IOException { return new Point2D.Float(0, 0); }
    @Override
    public void closePath() throws IOException {}
    @Override
    public void endPath() throws IOException {}
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {}
}
