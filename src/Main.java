import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static final int PAGE_LIMIT = 200;

    public static void main(String[] args) {
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
            } else {
                System.out.println("Argumento inválido.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
