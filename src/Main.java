import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

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
            switch (argument) {
                case "graphic" -> {
                    PDPageTree pages = document.getDocumentCatalog().getPages();
                    for (PDPage page : pages) {
                        GraphicElementExtractor extractor = new GraphicElementExtractor(page, document);
                        extractor.processPage(page);
                        extractor.getMensagens().forEach(System.out::println);
                    }
                }
                case "fonts" -> {
                    getFonts getFonts = new getFonts();
                    getFonts.extractFonts(document);
                }
                case "fontElement" -> {
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
                        // System.out.println("\n--- Página " + pageIndex + " ---");
                        if (textElements.isEmpty() && graphicElements.isEmpty()) {
                            System.out.println("Nenhum elemento encontrado.");
                        } else {
                            for (TextElement text : textElements) {
                                boolean isInsideGraphic = false;
                                for (GraphicElement graphic : graphicElements) {
                                    if (graphic.getBounds().contains(text.getX(), text.getY())) {
                                        System.out.println("Pagina: " + pageIndex + "  Posicao: (" + text.getX() + ", " + text.getY() + ")" + ", Tamanho: " + text.getFontSize() + "  CorTexto: " + Arrays.toString(text.getColor().getComponents()) + "  CorGrafico: " + Arrays.toString(graphic.getColor().getComponents()));
//                                    System.out.println("Texto dentro de elemento grafico:");
//                                    System.out.println("  Posicao: (" + text.getX() + ", " + text.getY() + ")");
//                                    System.out.println("  Fonte: " + text.getFontName() + ", Tamanho: " + text.getFontSize());
//                                    System.out.println("  Cor do Texto: " + Arrays.toString(text.getColor().getComponents()));
//                                    System.out.println("  Cor do Grafico: " + Arrays.toString(graphic.getColor().getComponents()));
                                        isInsideGraphic = true;
                                    }
                                }

                            }
                        }
                        pageIndex++;
                    }
                }
                case "image" -> {
                    PDPageTree pages = document.getDocumentCatalog().getPages();
                    for (PDPage page : pages) {
                        getImages extractor = new getImages(page, document);
                        extractor.processPage(page);
                    }
                }
                case "margin" -> {
                    PDPageTree pages = document.getDocumentCatalog().getPages();
                    for (PDPage page : pages) {
                        PDFMargins extractor = new PDFMargins(page, document);
                        extractor.calculateMarginsAndBleed();
                    }
                }
                case "marginSafety" -> {
                    PDPageTree pages = document.getDocumentCatalog().getPages();
                    for (PDPage page : pages) {
                        PDFMargins extractor = new PDFMargins(page, document);
                        extractor.calculateSafetyMargin();
                    }
                }
                case "info" -> {
                    PDPageTree pages = document.getDocumentCatalog().getPages();
                    int quantidadePagina = pages.getCount();

                    PDPage page = pages.get(0);
                    PDRectangle mediaBox = page.getMediaBox();

                    // Convertendo de pontos para mm
                    float widthMm = (mediaBox.getWidth() / 72) * 25.4f;
                    float heightMm = (mediaBox.getHeight() / 72) * 25.4f;

                    System.out.println("Paginas: " + quantidadePagina);
                    System.out.printf("Resolucao: %.2f mm x %.2f mm", widthMm, heightMm);
                }
                case null, default -> System.out.println("Argumento inválido.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}