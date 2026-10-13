package Main;

import java.util.*;

/**
 * Module 1: String Algorithms for EduTrack Platform
 * Contains implementations of KMP, Z-Function, Rabin-Karp, and Aho-Corasick.
 */
public class M1_StringAlgorithms {

    // =========================================================================
    // 1. KNUTH-MORRIS-PRATT (KMP) ALGORITHM
    // =========================================================================
    
    /**
     * Precomputes the Longest Prefix Suffix (LPS) array for KMP.
     */
    private static int[] computeLPSArray(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;

        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Searches for occurrences of a pattern in a text using KMP.
     */
    public static List<Integer> kmpSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) return matches;

        int[] lps = computeLPSArray(pattern);
        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                matches.add(i - j); // Match found
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return matches;
    }

    // =========================================================================
    // 2. Z-FUNCTION ALGORITHM
    // =========================================================================
    
    /**
     * Calculates the Z-array for a string S.
     */
    public static int[] calculateZFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;

        for (int i = 1; i < n; i++) {
            if (i <= r) {
                z[i] = Math.min(r - i + 1, z[i - l]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] - 1 > r) {
                l = i;
                r = i + z[i] - 1;
            }
        }
        return z;
    }

    /**
     * Searches for a pattern in assignment text using Z-Function.
     */
    public static List<Integer> zSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        String concat = pattern + "$" + text;
        int[] z = calculateZFunction(concat);
        int patternLen = pattern.length();

        for (int i = 0; i < z.length; i++) {
            if (z[i] == patternLen) {
                matches.add(i - patternLen - 1);
            }
        }
        return matches;
    }

    // =========================================================================
    // 3. RABIN-KARP ALGORITHM
    // =========================================================================
    
    /**
     * Rabin-Karp pattern searching algorithm using rolling hash.
     */
    public static List<Integer> rabinKarpSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (n < m) return matches;

        int d = 256; // Alphabet size
        int q = 101; // Prime modulus
        int p = 0;   // Pattern hash
        int t = 0;   // Text hash
        int h = 1;

        for (int i = 0; i < m - 1; i++) {
            h = (h * d) % q;
        }

        for (int i = 0; i < m; i++) {
            p = (d * p + pattern.charAt(i)) % q;
            t = (d * t + text.charAt(i)) % q;
        }

        for (int i = 0; i <= n - m; i++) {
            if (p == t) {
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matches.add(i);
                }
            }

            if (i < n - m) {
                t = (d * (t - text.charAt(i) * h) + text.charAt(i + m)) % q;
                if (t < 0) {
                    t = (t + q);
                }
            }
        }
        return matches;
    }

    // =========================================================================
    // 4. AHO-CORASICK MULTI-PATTERN SEARCH ALGORITHM
    // =========================================================================
    
    static class AhoCorasickNode {
        Map<Character, AhoCorasickNode> children = new HashMap<>();
        AhoCorasickNode failLink = null;
        List<String> output = new ArrayList<>();
    }

    public static class AhoCorasickMatch {
        public String keyword;
        public int position;

        public AhoCorasickMatch(String keyword, int position) {
            this.keyword = keyword;
            this.position = position;
        }
    }

    public static class AhoCorasick {
        private final AhoCorasickNode root = new AhoCorasickNode();

        public void insert(String pattern) {
            AhoCorasickNode curr = root;
            for (char ch : pattern.toCharArray()) {
                curr.children.putIfAbsent(ch, new AhoCorasickNode());
                curr = curr.children.get(ch);
            }
            curr.output.add(pattern);
        }

        public void buildFailureLinks() {
            Queue<AhoCorasickNode> queue = new LinkedList<>();

            for (AhoCorasickNode child : root.children.values()) {
                child.failLink = root;
                queue.add(child);
            }

            while (!queue.isEmpty()) {
                AhoCorasickNode curr = queue.poll();

                for (Map.Entry<Character, AhoCorasickNode> entry : curr.children.entrySet()) {
                    char ch = entry.getKey();
                    AhoCorasickNode child = entry.getValue();

                    AhoCorasickNode fail = curr.failLink;
                    while (fail != null && !fail.children.containsKey(ch)) {
                        fail = fail.failLink;
                    }

                    child.failLink = (fail != null) ? fail.children.get(ch) : root;
                    child.output.addAll(child.failLink.output);
                    queue.add(child);
                }
            }
        }

        public List<AhoCorasickMatch> search(String text) {
            List<AhoCorasickMatch> results = new ArrayList<>();
            AhoCorasickNode curr = root;

            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);

                while (curr != null && !curr.children.containsKey(ch)) {
                    curr = curr.failLink;
                }

                if (curr == null) {
                    curr = root;
                    continue;
                }

                curr = curr.children.get(ch);
                for (String pattern : curr.output) {
                    results.add(new AhoCorasickMatch(pattern, i - pattern.length() + 1));
                }
            }
            return results;
        }
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM1() {
        System.out.println("=============================================");
        System.out.println("          M1 - STRING ALGORITHMS             ");
        System.out.println("=============================================");

        // 1. KMP Demonstration
        System.out.println("\n--- 1. KMP (KNUTH-MORRIS-PRATT) ---");
        String kmpText = "Data Structures and Algorithms";
        String kmpPattern = "Algorithms";
        System.out.println("Text   : \"" + kmpText + "\"");
        System.out.println("Pattern: \"" + kmpPattern + "\"");
        List<Integer> kmpMatches = kmpSearch(kmpText, kmpPattern);
        if (!kmpMatches.isEmpty()) {
            for (int pos : kmpMatches) {
                System.out.println("Pattern found at index: " + pos);
            }
        } else {
            System.out.println("Pattern not found.");
        }

        // 2. Z-Function Demonstration
        System.out.println("\n--- 2. Z-FUNCTION ---");
        String zText = "Data Structures and Data Science in CSE Assignment";
        String zPattern = "Data";
        System.out.println("Text   : \"" + zText + "\"");
        System.out.println("Pattern: \"" + zPattern + "\"");
        String zConcat = zPattern + "$" + zText;
        int[] zArray = calculateZFunction(zConcat);
        System.out.print("Z-Array for concatenated string (" + zConcat + "):\n[");
        for (int i = 0; i < zArray.length; i++) {
            System.out.print(zArray[i] + (i < zArray.length - 1 ? ", " : ""));
        }
        System.out.println("]");
        List<Integer> zMatches = zSearch(zText, zPattern);
        System.out.println("Pattern matches at indices in text: " + zMatches);

        // 3. Rabin-Karp Demonstration
        System.out.println("\n--- 3. RABIN-KARP ---");
        String rkText = "CSDSA2026DBMS2026ML2026";
        String rkPattern = "DBMS2026";
        System.out.println("Text   : \"" + rkText + "\"");
        System.out.println("Pattern: \"" + rkPattern + "\"");
        List<Integer> rkMatches = rabinKarpSearch(rkText, rkPattern);
        for (int pos : rkMatches) {
            System.out.println("Matching position found at index: " + pos);
        }

        // 4. Aho-Corasick Demonstration
        System.out.println("\n--- 4. AHO-CORASICK (MULTI-PATTERN SEARCH) ---");
        String acText = "Welcome to EduTrack: We offer courses in DSA, DBMS, Machine Learning, and IoT for CSE students.";
        List<String> keywords = Arrays.asList("DSA", "DBMS", "Machine Learning", "IoT");
        System.out.println("Text    : \"" + acText + "\"");
        System.out.println("Keywords: " + keywords);
        
        AhoCorasick ac = new AhoCorasick();
        for (String kw : keywords) {
            ac.insert(kw);
        }
        ac.buildFailureLinks();
        List<AhoCorasickMatch> acMatches = ac.search(acText);

        System.out.println("Search Results:");
        for (AhoCorasickMatch match : acMatches) {
            System.out.println("  Keyword: \"" + match.keyword + "\" found at index " + match.position);
        }

        System.out.println("\n=============================================");
        System.out.println("         M1 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}