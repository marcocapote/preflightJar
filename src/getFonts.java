import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.contentstream.operator.text.ShowText;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;


import java.io.IOException;
import java.util.*;

public class getFonts extends PDFStreamEngine {

    private int currentPage;

    public getFonts() throws IOException {
        addOperator(new ShowText());
        // Add color operators
        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetStrokingColor());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetStrokingColorN());
        addOperator(new SetNonStrokingColorN());
        addOperator(new SetStrokingDeviceGrayColor());
        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetStrokingDeviceRGBColor());
        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceCMYKColor());
    }

    public void extractFonts(PDDocument document) throws IOException {
        int pageIndex = 1;
        for (PDPage page : document.getPages()) {
            currentPage = pageIndex;
            processPage(page);
            pageIndex++;
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();

        if ("Tj".equals(operation) || "TJ".equals(operation)) { // Operators that draw text
            PDGraphicsState state = getGraphicsState();
            PDColor color = state != null ? state.getNonStrokingColor() : null;
            PDResources resources = getResources();

            if (resources != null) {
                List<String> fontes = new ArrayList<>();

                for (COSName nomeFonte : resources.getFontNames()) {
                    PDFont font = resources.getFont(nomeFonte);
                    if (font != null) {
                        fontes.add(font.getName()); // Get the real font name
                    }
                }

            //    String info = "Fonte(s): " + fontes + " | Cor: " + (color != null ? color.getColorSpace().getName()  + ' ' + Arrays.toString(color.getComponents()) : "Indefinido ");
                String info = "Cor: " + (color != null ? color.getColorSpace().getName()  + ' ' + Arrays.toString(color.getComponents()) : "Indefinido ") + " Fonte(s): " + fontes ;
                System.out.println("Pagina: " + currentPage + " - " + info);
            }
        }
        super.processOperator(operator, operands);
    }


}