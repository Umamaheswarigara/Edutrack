package Main;

import java.util.*;

/**
 * Module 2: Suffix Structures for EduTrack Platform
 * Contains implementations of Suffix Array, SA-IS Algorithm, Kasai's LCP,
 * Suffix Tree, and Suffix Automaton.
 */
public class M2_SuffixStructures {

    // =========================================================================
    // 1. STANDARD SUFFIX ARRAY & BINARY SEARCH
    // =========================================================================
    
    public static class Suffix implements Comparable<Suffix> {
        public int index;
        public String suffixStr;

        public Suffix(int index, String suffixStr) {
            this.index = index;
            this.suffixStr = suffixStr;
        }

        @Override
        public int compareTo(Suffix other) {
            return this.suffixStr.compareTo(other.suffixStr);
        }
    }

    public static int[] buildSuffixArrayNaive(String text) {
        int n = text.length();
        Suffix[] suffixes = new Suffix[n];
        for (int i = 0; i < n; i++) {
            suffixes[i] = new Suffix(i, text.substring(i));
        }
        Arrays.sort(suffixes);
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = suffixes[i].index;
        }
        return sa;
    }

    public static boolean searchPatternSA(String text, int[] sa, String pattern) {
        int n = text.length();
        int m = pattern.length();
        int l = 0, r = n - 1;

        while (l <= r) {
            int mid = l + (r - l) / 2;
            int suffixIndex = sa[mid];
            String suffix = text.substring(suffixIndex, Math.min(suffixIndex + m, n));

            int res = pattern.compareTo(suffix);
            if (res == 0) {
                return true;
            } else if (res < 0) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return false;
    }

    // =========================================================================
    // 2. SA-IS (SUFFIX ARRAY - INDUCED SORTING) ALGORITHM
    // =========================================================================
    
    private static final boolean S_TYPE = true;
    private static final boolean L_TYPE = false;

    public static int[] buildSAIS(String text) {
        int n = text.length() + 1;
        int[] s = new int[n];
        int maxChar = 0;
        for (int i = 0; i < text.length(); i++) {
            s[i] = text.charAt(i) + 1;
            maxChar = Math.max(maxChar, s[i]);
        }
        s[n - 1] = 0;

        int[] sa = new int[n];
        sais(s, sa, n, maxChar + 1);

        int[] finalSA = new int[text.length()];
        System.arraycopy(sa, 1, finalSA, 0, text.length());
        return finalSA;
    }

    private static void sais(int[] s, int[] sa, int n, int K) {
        boolean[] type = new boolean[n];
        type[n - 1] = S_TYPE;
        for (int i = n - 2; i >= 0; i--) {
            if (s[i] < s[i + 1]) {
                type[i] = S_TYPE;
            } else if (s[i] > s[i + 1]) {
                type[i] = L_TYPE;
            } else {
                type[i] = type[i + 1];
            }
        }

        int[] lms = new int[n];
        int lmsCount = 0;
        for (int i = 1; i < n; i++) {
            if (type[i] == S_TYPE && type[i - 1] == L_TYPE) {
                lms[lmsCount++] = i;
            }
        }

        int[] bucket = new int[K];
        getBuckets(s, n, bucket, K);

        Arrays.fill(sa, -1);
        int[] tail = getTails(bucket, K);
        for (int i = 0; i < lmsCount; i++) {
            int idx = lms[i];
            sa[tail[s[idx]]--] = idx;
        }
        inducedSort(s, sa, type, n, K, bucket);

        int[] lmsOrder = new int[lmsCount];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (sa[i] > 0 && type[sa[i]] == S_TYPE && type[sa[i] - 1] == L_TYPE) {
                lmsOrder[idx++] = sa[i];
            }
        }

        int[] s1 = new int[lmsCount];
        int name = 0;
        s1[getLMSIndex(lmsOrder[0], lms, lmsCount)] = name;

        for (int i = 1; i < lmsCount; i++) {
            int prev = lmsOrder[i - 1];
            int curr = lmsOrder[i];
            if (!isLMSEqual(s, type, prev, curr)) {
                name++;
            }
            s1[getLMSIndex(curr, lms, lmsCount)] = name;
        }

        int[] sa1 = new int[lmsCount];
        if (name + 1 < lmsCount) {
            sais(s1, sa1, lmsCount, name + 1);
        } else {
            for (int i = 0; i < lmsCount; i++) {
                sa1[s1[i]] = i;
            }
        }

        Arrays.fill(sa, -1);
        tail = getTails(bucket, K);
        for (int i = lmsCount - 1; i >= 0; i--) {
            int origIdx = lms[sa1[i]];
            sa[tail[s[origIdx]]--] = origIdx;
        }
        inducedSort(s, sa, type, n, K, bucket);
    }

    private static void inducedSort(int[] s, int[] sa, boolean[] type, int n, int K, int[] bucket) {
        int[] head = getHeads(bucket, K);
        for (int i = 0; i < n; i++) {
            if (sa[i] > 0 && type[sa[i] - 1] == L_TYPE) {
                sa[head[s[sa[i] - 1]]++] = sa[i] - 1;
            }
        }

        int[] tail = getTails(bucket, K);
        for (int i = n - 1; i >= 0; i--) {
            if (sa[i] > 0 && type[sa[i] - 1] == S_TYPE) {
                sa[tail[s[sa[i] - 1]]--] = sa[i] - 1;
            }
        }
    }

    private static void getBuckets(int[] s, int n, int[] bucket, int K) {
        Arrays.fill(bucket, 0);
        for (int i = 0; i < n; i++) {
            bucket[s[i]]++;
        }
    }

    private static int[] getHeads(int[] bucket, int K) {
        int[] head = new int[K];
        int sum = 0;
        for (int i = 0; i < K; i++) {
            head[i] = sum;
            sum += bucket[i];
        }
        return head;
    }

    private static int[] getTails(int[] bucket, int K) {
        int[] tail = new int[K];
        int sum = 0;
        for (int i = 0; i < K; i++) {
            sum += bucket[i];
            tail[i] = sum - 1;
        }
        return tail;
    }

    private static int getLMSIndex(int target, int[] lms, int lmsCount) {
        for (int i = 0; i < lmsCount; i++) {
            if (lms[i] == target) return i;
        }
        return -1;
    }

    private static boolean isLMSEqual(int[] s, boolean[] type, int i, int j) {
        do {
            if (s[i] != s[j] || type[i] != type[j]) return false;
            i++;
            j++;
        } while (type[i] == L_TYPE || type[i - 1] == S_TYPE || type[j] == L_TYPE || type[j - 1] == S_TYPE);
        return s[i] == s[j] && type[i] == type[j];
    }

    // =========================================================================
    // 3. LCP ARRAY & KASAI'S ALGORITHM
    // =========================================================================
    
    public static int[] buildLCPKasai(String text, int[] sa) {
        int n = text.length();
        int[] rank = new int[n];
        int[] lcp = new int[n];

        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int k = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {
                k = 0;
                continue;
            }
            int j = sa[rank[i] - 1];
            while (i + k < n && j + k < n && text.charAt(i + k) == text.charAt(j + k)) {
                k++;
            }
            lcp[rank[i]] = k;
            if (k > 0) {
                k--;
            }
        }
        return lcp;
    }

    // =========================================================================
    // 4. SUFFIX TREE IMPLEMENTATION
    // =========================================================================
    
    public static class SuffixTreeNode {
        Map<Character, SuffixTreeNode> children = new HashMap<>();
        int start;
        int end;

        public SuffixTreeNode(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public static class SimpleSuffixTree {
        private final String text;
        private final SuffixTreeNode root = new SuffixTreeNode(-1, -1);

        public SimpleSuffixTree(String text) {
            this.text = text;
            buildTree();
        }

        private void buildTree() {
            int n = text.length();
            for (int i = 0; i < n; i++) {
                insertSuffix(i, n);
            }
        }

        private void insertSuffix(int suffixStart, int textLen) {
            SuffixTreeNode curr = root;
            int i = suffixStart;

            while (i < textLen) {
                char ch = text.charAt(i);
                if (!curr.children.containsKey(ch)) {
                    curr.children.put(ch, new SuffixTreeNode(i, textLen));
                    return;
                }

                SuffixTreeNode child = curr.children.get(ch);
                int edgeStart = child.start;
                int edgeEnd = child.end;
                int k = edgeStart;

                while (k < edgeEnd && i < textLen && text.charAt(k) == text.charAt(i)) {
                    k++;
                    i++;
                }

                if (k == edgeEnd) {
                    curr = child;
                } else {
                    SuffixTreeNode splitNode = new SuffixTreeNode(edgeStart, k);
                    child.start = k;
                    splitNode.children.put(text.charAt(k), child);
                    splitNode.children.put(text.charAt(i), new SuffixTreeNode(i, textLen));
                    curr.children.put(text.charAt(edgeStart), splitNode);
                    return;
                }
            }
        }

        public boolean searchSubstring(String pattern) {
            SuffixTreeNode curr = root;
            int i = 0;
            int m = pattern.length();

            while (i < m) {
                char ch = pattern.charAt(i);
                if (!curr.children.containsKey(ch)) {
                    return false;
                }

                SuffixTreeNode child = curr.children.get(ch);
                int k = child.start;
                int edgeEnd = child.end;

                while (k < edgeEnd && i < m) {
                    if (text.charAt(k) != pattern.charAt(i)) {
                        return false;
                    }
                    k++;
                    i++;
                }
                curr = child;
            }
            return true;
        }
    }

    // =========================================================================
    // 5. SUFFIX AUTOMATON (SAM) IMPLEMENTATION
    // =========================================================================
    
    public static class SuffixAutomatonState {
        int len;
        int link;
        Map<Character, Integer> next = new HashMap<>();

        public SuffixAutomatonState(int len, int link) {
            this.len = len;
            this.link = link;
        }
    }

    public static class SuffixAutomaton {
        List<SuffixAutomatonState> st = new ArrayList<>();
        int last;

        public SuffixAutomaton() {
            st.add(new SuffixAutomatonState(0, -1));
            last = 0;
        }

        public void extend(char c) {
            int cur = st.size();
            st.add(new SuffixAutomatonState(st.get(last).len + 1, -1));

            int p = last;
            while (p != -1 && !st.get(p).next.containsKey(c)) {
                st.get(p).next.put(c, cur);
                p = st.get(p).link;
            }

            if (p == -1) {
                st.get(cur).link = 0;
            } else {
                int q = st.get(p).next.get(c);
                if (st.get(p).len + 1 == st.get(q).len) {
                    st.get(cur).link = q;
                } else {
                    int clone = st.size();
                    SuffixAutomatonState qState = st.get(q);
                    SuffixAutomatonState cloneState = new SuffixAutomatonState(st.get(p).len + 1, qState.link);
                    cloneState.next.putAll(qState.next);
                    st.add(cloneState);

                    while (p != -1 && st.get(p).next.get(c) == q) {
                        st.get(p).next.put(c, clone);
                        p = st.get(p).link;
                    }
                    qState.link = clone;
                    st.get(cur).link = clone;
                }
            }
            last = cur;
        }

        public boolean searchSubstring(String pattern) {
            int curr = 0;
            for (char ch : pattern.toCharArray()) {
                if (!st.get(curr).next.containsKey(ch)) {
                    return false;
                }
                curr = st.get(curr).next.get(ch);
            }
            return true;
        }
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM2() {
        System.out.println("=============================================");
        System.out.println("          M2 - SUFFIX STRUCTURES             ");
        System.out.println("=============================================");

        String docText = "ACADEMIC_RESEARCH_ACADEMIC_PAPER$";
        System.out.println("Document Text: \"" + docText + "\"\n");

        // 1. Suffix Array & Substring Search
        System.out.println("--- 1. SUFFIX ARRAY & PATTERN SEARCH ---");
        int[] saNaive = buildSuffixArrayNaive(docText);
        System.out.println("Idx | SA[i] | Suffix");
        System.out.println("----------------------------------------");
        for (int i = 0; i < saNaive.length; i++) {
            System.out.printf("%-3d | %-5d | %s\n", i, saNaive[i], docText.substring(saNaive[i]));
        }
        
        String patternToSearch = "RESEARCH";
        boolean foundSA = searchPatternSA(docText, saNaive, patternToSearch);
        System.out.println("Binary Search on SA for Pattern \"" + patternToSearch + "\": " + (foundSA ? "FOUND" : "NOT FOUND"));

        // 2. SA-IS Algorithm Demonstration
        System.out.println("\n--- 2. SA-IS (SUFFIX ARRAY INDUCED SORTING) ---");
        String saisInput = "ACADEMIA";
        System.out.println("Input String for SA-IS: \"" + saisInput + "\"");
        int[] saIS = buildSAIS(saisInput);
        System.out.print("Generated Suffix Array (SA-IS): [");
        for (int i = 0; i < saIS.length; i++) {
            System.out.print(saIS[i] + (i < saIS.length - 1 ? ", " : ""));
        }
        System.out.println("]");

        // 3. LCP Array / Kasai's Algorithm Demonstration
        System.out.println("\n--- 3. LCP / KASAI'S ALGORITHM ---");
        int[] lcp = buildLCPKasai(docText, saNaive);
        System.out.println("Idx | SA[i] | LCP[i] | Suffix");
        System.out.println("--------------------------------------------------");
        int maxLcp = 0;
        int maxLcpIndex = -1;
        for (int i = 0; i < saNaive.length; i++) {
            System.out.printf("%-3d | %-5d | %-6d | %s\n", i, saNaive[i], lcp[i], docText.substring(saNaive[i]));
            if (lcp[i] > maxLcp) {
                maxLcp = lcp[i];
                maxLcpIndex = saNaive[i];
            }
        }
        if (maxLcpIndex != -1) {
            String repeatedPhrase = docText.substring(maxLcpIndex, maxLcpIndex + maxLcp);
            System.out.println("Longest Repeated Phrase Detected: \"" + repeatedPhrase + "\" (Length: " + maxLcp + ")");
        }

        // 4. Suffix Tree Demonstration
        System.out.println("\n--- 4. SUFFIX TREE ---");
        SimpleSuffixTree suffixTree = new SimpleSuffixTree(docText);
        String treeQuery1 = "PAPER";
        String treeQuery2 = "STUDENT";
        System.out.println("Search \"" + treeQuery1 + "\" in Suffix Tree: " + (suffixTree.searchSubstring(treeQuery1) ? "FOUND" : "NOT FOUND"));
        System.out.println("Search \"" + treeQuery2 + "\" in Suffix Tree: " + (suffixTree.searchSubstring(treeQuery2) ? "FOUND" : "NOT FOUND"));

        // 5. Suffix Automaton Demonstration
        System.out.println("\n--- 5. SUFFIX AUTOMATON (SAM) ---");
        SuffixAutomaton sam = new SuffixAutomaton();
        for (char c : docText.toCharArray()) {
            sam.extend(c);
        }
        String samQuery1 = "ACADEMIC";
        String samQuery2 = "SCIENCE";
        System.out.println("Search \"" + samQuery1 + "\" in Suffix Automaton: " + (sam.searchSubstring(samQuery1) ? "EXISTS" : "DOES NOT EXIST"));
        System.out.println("Search \"" + samQuery2 + "\" in Suffix Automaton: " + (sam.searchSubstring(samQuery2) ? "EXISTS" : "DOES NOT EXIST"));

        System.out.println("\n=============================================");
        System.out.println("         M2 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}