import java.util.*;

/** Independent small-input reference checks, run with assertions enabled. */
public class AlgorithmChecks {
    static Random random = new Random(253741);
    static int checks;
    static void check(boolean ok) { checks++; if (!ok) throw new AssertionError("Check " + checks); }
    static String dna(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) s.append("ACGT".charAt(random.nextInt(4)));
        return s.toString();
    }
    static int edit(String a, String b) {
        if (a.isEmpty()) return b.length();
        if (b.isEmpty()) return a.length();
        return Math.min(edit(a.substring(1), b.substring(1)) + (a.charAt(0) == b.charAt(0) ? 0 : 1),
                1 + Math.min(edit(a.substring(1), b), edit(a, b.substring(1))));
    }
    static int lcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            int j = 0, matched = 0;
            for (int i = 0; i < a.length(); i++) if ((mask & (1 << i)) != 0) {
                while (j < b.length() && b.charAt(j) != a.charAt(i)) j++;
                if (j == b.length()) break;
                j++; matched++;
            }
            if (matched == Integer.bitCount(mask)) best = Math.max(best, matched);
        }
        return best;
    }
    public static void main(String[] args) {
        for (int trial = 0; trial < 300; trial++) {
            String a = dna(random.nextInt(7)), b = dna(random.nextInt(7));
            check(Strains.editDistance(a, b) == edit(a, b));
            check(Strains.lcsLength(a, b) == lcs(a, b));
            List<Integer> hits = new ArrayList<>();
            for (int i = 0; i + b.length() <= a.length(); i++) if (a.startsWith(b, i)) hits.add(i);
            check(Arrays.equals(Marker.findAll(a, b), hits.stream().mapToInt(i -> i).toArray()));
            int[] pi = Marker.prefixFunction(a);
            for (int i = 0; i < a.length(); i++) {
                int border = 0;
                for (int k = 1; k <= i; k++) if (a.substring(0, k).equals(a.substring(i - k + 1, i + 1))) border = k;
                check(pi[i] == border);
            }
            for (int k = 1; k <= 8; k++) {
                int count = 0;
                for (int i = 0; i + k <= a.length(); i++) {
                    boolean found = false;
                    for (int j = 0; j + k <= b.length(); j++) if (a.regionMatches(i, b, j, k)) found = true;
                    if (found) count++;
                }
                check(Marker.sharedWords(a, b, k) == count);
            }
        }
        check(Arrays.equals(Marker.findAll("AAAAA", "AAA"), new int[]{0, 1, 2}));
        check(Marker.sharedWords("AAAAA", "AAAA", 3) == 3);
        check(Outbreak.longestChain(0, new int[0][]) == 0);
        for (int trial = 0; trial < 200; trial++) {
            int n = 1 + random.nextInt(10);
            int[] parent = new int[n], costs = new int[n];
            List<List<Integer>> lists = new ArrayList<>();
            for (int v = 0; v < n; v++) lists.add(new ArrayList<>());
            for (int v = 1; v < n; v++) {
                parent[v] = random.nextInt(v);
                lists.get(v).add(parent[v]); lists.get(parent[v]).add(v);
            }
            int[][] nb = new int[n][];
            for (int v = 0; v < n; v++) { nb[v] = lists.get(v).stream().mapToInt(i -> i).toArray(); costs[v] = random.nextInt(20); }
            for (int pass = 0; pass < 2; pass++) {
                if (pass == 1) Arrays.fill(costs, 1);
                long best = Long.MAX_VALUE;
                for (int mask = 0; mask < (1 << n); mask++) {
                    boolean valid = true; long sum = 0;
                    for (int v = 0; v < n; v++) {
                        if ((mask & (1 << v)) != 0) sum += costs[v];
                        if (v > 0 && (mask & ((1 << v) | (1 << parent[v]))) == 0) valid = false;
                    }
                    if (valid) best = Math.min(best, sum);
                }
                boolean[] selected = Outbreak.cheapestTests(n, nb, costs);
                long actual = 0;
                for (int v = 0; v < n; v++) {
                    if (selected[v]) actual += costs[v];
                    if (v > 0) check(selected[v] || selected[parent[v]]);
                }
                check(actual == best);
            }
            int diameter = 0;
            for (int start = 0; start < n; start++) {
                int[] dist = new int[n]; Arrays.fill(dist, -1); dist[start] = 0;
                Queue<Integer> q = new ArrayDeque<>(); q.add(start);
                while (!q.isEmpty()) {
                    int v = q.remove(); diameter = Math.max(diameter, dist[v]);
                    for (int u : nb[v]) if (dist[u] < 0) { dist[u] = dist[v] + 1; q.add(u); }
                }
            }
            check(Outbreak.longestChain(n, nb) == diameter);
        }
        System.out.println("Passed " + checks + " independent checks (seed 253741).");
    }
}
