package Main;

import java.util.*;

/**
 * Module 4: Network Flow & Conflict Analysis for EduTrack Platform
 * Contains Bipartite Matching, Dinic's Maximum Flow, and Exam Conflict Analysis.
 */
public class M4_NetworkFlow {

    // =========================================================================
    // 1. BIPARTITE MATCHING (FACULTY - COURSE ALLOCATION)
    // =========================================================================
    
    public static class BipartiteMatcher {
        private final int numFaculties;
        private final int numCourses;
        private final List<List<Integer>> adj;

        public BipartiteMatcher(int numFaculties, int numCourses) {
            this.numFaculties = numFaculties;
            this.numCourses = numCourses;
            this.adj = new ArrayList<>();
            for (int i = 0; i < numFaculties; i++) {
                adj.add(new ArrayList<>());
            }
        }

        public void addEdge(int faculty, int course) {
            adj.get(faculty).add(course);
        }

        private boolean dfs(int u, boolean[] visited, int[] matchC) {
            for (int v : adj.get(u)) {
                if (!visited[v]) {
                    visited[v] = true;
                    if (matchC[v] < 0 || dfs(matchC[v], visited, matchC)) {
                        matchC[v] = u;
                        return true;
                    }
                }
            }
            return false;
        }

        public Map<Integer, Integer> maxBipartiteMatching() {
            int[] matchC = new int[numCourses];
            Arrays.fill(matchC, -1);

            for (int f = 0; f < numFaculties; f++) {
                boolean[] visited = new boolean[numCourses];
                if (dfs(f, visited, matchC)) {
                }
            }

            Map<Integer, Integer> assignments = new LinkedHashMap<>();
            for (int c = 0; c < numCourses; c++) {
                if (matchC[c] != -1) {
                    assignments.put(matchC[c], c);
                }
            }
            return assignments;
        }
    }

    // =========================================================================
    // 2. DINIC'S MAXIMUM FLOW ALGORITHM
    // =========================================================================
    
    public static class FlowEdge {
        public int u, v;
        public int capacity;
        public int flow;
        public int revIndex;

        public FlowEdge(int u, int v, int capacity, int revIndex) {
            this.u = u;
            this.v = v;
            this.capacity = capacity;
            this.flow = 0;
            this.revIndex = revIndex;
        }
    }

    public static class Dinic {
        private final List<List<FlowEdge>> adj;
        private final int[] level;
        private final int[] ptr;

        public Dinic(int n) {
            this.adj = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                adj.add(new ArrayList<>());
            }
            this.level = new int[n];
            this.ptr = new int[n];
        }

        public void addEdge(int u, int v, int capacity) {
            FlowEdge a = new FlowEdge(u, v, capacity, adj.get(v).size());
            FlowEdge b = new FlowEdge(v, u, 0, adj.get(u).size());
            adj.get(u).add(a);
            adj.get(v).add(b);
        }

        private boolean bfs(int s, int t) {
            Arrays.fill(level, -1);
            level[s] = 0;
            Queue<Integer> q = new LinkedList<>();
            q.add(s);

            while (!q.isEmpty()) {
                int u = q.poll();
                for (FlowEdge edge : adj.get(u)) {
                    if (edge.capacity - edge.flow > 0 && level[edge.v] == -1) {
                        level[edge.v] = level[u] + 1;
                        q.add(edge.v);
                    }
                }
            }
            return level[t] != -1;
        }

        private int dfs(int u, int t, int pushed) {
            if (pushed == 0 || u == t) return pushed;

            for (; ptr[u] < adj.get(u).size(); ptr[u]++) {
                FlowEdge edge = adj.get(u).get(ptr[u]);
                int tr = edge.v;

                if (level[u] + 1 != level[tr] || edge.capacity - edge.flow == 0) continue;

                int trPushed = dfs(tr, t, Math.min(pushed, edge.capacity - edge.flow));
                if (trPushed == 0) continue;

                edge.flow += trPushed;
                adj.get(tr).get(edge.revIndex).flow -= trPushed;
                return trPushed;
            }
            return 0;
        }

