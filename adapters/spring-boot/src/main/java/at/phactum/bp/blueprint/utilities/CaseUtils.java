package at.phactum.bp.blueprint.utilities;

import java.util.regex.Pattern;

public class CaseUtils {

    private static final Pattern CAMEL_PATTERN = Pattern.compile("([a-z])([A-Z]+)");
    
    public static String camelToKebap(final String str) {
        
        return CAMEL_PATTERN
                .matcher(str)
                .replaceAll("$1-$2")
                .toLowerCase();

    }
    
}
