import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextExtractor extends PDFStreamEngine {
    private List<TextElement> textElements = new ArrayList<>();
    private PDPage currentPage; // Current page being processed
    private Matrix textMatrix = new Matrix();
    private Matrix lineMatrix = new Matrix();

    public TextExtractor() throws IOException {
        addOperator(new BeginText());
        addOperator(new EndText());
        addOperator(new SetFontAndSize());
        addOperator(new ShowText());
        addOperator(new ShowTextAdjusted());
        addOperator(new MoveText());
        addOperator(new MoveTextSetLeading());
        addOperator(new NextLine());

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

    public void setCurrentPage(PDPage page) {
        this.currentPage = page;
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        PDGraphicsState state = getGraphicsState();

        if (state == null || currentPage == null) {
            return;
        }

        switch (operation) {
            case "BT":
                textMatrix = new Matrix();
                lineMatrix = new Matrix();
                break;
            case "ET":
                textMatrix = null;
                lineMatrix = null;
                break;
            case "Tj":
            case "TJ":
                if (textMatrix != null) {
                    float x = textMatrix.getTranslateX();
                    float y = textMatrix.getTranslateY();
                    float pageHeight = currentPage.getMediaBox().getHeight();
                    float yNormalized = pageHeight - y;

                    PDColor color = state.getNonStrokingColor();
                    PDFont font = state.getTextState().getFont();
                    if (font == null) {
                        break;
                    }

                    String fontName = font.getName();
                    float basefontSize = state.getTextState().getFontSize();
                    float textScaleY = textMatrix.getScaleY();
                    Matrix ctm = state.getCurrentTransformationMatrix();
                    float ctmScaleY = ctm.getScaleY();
                    float fontSize = basefontSize * textScaleY * ctmScaleY;

                    String text = font.toString();

                    textElements.add(new TextElement(x, yNormalized, color, fontName, fontSize, text));
                }
                break;
            case "Td": // Mover posição do texto
            case "TD": // Mover posição do texto e definir leading
                if (operands.size() >= 2) {
                    if (operands.get(0) instanceof COSNumber && operands.get(1) instanceof COSNumber) {
                        float tx = ((COSNumber) operands.get(0)).floatValue();
                        float ty = ((COSNumber) operands.get(1)).floatValue();
                        textMatrix = Matrix.getTranslateInstance(tx, ty).multiply(lineMatrix);
                    }
                }
                break;
            case "Tm": // Definir matriz de texto
                if (operands.size() >= 6) {
                    if (operands.get(0) instanceof COSNumber &&
                            operands.get(1) instanceof COSNumber &&
                            operands.get(2) instanceof COSNumber &&
                            operands.get(3) instanceof COSNumber &&
                            operands.get(4) instanceof COSNumber &&
                            operands.get(5) instanceof COSNumber) {

                        float a = ((COSNumber) operands.get(0)).floatValue();
                        float b = ((COSNumber) operands.get(1)).floatValue();
                        float c = ((COSNumber) operands.get(2)).floatValue();
                        float d = ((COSNumber) operands.get(3)).floatValue();
                        float e = ((COSNumber) operands.get(4)).floatValue();
                        float f = ((COSNumber) operands.get(5)).floatValue();
                        textMatrix = new Matrix(a, b, c, d, e, f);
                        lineMatrix = new Matrix(a, b, c, d, e, f);

                        float x = textMatrix.getTranslateX();
                        float y = textMatrix.getTranslateY();
                        float pageHeight = currentPage.getMediaBox().getHeight();
                        float yNormalized = pageHeight - y;

                        PDColor color = state.getNonStrokingColor();
                        PDFont font = state.getTextState().getFont();
                        if (font == null) {
                            break;
                        }

                        String fontName = font.getName();
                        float fontSize = state.getTextState().getFontSize();
                        String text = font.toString();

                        textElements.add(new TextElement(x, yNormalized, color, fontName, fontSize, text));
                    }
                }
                break;
            case "T*": // Próxima linha
                if (lineMatrix != null) {
                    textMatrix = Matrix.getTranslateInstance(0, -state.getTextState().getLeading()).multiply(lineMatrix);
                }
                break;
        }

        super.processOperator(operator, operands);
    }

    public List<TextElement> getTextElements() {
        return textElements;
    }
}