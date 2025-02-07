//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//public class FontInGraphicChecker {
//
//    public void checkPdf(File file) throws IOException {
//        try (PDDocument doc = PDDocument.load(file)) {
//            int pageNumber = 1;
//            for (PDPage page : doc.getPages()) {
//                // Extract graphic elements
//                GraphicElementExtractor graphicExtractor = new GraphicElementExtractor(page, doc);
//                graphicExtractor.processPage(page);
//                List<GraphicElement> graphicElements = graphicExtractor.getGraphicElements();
//
//                // Extract text elements
//                TextExtractor textExtractor = new TextExtractor();
//                textExtractor.processPage(page);
//                List<TextElement> textElements = textExtractor.getTextElements();
//
//                // Check for text inside graphic elements
//                for (TextElement text : textElements) {
//                    for (GraphicElement graphic : graphicElements) {
//                        if (graphic.getBounds().contains(text.getX(), text.getY())) {
//                            System.out.println("Page " + pageNumber + ": Text is inside a graphic element.");
//                            System.out.println("Text Font: " + text.getFontName() + ", Size: " + text.getFontSize() +
//                                    ", Color: " + Arrays.toString(text.getColor().getComponents()));
//                            System.out.println("Graphic Color: " + Arrays.toString(graphic.getColor().getComponents()));
//                        }
//                    }
//                }
//                pageNumber++;
//            }
//        }
//    }
//
//
//}