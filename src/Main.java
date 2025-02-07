import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        // Configuração para suprimir warnings do PDFBox
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        if (args.length <= 1) {
            System.err.println("Uso: java -jar preflight.jar <caminho/para/pdf> <argumento>");
            System.exit(1);
        }

        String filePath = args[0];
        String argument = args[1];

        File file = new File(filePath);

        try (PDDocument document = PDDocument.load(file)) {
            if (Objects.equals(argument, "graphic")) {
                PDPageTree pages = document.getDocumentCatalog().getPages();
                for (PDPage page : pages) {
                    GraphicElementExtractor extractor = new GraphicElementExtractor(page, document);
                    extractor.processPage(page);
                }
            } else if (Objects.equals(argument, "fonts")) {
                getFonts getFonts = new getFonts();
                getFonts.extractFonts(document);
            }else if (Objects.equals(argument, "fontElement")) {
                PDPageTree pages = document.getDocumentCatalog().getPages();
                int pageIndex = 1;
                for (PDPage page : pages) {
                    // Extrai elementos gráficos
                    GraphicElementExtractor graphicExtractor = new GraphicElementExtractor(page, document);
                    graphicExtractor.processPage(page);
                    List<GraphicElement> graphicElements = graphicExtractor.getGraphicElements();

                    // Extrai elementos de texto
                    TextExtractor textExtractor = new TextExtractor();
                    textExtractor.setCurrentPage(page); // Define a página atual
                    textExtractor.processPage(page); // Processa a página
                    List<TextElement> textElements = textExtractor.getTextElements();

                    // Verifica sobreposição
                    System.out.println("\n--- Página " + pageIndex + " ---");
                    if (textElements.isEmpty() && graphicElements.isEmpty()) {
                        System.out.println("Nenhum elemento encontrado.");
                    } else {
                        for (TextElement text : textElements) {
                            boolean isInsideGraphic = false;
                            for (GraphicElement graphic : graphicElements) {
                                if (graphic.getBounds().contains(text.getX(), text.getY())) {
                                    System.out.println("Texto dentro de elemento gráfico:");
                                    System.out.println("  Posição: (" + text.getX() + ", " + text.getY() + ")");
                                    System.out.println("  Fonte: " + text.getFontName() + ", Tamanho: " + text.getFontSize());
                                    System.out.println("  Cor do Texto: " + Arrays.toString(text.getColor().getComponents()));
                                    System.out.println("  Cor do Gráfico: " + Arrays.toString(graphic.getColor().getComponents()));
                                    isInsideGraphic = true;
                                }
                            }
                            if (!isInsideGraphic) {
                                System.out.println("Texto fora de elementos gráficos: '" + text.getFontName() + "' em (" + text.getX() + ", " + text.getY() + ")");
                            }
                        }
                    }
                    pageIndex++;
                }
            } else if (Objects.equals(argument, "image")) {
                PDPageTree pages = document.getDocumentCatalog().getPages();
                for (PDPage page : pages) {
                    getImages extractor = new getImages(page, document);
                    extractor.processPage(page);
                }
            } else if (Objects.equals(argument, "margin")) {
                PDPageTree pages = document.getDocumentCatalog().getPages();
                for (PDPage page : pages) {
                    PDFMargins extractor = new PDFMargins(page, document);
                    extractor.calculateMarginsAndBleed();
                }
            } else if (Objects.equals(argument, "marginSafety")) {
                PDPageTree pages = document.getDocumentCatalog().getPages();
                for (PDPage page : pages) {
                    PDFMargins extractor = new PDFMargins(page, document);
                    extractor.calculateSafetyMargin();
                }
            } else {
                System.out.println("Argumento inválido.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}