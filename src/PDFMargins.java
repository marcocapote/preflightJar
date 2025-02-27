import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.awt.geom.Rectangle2D;

public class PDFMargins {
    private final PDPage currentPage;
    private final PDDocument document;
    private final PDRectangle mediaBox;
    private PDRectangle bleedBox;
    private PDRectangle trimBox;
    private PDRectangle artBox;
    private List<GraphicElement> graphicElements;
    private final int margemErro = 150;

    public PDFMargins(PDPage page, PDDocument document) throws IOException {
        this.currentPage = page;
        this.document = document;
        this.mediaBox = page.getMediaBox();
        this.bleedBox = page.getBleedBox();
        this.trimBox = page.getTrimBox();
        this.artBox = page.getArtBox();
        GraphicElementExtractor graphicExtractor = new GraphicElementExtractor(page, document);
        graphicExtractor.processPage(page);
        this.graphicElements = graphicExtractor.getGraphicElements();
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
        System.out.println("-------------------------");
        System.out.println("Pagina: " + getPageNumber());
        System.out.printf("Sangria [Esq: %.1f mm, Dir: %.1f mm, Topo: %.1f mm, Base: %.1f mm]%n",
                bleedLeftMm, bleedRightMm, bleedTopMm, bleedBottomMm);

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

    public Rectangle2D margemErroMaior() {
        // Cria uma area maior que a trimbox a partir do valor de margemErro
        return new Rectangle2D.Double(
                trimBox.getLowerLeftX() - margemErro, // Reduz o X mínimo
                trimBox.getLowerLeftY() - margemErro, // Reduz o Y mínimo
                trimBox.getWidth() + 2 * margemErro,  // Aumenta a largura em 2 * margemErro
                trimBox.getHeight() + 2 * margemErro  // Aumenta a altura em 2 * margemErro
        );
    }

    public Rectangle2D margemErroMenor() {
        // Cria uma area menor que a trimbox a partir do valor de margemErro
        return new Rectangle2D.Double(
                trimBox.getLowerLeftX() + margemErro, // Reduz o X mínimo
                trimBox.getLowerLeftY() + margemErro, // Reduz o Y mínimo
                trimBox.getWidth() - 2 * margemErro,  // Aumenta a largura em 2 * margemErro
                trimBox.getHeight() - 2 * margemErro  // Aumenta a altura em 2 * margemErro
        );
    }



    private float pontosParaMm(float pontos) {
        return pontos * 0.352778f; // 1 ponto = 0,352778 mm
    }

    private int getPageNumber() {
        return document.getDocumentCatalog().getPages().indexOf(currentPage) + 1;
    }

    public void elementMargin() {
        Rectangle2D margemMaior = margemErroMaior();
        Rectangle2D margemMenor = margemErroMenor();

        for (GraphicElement graphic : graphicElements) {
            Rectangle2D bounds = graphic.getBounds();
            double difL = trimBox.getLowerLeftX() - bounds.getMinX();
            double difR = bounds.getMaxX() - trimBox.getUpperRightX();
            double difD = trimBox.getLowerLeftY() - bounds.getMinY();
            double difU = bounds.getMaxY() - trimBox.getUpperRightY();

            // Verifica se o elemento está na área de risco
            if (margemMaior.intersects(bounds) && !margemMenor.contains(bounds)) {
              //  System.out.println("Elemento muito perto da margem de corte: " + graphic + " " + graphic.getColor());
                // Verifica cada lado individualmente e calcula a distância em relação à trimBox
                if (Math.round(Math.abs(pontosParaMm((float) difL))) < 5) {
                    System.out.println("esquerda distancia: " + Math.abs(pontosParaMm((float) difL)) + "mm " );
                }
                if (Math.round(Math.abs(pontosParaMm((float) difR))) < 5) {
                    System.out.println("direita distancia: " + Math.abs(pontosParaMm((float) difR)) + "mm " );
                }
                if (Math.round(Math.abs(pontosParaMm((float) difD))) < 5) {
                    System.out.println("inferior distancia: " + Math.abs(pontosParaMm((float) difD)) + "mm " );
                }
                if (Math.round(Math.abs(pontosParaMm((float) difU))) < 5) {
                    System.out.println("superior distancia: " + Math.abs(pontosParaMm((float) difU)) + "mm "  );
                }
            }
        }
    }
}