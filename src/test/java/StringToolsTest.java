import org.junit.Test;
import static org.junit.Assert.*;

public class StringToolsTest {

    @Test
    public void _1234567890_whitelist() {
        StringTools stringTools = new StringTools();
        assertFalse(stringTools.containsCharsBesides("1234567890", "1234567890"));
        assertFalse(stringTools.containsCharsBesides("4568978896898989", "1234567890"));
        assertFalse(stringTools.containsCharsBesides("12634563363456345234634563634563634563563456345290", "1234567890"));

        assertTrue(stringTools.containsCharsBesides("1g23ccan45v67dmhf890", "1234567890"));
        assertTrue(stringTools.containsCharsBesides("////", "1234567890"));
        assertTrue(stringTools.containsCharsBesides("45k689gfhg788uyk968mnbnm9kydfgcvb8989", "1234567890"));
        assertTrue(stringTools.containsCharsBesides("12634rhrthtrh563363eh45rj63452346345yumyum6363456mgnfgnfg3634563ghmghmg5fgnf63456345290", "1234567890"));
    }

    @Test
    public void _abcdef_whitelist() {
        StringTools stringTools = new StringTools();
        assertFalse(stringTools.containsCharsBesides("aebdcdebcdcdebcdcdebcdcdebcdeef", "abcdef"));
        assertFalse(stringTools.containsCharsBesides("cdcdebcdcdebccdebcdcdebdcddcdebcdcdecdebcdcdebc", "abcdef"));
        assertFalse(stringTools.containsCharsBesides("abcdefabcdefabcdefabcdefabcdefabcdefabcdef", "abcdef"));

        assertTrue(stringTools.containsCharsBesides("1g23ccan45v67dmhf890", "abcdef"));
        assertTrue(stringTools.containsCharsBesides("ANDOWQNFOWNEGNWGOIENRGKLN", "abcdef"));
        assertTrue(stringTools.containsCharsBesides("!dSgdf%#$GFGGRTGHRHTgdf34g35", "abcdef"));
    }

    @Test
    public void _1234567890abcdefABCDEF_whitelist() {
        StringTools stringTools = new StringTools();
        assertFalse(stringTools.containsCharsBesides("1234567890abcdefABCDEF", "1234567890abcdefABCDEF"));
        assertFalse(stringTools.containsCharsBesides("12defABCD567890ab0a340abcdefABCD567890defABCD567890ab0aab0abcdefABCDcdefABCDEF", "1234567890abcdefABCDEF"));
        assertFalse(stringTools.containsCharsBesides("12634563367890abcde634563452346367890ab4234567890abcdefABC563634563634563563456345290", "1234567890abcdefABCDEF"));

        assertTrue(stringTools.containsCharsBesides("1g23ccan45v67dmhf890", "1234567890abcdefABCDEF"));
        assertTrue(stringTools.containsCharsBesides("45k689gfhg788uyk968mnbnm9kydfgcvb8989", "1234567890abcdefABCDEF"));
        assertTrue(stringTools.containsCharsBesides("12634rhrthtrh563363eh45rj63452346345yumyum6363456mgnfgnfg3634563ghmghmg5fgnf63456345290", "1234567890abcdefABCDEF"));
    }

}
