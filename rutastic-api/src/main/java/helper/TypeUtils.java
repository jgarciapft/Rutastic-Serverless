package helper;

public class TypeUtils {
    public static boolean isANumber(String number) {
        return number.matches("-?[0-9]+");
    }

    public static boolean isABoolean(String bool) {
        return bool.matches("true|false");
    }
}
