import java.awt.geom.Point2D;
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
    private PDRectangle bleedBox;
    private final PDRectangle artBox;
    private PDRectangle trimBox;
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private float maxY = Float.MIN_VALUE;

    protected PDFMargins(PDPage page, PDDocument document) {
        super(page);
        this.currentPage = page;
        this.document = document;
        this.mediaBox = page.getMediaBox();
        this.bleedBox = page.getBleedBox();
        this.artBox = page.getArtBox();
        this.trimBox = page.getTrimBox();

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
        // Implementação vazia
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
        // Implementação vazia
    }

    @Override
    public void endPath() throws IOException {
        // Implementação vazia
    }

    @Override
    public void strokePath() throws IOException {
        // Implementação vazia
    }

    @Override
    public void fillPath(int i) throws IOException {
        // Implementação vazia
    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {
        // Implementação vazia
    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {
        // Implementação vazia
    }

    private void updateBounds(float x, float y) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    public void calculateMarginsAndBleed() {
        // Se TrimBox não existir, usa MediaBox como padrão
        if (trimBox == null) {
            trimBox = mediaBox;
        }

        // Se BleedBox não existir, assume uma sangria padrão (3 mm)
        if (bleedBox == null) {
            float bleedSize = 3 * 2.83465f; // 3 mm em pontos (1 mm = 2.83465 pt)
            bleedBox = new PDRectangle(
                    mediaBox.getLowerLeftX() - bleedSize,
                    mediaBox.getLowerLeftY() - bleedSize,
                    mediaBox.getWidth() + 2 * bleedSize,
                    mediaBox.getHeight() + 2 * bleedSize
            );
        }

        // Calcula a sangria (diferença entre BleedBox e TrimBox)
        float bleedLeft = bleedBox.getLowerLeftX() - trimBox.getLowerLeftX();
        float bleedRight = trimBox.getUpperRightX() - bleedBox.getUpperRightX();
        float bleedTop = trimBox.getUpperRightY() - bleedBox.getUpperRightY();
        float bleedBottom = bleedBox.getLowerLeftY() - trimBox.getLowerLeftY();

        // Converte para milímetros
        float bleedLeftMm = Math.abs(pontosParaMm(bleedLeft));
        float bleedRightMm = Math.abs(pontosParaMm(bleedRight));
        float bleedTopMm = Math.abs(pontosParaMm(bleedTop));
        float bleedBottomMm = Math.abs(pontosParaMm(bleedBottom));

        // Exibe os resultados
        System.out.println("Página: " + getPageNumber(currentPage));
        System.out.printf("Sangria [Esq: %.1f mm, Dir: %.1f mm, Topo: %.1f mm, Base: %.1f mm]%n",
                bleedLeftMm, bleedRightMm, bleedTopMm, bleedBottomMm);
        System.out.println("-------------------------");
    }

    private float pontosParaMm(float pontos) {
        return pontos * 0.352778f; // 1 ponto = 0,352778 mm
    }

    private int getPageNumber(PDPage currentPage) {
        return document.getDocumentCatalog().getPages().indexOf(this.currentPage) + 1;
    }
}