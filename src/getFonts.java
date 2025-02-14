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
import org.apache.pdfbox.cos.*;
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
    private Matrix textMatrix;

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
            textMatrix = new Matrix(a, b, c, d, e, f);
            System.out.println("Operador Tm - Text Matrix: " + textMatrix);
        }

        // Se for um operador de desenho de texto (Tj ou TJ)
        if ("Tj".equals(operation) || "TJ".equals(operation)) {
            PDGraphicsState state = getGraphicsState();
            PDColor color = state.getNonStrokingColor();
            float baseFontSize = state.getTextState().getFontSize();
            PDFont font = state.getTextState().getFont();

            // Usa a escala Y (valor 'd' da matriz)
            float textScaleY = textMatrix.getScaleY();
            Matrix ctm = state.getCurrentTransformationMatrix();
            float ctmScaleY = ctm.getScaleY();
            float effectiveFontSize = baseFontSize * textScaleY * ctmScaleY;

            // Extrai o conteúdo do texto
            String textContent = "";
            if ("Tj".equals(operation)) {
                // Operador Tj: texto simples
                COSString text = (COSString) operands.get(0);
                textContent = text.getString();
            } else if ("TJ".equals(operation)) {
                // Operador TJ: array de texto e espaçamento
                COSArray textArray = (COSArray) operands.get(0);
                StringBuilder textBuilder = new StringBuilder();
                for (COSBase item : textArray) {
                    if (item instanceof COSString) {
                        textBuilder.append(((COSString) item).getString());
                    }
                }
                textContent = textBuilder.toString();
            }

//            // Exibe as informações
//            System.out.println("Texto: " + textContent);
//            System.out.println("Tamanho Base (Tf): " + baseFontSize);
//            System.out.println("Text Scale Y (Tm): " + textScaleY);
//            System.out.println("Tamanho Efetivo: " + effectiveFontSize);
//            System.out.println("Coordenadas X:" + ctm.getTranslateX() + " y:" + ctm.getTranslateY());



            // Exibe informações sobre a fonte
            PDResources resources = getResources();
            if (resources != null) {
                List<String> fontes = new ArrayList<>();
                for (COSName nomeFonte : resources.getFontNames()) {
                    PDFont fontResource = resources.getFont(nomeFonte);
                    if (fontResource != null) {
                        fontes.add(" Tamanho base: " + baseFontSize + " | Tamanho efetivo: " + effectiveFontSize);
                    }
                }
                String info = "Cor: " + (color != null
                        ? color.getColorSpace().getName() + " " + Arrays.toString(color.getComponents())
                        : "Indefinido")
                        + " Fonte(s): " + fontes.getFirst();
                System.out.println("Pagina: " + currentPage + " - " + info);
            }
        }

        // Continua o processamento normal para atualizar o estado (inclusive o textMatrix)
        super.processOperator(operator, operands);
    }

}