        public int maxFlow(int s, int t) {
            int flow = 0;
            while (bfs(s, t)) {
                Arrays.fill(ptr, 0);
                while (true) {
                    int pushed = dfs(s, t, Integer.MAX_VALUE);
                    if (pushed == 0) break;
                    flow += pushed;
                }
            }
            return flow;
        }
    }

    // =========================================================================
    // 3. CONFLICT ANALYSIS & EXAM SCHEDULING GRAPH
    // =========================================================================
    
    public static class ConflictGraph {
        private final String[] subjects;
        private final int[][] sharedStudents;

        public ConflictGraph(String[] subjects, int[][] sharedStudents) {
            this.subjects = subjects;
            this.sharedStudents = sharedStudents;
        }

        public void printConflictAnalysis() {
            int n = subjects.length;
            int conflictCount = 0;
            System.out.println("Exam Scheduling Conflict Graph Analysis:");
            System.out.println("--------------------------------------------------");

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (sharedStudents[i][j] > 0) {
                        conflictCount++;
                        System.out.println("  [CONFLICT DETECTED] " + subjects[i] + " <---> " 
                                           + subjects[j] + " | Shared Students: " + sharedStudents[i][j]);
                    }
                }
            }
            System.out.println("--------------------------------------------------");
            System.out.println("Total Conflicts Identified: " + conflictCount);
            System.out.println("Scheduling Insight: Subjects in conflict CANNOT be scheduled in the same exam slot.");
        }
    }

    // =========================================================================
    // MODULE RUNNER METHOD
    // =========================================================================
    public static void runM4() {
        System.out.println("=============================================");
        System.out.println("          M4 - NETWORK FLOW                  ");
        System.out.println("=============================================");

        // 1. Bipartite Matching Demonstration
        System.out.println("\n--- 1. BIPARTITE MATCHING (FACULTY-COURSE ALLOCATION) ---");
        String[] faculties = {"F1 (Dr. Smith)", "F2 (Dr. Johnson)", "F3 (Dr. Williams)"};
        String[] courses = {"DSA", "DBMS", "ML", "IoT"};

        BipartiteMatcher matcher = new BipartiteMatcher(faculties.length, courses.length);
        matcher.addEdge(0, 0); // F1 -> DSA
        matcher.addEdge(0, 1); // F1 -> DBMS
        matcher.addEdge(1, 1); // F2 -> DBMS
        matcher.addEdge(1, 2); // F2 -> ML
        matcher.addEdge(2, 2); // F3 -> ML
        matcher.addEdge(2, 3); // F3 -> IoT

        System.out.println("Faculty Preferences:");
        System.out.println("  F1 -> DSA, DBMS");
        System.out.println("  F2 -> DBMS, ML");
        System.out.println("  F3 -> ML, IoT");

        Map<Integer, Integer> matching = matcher.maxBipartiteMatching();
        System.out.println("\nMaximum Bipartite Matching Result:");
        for (Map.Entry<Integer, Integer> entry : matching.entrySet()) {
            System.out.println("  " + faculties[entry.getKey()] + " === Assigned To ===> " + courses[entry.getValue()]);
        }
        System.out.println("Total Courses Allocated: " + matching.size());

        // 2. Dinic's Algorithm Demonstration
        System.out.println("\n--- 2. DINIC'S MAXIMUM FLOW (CAMPUS RESOURCE ALLOCATION) ---");
        int numNodes = 6;
        Dinic dinic = new Dinic(numNodes);

        dinic.addEdge(0, 1, 10);
        dinic.addEdge(0, 2, 15);
        dinic.addEdge(1, 3, 5);
        dinic.addEdge(1, 4, 8);
        dinic.addEdge(2, 4, 10);
        dinic.addEdge(3, 5, 10);
        dinic.addEdge(4, 5, 12);

        System.out.println("Flow Network Concept: Student Workload Distribution across Campus Labs");
        System.out.println("Source -> Departments -> Labs -> Central System");

        int maxFlowValue = dinic.maxFlow(0, 5);
        System.out.println("Calculated Maximum Resource Flow: " + maxFlowValue + " units/sec");

        // 3. Conflict Analysis Demonstration
        System.out.println("\n--- 3. CONFLICT ANALYSIS (EXAM SCHEDULING GRAPH) ---");
        String[] examSubjects = {"Data Structures", "Database Systems", "Machine Learning", "Computer Networks", "Cyber Security"};
        int[][] overlapMatrix = {
            {0, 25,  0, 15,  0},
            {25, 0, 30,  0, 10},
            {0, 30,  0,  5, 20},
            {15, 0,  5,  0, 40},
            {0, 10, 20, 40,  0}
        };

        ConflictGraph cGraph = new ConflictGraph(examSubjects, overlapMatrix);
        cGraph.printConflictAnalysis();

        System.out.println("\n=============================================");
        System.out.println("         M4 EXECUTION COMPLETED              ");
        System.out.println("=============================================");
    }
}