import java.util.prefs.Preferences;

/**
 * Removes the Supabase auth session from the JVM user-root preferences.
 *
 * supabase-kt persists the session via java.util.prefs.Preferences as a top-level
 * `sb-<ref>-supabase-co-session` key. Only `sb-*`/`supabase*` keys are removed, leaving
 * sibling IDE child nodes (google/jetbrains/...) intact. Run as a single-file source
 * program: `java ClearSupabaseSession.java`. Invoked by scripts/reset-desktop-data.sh.
 */
public class ClearSupabaseSession {
    public static void main(String[] args) throws Exception {
        Preferences root = Preferences.userRoot();
        int removed = 0;
        for (String key : root.keys()) {
            if (key.startsWith("sb-") || key.startsWith("supabase")) {
                root.remove(key);
                System.out.println("Removed Preferences key: " + key);
                removed++;
            }
        }
        root.flush();
        if (removed == 0) {
            System.out.println("No Supabase auth session found in user preferences.");
        }
    }
}
