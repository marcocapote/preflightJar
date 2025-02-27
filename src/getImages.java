import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.util.Matrix;

public class getImages extends PDFGraphicsStreamEngine {
    private final PDPage currentPage;
    private final PDDocument document;

    protected getImages(PDPage page, PDDocument document) {
        super(page);
        this.currentPage = page;
        this.document = document;
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        float widthPixels = pdImage.getWidth();
        float heightPixels = pdImage.getHeight();

        // Obtém a matriz de transformação da imagem dentro da página
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();

        // Converte para pontos (1 ponto = 1/72 polegada)
        float widthPoints = Math.abs(ctm.getScalingFactorX());
        float heightPoints = Math.abs(ctm.getScalingFactorY());

        // Calcula os DPI corretamente
        float dpiX = (widthPixels / widthPoints) * 72;
        float dpiY = (heightPixels / heightPoints) * 72;
        int mediaDpi = Math.round((dpiX + dpiY)/2);

        if (mediaDpi < 300 && mediaDpi != 0) {
            System.out.println("Pagina: " + getPageNumber(currentPage) + " Resolucao: " + mediaDpi + "dpi");
        } else{
            System.out.println("ok");
        }
    }


    private int getPageNumber(PDPage page) {
        PDPageTree pages = document.getDocumentCatalog().getPages();
        int index = pages.indexOf(page);
        return (index != -1) ? index + 1 : -1;  // Retorna o número correto ou -1 se não encontrar
    }



    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {}

    @Override
    public void clip(int windingRule) throws IOException {}

    @Override
    public void moveTo(float x, float y) throws IOException {}

    @Override
    public void lineTo(float x, float y) throws IOException {}

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {}

    @Override
    public Point2D getCurrentPoint() throws IOException {  return new Point2D.Float(0, 0);  }

    @Override
    public void closePath() throws IOException {}

    @Override
    public void endPath() throws IOException {}

    @Override
    public void strokePath() throws IOException {}

    @Override
    public void fillPath(int windingRule) throws IOException {}

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {}

    @Override
    public void shadingFill(COSName shadingName) throws IOException {}
}
