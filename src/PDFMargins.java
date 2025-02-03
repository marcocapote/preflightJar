import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.util.Matrix;

public class PDFMargins extends PDFGraphicsStreamEngine {
    private final PDPage currentPage;
    private final PDDocument document;
    private final PDRectangle mediaBox;
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private float maxY = Float.MIN_VALUE;

    protected PDFMargins(PDPage page, PDDocument document) {
        super(page);
        this.currentPage = page;
        this.document = document;
        this.mediaBox = page.getMediaBox();
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        updateBounds(ctm.getTranslateX(), ctm.getTranslateY());
        updateBounds(
                ctm.getTranslateX() + pdImage.getWidth() * ctm.getScalingFactorX(),
                ctm.getTranslateY() + pdImage.getHeight() * ctm.getScalingFactorY()
        );
    }

    @Override
    public void clip(int i) throws IOException {

    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        updateBounds((float) p0.getX(), (float) p0.getY());
        updateBounds((float) p2.getX(), (float) p2.getY());
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        updateBounds(x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        updateBounds(x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        updateBounds(x1, y1);
        updateBounds(x3, y3);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return null;
    }

    @Override
    public void closePath() throws IOException {

    }

    @Override
    public void endPath() throws IOException {

    }

    @Override
    public void strokePath() throws IOException {

    }

    @Override
    public void fillPath(int i) throws IOException {

    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {

    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {

    }

    // ... (outros métodos mantidos conforme necessário)

    private void updateBounds(float x, float y) {
        // Considera apenas elementos dentro da página
        if (x >= mediaBox.getLowerLeftX() && x <= mediaBox.getUpperRightX() &&
                y >= mediaBox.getLowerLeftY() && y <= mediaBox.getUpperRightY()) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
    }

    public void calculateMarginsAndBleed() {
        // Verifica se o conteúdo foi detectado
        if (minX == Float.MAX_VALUE) {
            System.out.println("Nenhum conteúdo detectado");
            return;
        }

        // Obtém a BleedBox (área de sangria)
        PDRectangle bleedBox = currentPage.getBleedBox();
        if (bleedBox == null) {
            // Se a BleedBox não estiver definida, assume um valor padrão de 5 mm
            float bleedSize = 5 * 2.83465f; // Converte mm para pontos (1 mm = 2.83465 pontos)
            bleedBox = new PDRectangle(
                    mediaBox.getLowerLeftX() - bleedSize,
                    mediaBox.getLowerLeftY() - bleedSize,
                    mediaBox.getWidth() + 2 * bleedSize,
                    mediaBox.getHeight() + 2 * bleedSize
            );
        }

        // Calcula as margens internas
        float marginLeft = minX - mediaBox.getLowerLeftX();
        float marginRight = mediaBox.getUpperRightX() - maxX;
        float marginBottom = minY - mediaBox.getLowerLeftY();
        float marginTop = mediaBox.getUpperRightY() - maxY;

        // Calcula as sangrias
        float bleedLeft = bleedBox.getLowerLeftX() - mediaBox.getLowerLeftX();
        float bleedRight = bleedBox.getUpperRightX() - mediaBox.getUpperRightX();
        float bleedBottom = bleedBox.getLowerLeftY() - mediaBox.getLowerLeftY();
        float bleedTop = bleedBox.getUpperRightY() - mediaBox.getUpperRightY();

        // Converte os valores para milímetros
        float marginLeftMm = pontosParaMm(marginLeft);
        float marginRightMm = pontosParaMm(marginRight);
        float marginBottomMm = pontosParaMm(marginBottom);
        float marginTopMm = pontosParaMm(marginTop);

        float bleedLeftMm = pontosParaMm(bleedLeft);
        float bleedRightMm = pontosParaMm(bleedRight);
        float bleedBottomMm = pontosParaMm(bleedBottom);
        float bleedTopMm = pontosParaMm(bleedTop);

        // Exibe as margens e sangrias em milímetros
        System.out.println("Página: " + getPageNumber());
        System.out.printf("Margens [L:%.1f R:%.1f T:%.1f B:%.1f] mm%n",
                marginLeftMm, marginRightMm, marginTopMm, marginBottomMm);
        System.out.printf("Sangrias [L:%.1f R:%.1f T:%.1f B:%.1f] mm%n",
                bleedLeftMm, bleedRightMm, bleedTopMm, bleedBottomMm);
        System.out.println("-------------------------");
    }

    /**
     * Converte pontos para milímetros.
     */
    private float pontosParaMm(float pontos) {
        return pontos * 0.352778f; // 1 ponto = 0,352778 mm
    }

    private int getPageNumber() {
        return document.getDocumentCatalog().getPages().indexOf(currentPage) + 1;
    }
}