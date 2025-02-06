import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.text.SetFontAndSize;
import org.apache.pdfbox.contentstream.operator.text.ShowText;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class getFonts extends PDFStreamEngine {

    private int currentPage;

    public getFonts() throws IOException {
        // Registra o operador que define a fonte e o tamanho (Tf)
        addOperator(new SetFontAndSize());
        // Registra o operador que desenha o texto (Tj / TJ)
        addOperator(new ShowText());

        // Operadores de cor
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

        // Se for um operador de desenho de texto (Tj ou TJ)
        if ("Tj".equals(operation) || "TJ".equals(operation)) {
            PDGraphicsState state = getGraphicsState();
            PDColor color = state != null ? state.getNonStrokingColor() : null;
            // Obtém o tamanho base da fonte (definido pelo operador Tf processado pelo SetFontAndSize)
            assert state != null;
            float baseFontSize = state.getTextState().getFontSize();

            // Tenta obter a matriz de texto atual
            Matrix textMatrix = getTextMatrix();
            float effectiveFontSize = baseFontSize;
            if (textMatrix != null) {
                // Calcula o fator de escala na direção Y (normalmente a escala vertical)
                float scaleY = (float) Math.sqrt(
                        Math.pow(textMatrix.getValue(1, 0), 2) +
                                Math.pow(textMatrix.getValue(1, 1), 2)
                );
                effectiveFontSize = baseFontSize * scaleY;
            }

            PDResources resources = getResources();
            if (resources != null) {
                List<String> fontes = new ArrayList<>();
                for (COSName nomeFonte : resources.getFontNames()) {
                    PDFont font = resources.getFont(nomeFonte);
                    if (font != null) {
                        fontes.add(" Tamanho base: " + baseFontSize + " | Tamanho efetivo: " + effectiveFontSize);
                    }
                }
                String info = "Cor: " + (color != null
                        ? color.getColorSpace().getName() + " " + Arrays.toString(color.getComponents())
                        : "Indefinido")
                        + " Fonte(s): " + fontes;
                System.out.println("Página: " + currentPage + " - " + info);
            }
        }
        // Continua o processamento normal para atualizar o estado (inclusive o textMatrix)
        super.processOperator(operator, operands);
    }

}
