public class StringTools {

    public boolean containsCharsBesides(String stringToCheck, String whitelistChars) {
        char[] allowedChars = whitelistChars.toCharArray();
        int checkedCharCount = 0;
        for (char c : stringToCheck.toCharArray()) {
            for (char allowedChar : allowedChars) {
                if (c != allowedChar) {
                    checkedCharCount++;
                }
            }
            if (checkedCharCount == allowedChars.length) {
                return true;
            }
            checkedCharCount = 0;
        }
        return false;
    }

}
