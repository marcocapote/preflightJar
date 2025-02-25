import java.util.Arrays;

public class PathElement {
    public enum Type { MOVE_TO, LINE_TO, CURVE_TO, RECTANGLE, CLOSE_PATH }

    private Type type;
    private float[] coordinates;

    public PathElement(Type type, float[] coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public Type getType() {
        return type;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Tipo: " + type + ", Coordenadas: " + Arrays.toString(coordinates);
    }
}