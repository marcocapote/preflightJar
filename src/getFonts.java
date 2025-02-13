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
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class getFonts extends PDFStreamEngine {
    private Matrix currentTextMatrix = new Matrix();
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

        if ("Tm".equals(operation)) {
            // Atualiza a matriz de texto sempre que o operador Tm é processado
            float a = ((COSNumber) operands.get(0)).floatValue();
            float b = ((COSNumber) operands.get(1)).floatValue();
            float c = ((COSNumber) operands.get(2)).floatValue();
            float d = ((COSNumber) operands.get(3)).floatValue(); // Componente de escala Y
            float e = ((COSNumber) operands.get(4)).floatValue();
            float f = ((COSNumber) operands.get(5)).floatValue();
            currentTextMatrix = new Matrix(a, b, c, d, e, f);
            System.out.println("Operador Tm - Text Matrix: " + currentTextMatrix);
        }

        super.processOperator(operator, operands); // Processa operadores normalmente

        if ("Tj".equals(operation) || "TJ".equals(operation)) {
            PDGraphicsState state = getGraphicsState();
            PDColor color = state.getNonStrokingColor();
            float baseFontSize = state.getTextState().getFontSize();
            PDFont font = state.getTextState().getFont();

            // Usa a escala Y (valor 'd' da matriz)
            float textScaleY = currentTextMatrix.getScaleY();
            Matrix ctm = state.getCurrentTransformationMatrix();
            float ctmScaleY = ctm.getScaleY();
            float effectiveFontSize = baseFontSize * textScaleY * ctmScaleY;

            System.out.println("Tamanho Base (Tf): " + baseFontSize);
            System.out.println("Text Scale Y (Tm): " + textScaleY);
            System.out.println("Tamanho Efetivo: " + effectiveFontSize);
        }
    }




    private float getEffectiveFontSize(PDGraphicsState state, float baseFontSize) {
        Matrix textMatrix = getTextMatrix();
        Matrix ctm = state.getCurrentTransformationMatrix(); // CTM do estado gráfico

        float effectiveFontSize = baseFontSize;

        if (textMatrix != null && ctm != null) {
            // Calcular escala da matriz de texto (Y)
            float textScaleY = (float) Math.sqrt(
                    Math.pow(textMatrix.getValue(1, 0), 2) +
                            Math.pow(textMatrix.getValue(1, 1), 2)
            );

            // Calcular escala da CTM (Y)
            float ctmScaleY = (float) Math.sqrt(
                    Math.pow(ctm.getValue(1, 0), 2) +
                            Math.pow(ctm.getValue(1, 1), 2)
            );

            // Escala total = combinação das transformações
            effectiveFontSize = baseFontSize * textScaleY * ctmScaleY;
        }
        return effectiveFontSize;
    }

}
