package Main;

import java.util.*;

/**
 * Module 5: NP-Completeness & Approximation Algorithms for EduTrack Platform
 * Contains SAT, 3-SAT, Clique, Independent Set, and 2-Approximation Vertex Cover.
 */
public class M5_NPCompleteness {

    // =========================================================================
    // 1. SAT SOLVER (BOOLEAN SATISFIABILITY)
    // =========================================================================
    
    public static class Literal {
        public int varIndex;
        public boolean isPositive;

        public Literal(int varIndex, boolean isPositive) {
            this.varIndex = varIndex;
            this.isPositive = isPositive;
        }
    }

    public static class Clause {
        public List<Literal> literals = new ArrayList<>();

        public void addLiteral(int varIndex, boolean isPositive) {
            literals.add(new Literal(varIndex, isPositive));
        }

        public boolean evaluate(boolean[] assignment) {
            for (Literal lit : literals) {
                boolean val = assignment[lit.varIndex];
                if (!lit.isPositive) val = !val;
                if (val) return true;
            }
            return false;
        }
    }

    public static class SATSolver {
        private final int numVars;
        private final List<Clause> clauses = new ArrayList<>();

        public SATSolver(int numVars) {
            this.numVars = numVars;
        }

        public void addClause(Clause clause) {
            clauses.add(clause);
        }

        public boolean solve(boolean[] assignment, int varIdx) {
            if (varIdx == numVars) {
                for (Clause c : clauses) {
                    if (!c.evaluate(assignment)) return false;
                }
                return true;
            }

            assignment[varIdx] = false;
            if (solve(assignment, varIdx + 1)) return true;

            assignment[varIdx] = true;
            if (solve(assignment, varIdx + 1)) return true;

            return false;
        }
    }

    // =========================================================================
    // 2. 3-SAT SOLVER
    // =========================================================================
    
    public static class ThreeSATSolver {
        private final int numVars;
        private final List<Clause> clauses = new ArrayList<>();

        public ThreeSATSolver(int numVars) {
            this.numVars = numVars;
        }

        public void add3SATClause(int v1, boolean p1, int v2, boolean p2, int v3, boolean p3) {
            Clause c = new Clause();
            c.addLiteral(v1, p1);
            c.addLiteral(v2, p2);
            c.addLiteral(v3, p3);
            clauses.add(c);
        }

        public boolean solve3SAT(boolean[] assignment, int varIdx) {
            if (varIdx == numVars) {
                for (Clause c : clauses) {
                    if (!c.evaluate(assignment)) return false;
                }
                return true;
            }

            assignment[varIdx] = true;
            if (solve3SAT(assignment, varIdx + 1)) return true;

            assignment[varIdx] = false;
            if (solve3SAT(assignment, varIdx + 1)) return true;

            return false;
        }
    }

    // =========================================================================
    // 3. MAXIMUM CLIQUE FINDER
    // =========================================================================
    
    public static class CliqueFinder {
        private final int n;
        private final boolean[][] graph;

        public CliqueFinder(int n, boolean[][] graph) {
            this.n = n;
            this.graph = graph;
        }

        public List<Integer> findMaxClique() {
            List<Integer> maxClique = new ArrayList<>();
            findCliqueDFS(0, new ArrayList<>(), maxClique);
            return maxClique;
        }

        private void findCliqueDFS(int v, List<Integer> current, List<Integer> maxClique) {
            if (current.size() > maxClique.size()) {
                maxClique.clear();
                maxClique.addAll(current);
            }

            for (int i = v; i < n; i++) {
                boolean canAdd = true;
                for (int u : current) {
                    if (!graph[u][i]) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    current.add(i);
                    findCliqueDFS(i + 1, current, maxClique);
                    current.remove(current.size() - 1);
                }
            }
        }
    }

    // =========================================================================
    // 4. MAXIMUM INDEPENDENT SET FINDER
    // =========================================================================
    
    public static class IndependentSetFinder {
        private final int n;
        private final boolean[][] graph;

        public IndependentSetFinder(int n, boolean[][] graph) {
            this.n = n;
            this.graph = graph;
        }

        public List<Integer> findMaxIndependentSet() {
            List<Integer> maxSet = new ArrayList<>();
            findIndSetDFS(0, new ArrayList<>(), maxSet);
            return maxSet;
        }

        private void findIndSetDFS(int v, List<Integer> current, List<Integer> maxSet) {
            if (current.size() > maxSet.size()) {
                maxSet.clear();
                maxSet.addAll(current);
            }

            for (int i = v; i < n; i++) {
                boolean canAdd = true;
                for (int u : current) {
                    if (graph[u][i]) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    current.add(i);
                    findIndSetDFS(i + 1, current, maxSet);
                    current.remove(current.size() - 1);
                }
            }
        }
    }

    // =========================================================================
    // 5. VERTEX COVER (2-APPROXIMATION ALGORITHM)
    // =========================================================================
    
