import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PDFMargins {
    private final PDPage currentPage;
    private final PDDocument document;
    private final PDRectangle mediaBox;
    private PDRectangle bleedBox;
    private PDRectangle trimBox;
    private PDRectangle artBox;

    public PDFMargins(PDPage page, PDDocument document) {
        this.currentPage = page;
        this.document = document;
        this.mediaBox = page.getMediaBox();
        this.bleedBox = page.getBleedBox();
        this.trimBox = page.getTrimBox();
        this.artBox = page.getArtBox();
    }

    public void calculateMarginsAndBleed() {
        // Se TrimBox não existir, usa MediaBox como padrão
        if (trimBox == null) {
            trimBox = mediaBox;
        }

        // Se BleedBox não existir, assume uma sangria padrão (3 mm)
        if (bleedBox == null) {
            float bleedSize = 3 * 2.83465f; // 3 mm em pontos
            bleedBox = new PDRectangle(
                    mediaBox.getLowerLeftX() - bleedSize,
                    mediaBox.getLowerLeftY() - bleedSize,
                    mediaBox.getWidth() + 2 * bleedSize,
                    mediaBox.getHeight() + 2 * bleedSize
            );
        }

        // Calcula a sangria
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
        System.out.println("Pagina: " + getPageNumber());
        System.out.printf("Sangria [Esq: %.1f mm, Dir: %.1f mm, Topo: %.1f mm, Base: %.1f mm]%n",
                bleedLeftMm, bleedRightMm, bleedTopMm, bleedBottomMm);
        System.out.println("-------------------------");
    }

    public void calculateSafetyMargin(){
        float safetyMarginLeft = artBox.getLowerLeftX() - trimBox.getLowerLeftX();
        float safetyMarginRight = artBox.getUpperRightX() - trimBox.getUpperRightX();
        float safetyMarginTop = artBox.getUpperRightY() - trimBox.getUpperRightY();
        float safetyMarginBottom = artBox.getLowerLeftY() - trimBox.getLowerLeftY();

        // Converte para milímetros
        float safetyMarginLeftMm = Math.abs(pontosParaMm(safetyMarginLeft));
        float safetyMarginRightMm = Math.abs(pontosParaMm(safetyMarginRight));
        float safetyMarginTopMm = Math.abs(pontosParaMm(safetyMarginTop));
        float safetyMarginBottomMm = Math.abs(pontosParaMm(safetyMarginBottom));

        System.out.printf("Pagina: " + getPageNumber() + " Margens [Esq: %.1f mm, Dir: %.1f mm, Topo: %.1f mm, Base: %.1f mm]%n",
                safetyMarginLeftMm, safetyMarginRightMm, safetyMarginTopMm, safetyMarginBottomMm);
    }

    private float pontosParaMm(float pontos) {
        return pontos * 0.352778f; // 1 ponto = 0,352778 mm
    }

    private int getPageNumber() {
        return document.getDocumentCatalog().getPages().indexOf(currentPage) + 1;
    }
}