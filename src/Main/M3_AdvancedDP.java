package Main;

import java.util.*;

/**
 * Module 3: Advanced Dynamic Programming for EduTrack Platform
 * Contains Levenshtein, Damerau-Levenshtein, Matrix Chain Multiplication,
 * Bitmask DP, and Optimal Binary Search Tree.
 */
public class M3_AdvancedDP {

    // =========================================================================
    // 1. LEVENSHTEIN DISTANCE
    // =========================================================================
    
    public static int computeLevenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j],
                                  Math.min(dp[i][j - 1],
                                           dp[i - 1][j - 1]));
                }
            }
        }
        return dp[m][n];
    }

    public static String spellCheck(String query, String[] dictionary) {
        String bestMatch = query;
        int minDistance = Integer.MAX_VALUE;

        for (String word : dictionary) {
            int dist = computeLevenshteinDistance(query, word);
            if (dist < minDistance) {
                minDistance = dist;
                bestMatch = word;
            }
        }
        return bestMatch;
    }

    // =========================================================================
    // 2. DAMERAU-LEVENSHTEIN DISTANCE
    // =========================================================================
    
    public static int computeDamerauLevenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                           Math.min(dp[i][j - 1] + 1,
                                    dp[i - 1][j - 1] + cost));

                if (i > 1 && j > 1 
                        && s1.charAt(i - 1) == s2.charAt(j - 2) 
                        && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost);
                }
            }
        }
        return dp[m][n];
    }

    // =========================================================================
    // 3. MATRIX CHAIN MULTIPLICATION (MCM)
    // =========================================================================
    
    public static class MCMResult {
        public int minCost;
        public String parenthesization;

        public MCMResult(int minCost, String parenthesization) {
            this.minCost = minCost;
            this.parenthesization = parenthesization;
        }
    }

    public static MCMResult matrixChainOrder(int[] p) {
        int n = p.length - 1;
        int[][] m = new int[n + 1][n + 1];
        int[][] s = new int[n + 1][n + 1];

        for (int i = 1; i <= n; i++) {
            m[i][i] = 0;
        }

        for (int L = 2; L <= n; L++) {
            for (int i = 1; i <= n - L + 1; i++) {
                int j = i + L - 1;
                m[i][j] = Integer.MAX_VALUE;
                for (int k = i; k <= j - 1; k++) {
                    int q = m[i][k] + m[k + 1][j] + p[i - 1] * p[k] * p[j];
                    if (q < m[i][j]) {
                        m[i][j] = q;
                        s[i][j] = k;
                    }
                }
            }
        }

        String paren = getOptimalParenthesization(s, 1, n);
        return new MCMResult(m[1][n], paren);
    }

    private static String getOptimalParenthesization(int[][] s, int i, int j) {
        if (i == j) {
            return "A" + i;
        } else {
            return "(" + getOptimalParenthesization(s, i, s[i][j]) + " " 
                     + getOptimalParenthesization(s, s[i][j] + 1, j) + ")";
        }
    }

    // =========================================================================
    // 4. BITMASK DYNAMIC PROGRAMMING
    // =========================================================================
    
    public static class BitmaskCourseSelection {
        public int maxUtility;
        public List<String> selectedCourses;
        public int totalCredits;

        public BitmaskCourseSelection(int maxUtility, List<String> selectedCourses, int totalCredits) {
            this.maxUtility = maxUtility;
            this.selectedCourses = selectedCourses;
            this.totalCredits = totalCredits;
        }
    }

    public static BitmaskCourseSelection solveBitmaskDP(String[] courseNames, int[] credits, int[] utility, int maxCreditLimit) {
        int n = courseNames.length;
        int numStates = 1 << n;
        
        int[] dpUtility = new int[numStates];
        int[] dpCredits = new int[numStates];
        
        int bestMask = 0;
        int maxUtility = 0;

        for (int mask = 0; mask < numStates; mask++) {
            int currentCredits = 0;
            int currentUtility = 0;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    currentCredits += credits[i];
                    currentUtility += utility[i];
                }
            }

            dpCredits[mask] = currentCredits;
            dpUtility[mask] = currentUtility;

            if (currentCredits <= maxCreditLimit) {
                if (currentUtility > maxUtility) {
                    maxUtility = currentUtility;
                    bestMask = mask;
                }
            }
        }

        List<String> chosen = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                chosen.add(courseNames[i]);
            }
        }

        return new BitmaskCourseSelection(maxUtility, chosen, dpCredits[bestMask]);
    }

    // =========================================================================
    // 5. OPTIMAL BINARY SEARCH TREE (OBST)
    // =========================================================================
    
    public static class OBSTResult {
        public double minCost;
        public int[][] rootTable;
        public String[] keys;

        public OBSTResult(double minCost, int[][] rootTable, String[] keys) {
            this.minCost = minCost;
            this.rootTable = rootTable;
            this.keys = keys;
        }
    }

    public static OBSTResult optimalBST(String[] keys, double[] freq) {
        int n = keys.length;
        double[][] e = new double[n + 2][n + 2];
        double[][] w = new double[n + 2][n + 2];
        int[][] root = new int[n + 1][n + 1];

        for (int i = 1; i <= n + 1; i++) {
            e[i][i - 1] = 0;
            w[i][i - 1] = 0;
        }

        for (int l = 1; l <= n; l++) {
            for (int i = 1; i <= n - l + 1; i++) {
                int j = i + l - 1;
                e[i][j] = Double.MAX_VALUE;
                w[i][j] = w[i][j - 1] + freq[j - 1];

                for (int r = i; r <= j; r++) {
                    double t = e[i][r - 1] + e[r + 1][j] + w[i][j];
                    if (t < e[i][j]) {
                        e[i][j] = t;
                        root[i][j] = r;
                    }
                }
            }
        }
        return new OBSTResult(e[1][n], root, keys);
    }

    public static void printOBSTStructure(int[][] root, String[] keys, int i, int j, String parent, String childType) {
        if (i <= j) {
            int r = root[i][j];
            String currentKey = keys[r - 1];
            if (parent.isEmpty()) {
                System.out.println("Root of Tree: \"" + currentKey + "\"");
            } else {
                System.out.println("  " + childType + " child of \"" + parent + "\" is \"" + currentKey + "\"");
            }
            printOBSTStructure(root, keys, i, r - 1, currentKey, "Left");
            printOBSTStructure(root, keys, r + 1, j, currentKey, "Right");
        }
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM3() {
        System.out.println("=============================================");
        System.out.println("     M3 - ADVANCED DYNAMIC PROGRAMMING       ");
        System.out.println("=============================================");

        // 1. Levenshtein Distance Demonstration
        System.out.println("\n--- 1. LEVENSHTEIN DISTANCE (SPELLING CORRECTION) ---");
        String misspelledQuery = "Algoritm";
        String[] courseCatalog = {"Algorithm", "Database", "Operating Systems", "Networking"};
        int levDist = computeLevenshteinDistance(misspelledQuery, "Algorithm");
        String suggested = spellCheck(misspelledQuery, courseCatalog);
        System.out.println("Student Query      : \"" + misspelledQuery + "\"");
        System.out.println("Edit Distance      : " + levDist);
        System.out.println("Suggested Correction: \"" + suggested + "\"");

        // 2. Damerau-Levenshtein Distance Demonstration
        System.out.println("\n--- 2. DAMERAU-LEVENSHTEIN DISTANCE (TRANSPOSITION) ---");
        String transposedQuery = "Aglorithm";
        String targetWord = "Algorithm";
        int damLevDist = computeDamerauLevenshteinDistance(transposedQuery, targetWord);
        System.out.println("Transposed Input   : \"" + transposedQuery + "\"");
        System.out.println("Target Word        : \"" + targetWord + "\"");
        System.out.println("Damerau-Levenshtein Distance: " + damLevDist + " (Handles adjacent swap 'g' <-> 'l')");

        // 3. Matrix Chain Multiplication Demonstration
        System.out.println("\n--- 3. MATRIX CHAIN MULTIPLICATION ---");
        int[] matrixDims = {10, 20, 30, 40};
        System.out.println("Matrix Dimensions: [10x20, 20x30, 30x40]");
        MCMResult mcm = matrixChainOrder(matrixDims);
        System.out.println("Minimum Scalar Multiplications Cost: " + mcm.minCost);
        System.out.println("Optimal Parenthesization          : " + mcm.parenthesization);

        // 4. Bitmask Dynamic Programming Demonstration
        System.out.println("\n--- 4. BITMASK DP (FEASIBLE COURSE SELECTION) ---");
        String[] courses = {"DSA", "DBMS", "Machine Learning", "Cloud Computing"};
        int[] credits = {3, 4, 2, 5};
        int[] values = {30, 50, 20, 60};
        int maxCreditLimit = 7;

        System.out.println("Available Courses & Credits:");
        for (int i = 0; i < courses.length; i++) {
            System.out.println("  " + courses[i] + " -> Credits: " + credits[i] + ", Knowledge Value: " + values[i]);
        }
        System.out.println("Max Credit Limit: " + maxCreditLimit);

        BitmaskCourseSelection selection = solveBitmaskDP(courses, credits, values, maxCreditLimit);
        System.out.println("\nOptimal Selection Result:");
        System.out.println("  Selected Courses: " + selection.selectedCourses);
        System.out.println("  Total Credits   : " + selection.totalCredits + " / " + maxCreditLimit);
        System.out.println("  Max Total Value : " + selection.maxUtility);

        // 5. Optimal Binary Search Tree Demonstration
        System.out.println("\n--- 5. OPTIMAL BINARY SEARCH TREE (OBST) ---");
        String[] keys = {"DBMS", "DSA", "ML", "OS"};
        double[] freq = {30.0, 50.0, 10.0, 20.0};

        System.out.println("Keys & Search Frequencies:");
        for (int i = 0; i < keys.length; i++) {
            System.out.println("  " + keys[i] + " -> Frequency: " + freq[i]);
        }

        OBSTResult obst = optimalBST(keys, freq);
        System.out.println("\nMinimum Expected Search Cost: " + obst.minCost);
        System.out.println("Optimal Tree Structure:");
        printOBSTStructure(obst.rootTable, keys, 1, keys.length, "", "");

        System.out.println("\n=============================================");
        System.out.println("         M3 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}