    /**
     * 2-Approximation Algorithm for Vertex Cover using maximal matching edge selection.
     */
    public static List<Integer> approximateVertexCover(int n, List<int[]> edgeList) {
        Set<Integer> cover = new HashSet<>();
        boolean[] edgeVisited = new boolean[edgeList.size()];

        for (int i = 0; i < edgeList.size(); i++) {
            if (!edgeVisited[i]) {
                int u = edgeList.get(i)[0];
                int v = edgeList.get(i)[1];

                if (!cover.contains(u) && !cover.contains(v)) {
                    cover.add(u);
                    cover.add(v);
                }

                for (int j = 0; j < edgeList.size(); j++) {
                    int x = edgeList.get(j)[0];
                    int y = edgeList.get(j)[1];
                    if (cover.contains(x) || cover.contains(y)) {
                        edgeVisited[j] = true;
                    }
                }
            }
        }
        return new ArrayList<>(cover);
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM5() {
        System.out.println("=============================================");
        System.out.println("   M5 - NP-COMPLETENESS & APPROXIMATION      ");
        System.out.println("=============================================");

        // 1. SAT Demonstration
        System.out.println("\n--- 1. SAT (BOOLEAN SATISFIABILITY) ---");
        System.out.println("Academic Constraints: Course Scheduling Rules");
        System.out.println("  x0: Enable Online Mode | x1: Enable Morning Slot | x2: Enable Evening Slot");
        System.out.println("  Formula: (x0 OR x1) AND (NOT x0 OR x2)");

        SATSolver sat = new SATSolver(3);
        
        Clause c1 = new Clause();
        c1.addLiteral(0, true);
        c1.addLiteral(1, true);
        sat.addClause(c1);

        Clause c2 = new Clause();
        c2.addLiteral(0, false);
        c2.addLiteral(2, true);
        sat.addClause(c2);

        boolean[] satAssignment = new boolean[3];
        boolean isSat = sat.solve(satAssignment, 0);
        System.out.println("Satisfiable? " + (isSat ? "YES" : "NO"));
        if (isSat) {
            System.out.println("Satisfying Variable Assignment:");
            System.out.println("  x0 (Online Mode)  : " + satAssignment[0]);
            System.out.println("  x1 (Morning Slot) : " + satAssignment[1]);
            System.out.println("  x2 (Evening Slot) : " + satAssignment[2]);
        }

        // 2. 3-SAT Demonstration
        System.out.println("\n--- 2. 3-SAT (3-CONJUNCTIVE NORMAL FORM) ---");
        System.out.println("Formula: (x0 OR NOT x1 OR x2) AND (NOT x0 OR x1 OR x3)");
        ThreeSATSolver sat3 = new ThreeSATSolver(4);
        sat3.add3SATClause(0, true, 1, false, 2, true);
        sat3.add3SATClause(0, false, 1, true, 3, true);

        boolean[] sat3Assign = new boolean[4];
        boolean is3Sat = sat3.solve3SAT(sat3Assign, 0);
        System.out.println("3-SAT Satisfiable? " + (is3Sat ? "YES" : "NO"));
        if (is3Sat) {
            System.out.println("Satisfying 3-SAT Assignment: [x0=" + sat3Assign[0] 
                               + ", x1=" + sat3Assign[1] + ", x2=" + sat3Assign[2] + ", x3=" + sat3Assign[3] + "]");
        }

        String[] subjects = {"DSA", "DBMS", "OS", "CN", "AI"};
        int numSubjects = subjects.length;
        boolean[][] adjMatrix = {
            {false, true,  true,  false, true},
            {true,  false, true,  true,  false},
            {true,  true,  false, true,  true},
            {false, true,  true,  false, false},
            {true,  false, true,  false, false}
        };

        // 3. CLIQUE Demonstration
        System.out.println("\n--- 3. CLIQUE (FACULTY COLLABORATION NETWORK) ---");
        CliqueFinder cliqueFinder = new CliqueFinder(numSubjects, adjMatrix);
        List<Integer> maxClique = cliqueFinder.findMaxClique();
        System.out.println("Maximum Clique Found (Fully Connected Subgroup):");
        System.out.print("  Selected Subjects: ");
        for (int idx : maxClique) {
            System.out.print(subjects[idx] + " ");
        }
        System.out.println("\n  Clique Size: " + maxClique.size());

        // 4. INDEPENDENT SET Demonstration
        System.out.println("\n--- 4. INDEPENDENT SET (NON-CONFLICTING EXAM SLOTS) ---");
        IndependentSetFinder indFinder = new IndependentSetFinder(numSubjects, adjMatrix);
        List<Integer> maxIndSet = indFinder.findMaxIndependentSet();
        System.out.println("Maximum Independent Set Found (Courses that can share same exam slot):");
        System.out.print("  Selected Subjects: ");
        for (int idx : maxIndSet) {
            System.out.print(subjects[idx] + " ");
        }
        System.out.println("\n  Independent Set Size: " + maxIndSet.size());

        // 5. VERTEX COVER Demonstration
        System.out.println("\n--- 5. VERTEX COVER (2-APPROXIMATION ALGORITHM) ---");
        List<int[]> edgeList = new ArrayList<>();
        for (int i = 0; i < numSubjects; i++) {
            for (int j = i + 1; j < numSubjects; j++) {
                if (adjMatrix[i][j]) {
                    edgeList.add(new int[]{i, j});
                }
            }
        }

        List<Integer> approxCover = approximateVertexCover(numSubjects, edgeList);
        System.out.println("2-Approximate Vertex Cover (Min Monitor Nodes to Watch All Exam Conflicts):");
        System.out.print("  Selected Monitor Subjects: ");
        for (int idx : approxCover) {
            System.out.print(subjects[idx] + " ");
        }
        System.out.println("\n  Cover Size: " + approxCover.size());
        System.out.println("  Approximation Guarantee: Cover Size <= 2 * OPT");

        System.out.println("\n=============================================");
        System.out.println("         M5 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}