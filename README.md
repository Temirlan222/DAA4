# Assignment 4: Patient Zero

Student ID: **253741**  
Name: **Temirlan Mukhametov**  
Group: **SE-2532**

Java implementations of edit distance, LCS, KMP, Rabin-Karp and dynamic programming on an infection tree. The individual input files were generated with `java tools/MakeVariant.java 253741 data`.

## Run

Use JDK 17 or newer, from the repository root:

```sh
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
java tools/Check.java
java -cp out tools/AlgorithmChecks.java
java -cp out Bench
```

If wildcard compilation fails in your terminal, `java tools/Check.java` compiles the source files itself. Open `output/outbreak.html` in a browser to see the lab console.

## Program output

```text
Mutations: 80
Shared core: 1236
Marker hits: 7
First hit: 9973
Border length: 3
Shared words: 572
Tests needed: 41
Cheapest testing: 202
Longest chain: 36
```

The generated interactive report is [output/outbreak.html](output/outbreak.html). Download/open it locally; GitHub displays its source.

**Screenshot pending:** capture the whole generated page in a browser and save it as `output/outbreak.png`. Automated capture was blocked by the environment; no substitute image has been fabricated.

## Questions

### Mission 1: edit distance

`d[i][j]` is the minimum number of edits needed to turn the first `i` letters of A into the first `j` letters of B. I take the minimum of deletion (`d[i-1][j]+1`), insertion (`d[i][j-1]+1`) and replacement (`d[i-1][j-1]+cost`), where cost is 0 for equal letters and 1 otherwise. The first column is `i` and the first row is `j`, because converting to or from an empty string needs that many deletions or insertions.

### Mission 2: shared core

A subsequence keeps the order of letters but can skip letters; a substring must be continuous. Comparing only the same positions loses matches after an insertion or deletion shifts the sequence, while LCS can still match those letters. In this variant the shared core has length 1236.

### Mission 3: prefix function and KMP

`pi[i]` gives the length of the longest proper prefix of the pattern up to `i` that is also its suffix. On a mismatch, KMP uses `pi[j-1]` to retain a shorter already-matched prefix, so the text pointer never moves backwards; after a full match this also preserves overlapping occurrences. My marker is `ATCAAATC`, its prefix array is `[0, 0, 0, 1, 1, 1, 2, 3]`, and its border length is 3 because `ATC` is both the beginning and the ending.

### Mission 4: Rabin-Karp

Different words can have equal hashes, so my code compares every letter before accepting a candidate. It stores starting positions in B by hash, then counts each matching position in A once, even if that word appears many times in B. With many colliding unequal windows, it may compare every A window with every B window, giving worst-case `O(n*m*k)` time.

### Mission 5: tree DP

`dp[v][1]` is the minimum cost for the subtree when v is tested, and `dp[v][0]` is the minimum when v is not tested: the first adds the cheaper state of each child, while the second must add every child's tested state. I choose the cheaper root state and reconstruct the selected people using the same rule; equal costs prefer leaving a person untested unless the parent forces them to be tested. Greedy selection by degree ignores costs: in a star with three leaves, a centre costing 10 and leaves costing 1 each, choosing the centre costs 10 while choosing all leaves costs only 3.

The diameter uses the two longest downward branches at each vertex. Their sum is the longest path passing through that vertex; the maximum over all vertices is the answer in edges.

## AI use

OpenAI Codex (ChatGPT) was used to generate the implementations in `Strains.java`, `Marker.java` and `Outbreak.java`, generate additional tests, draft the README explanations, run checks and benchmarks, and prepare Git commits. This is substantial AI assistance, not only code review. The student must review, understand and personally revise the generated work before submission in accordance with the assignment rules; this repository does not claim that this review has already happened.

The starter code, lab visualisation, examples, generator and benchmark were supplied with the assignment. No external code or textbook passages were copied into the implementations.

## Timing (Mission 7)

Actual output of the supplied, unchanged `Bench` class on Windows with OpenJDK 25.0.1. Each number is the average of 3 runs, in milliseconds.

| Test | Size | Time (ms) |
|---|---|---|
| Naive search | 20,000 / 40,000 / 80,000 | 54.5 / 128.9 / 237.1 |
| KMP | 20,000 / 40,000 / 80,000 | 2.5 / 2.6 / 0.9 |
| Edit distance | 500 / 1,000 / 2,000 / 4,000 | 14.5 / 13.3 / 35.6 / 89.6 |

With fixed pattern length 2000, doubling the text should roughly double both naive and KMP work, although naive search repeats far more comparisons here; measured naive ratios are 2.37 and 1.84. Doubling both strain lengths should roughly quadruple the number of edit-distance cells, but measured time ratios are 0.92, 2.68 and 2.52. This short benchmark has no separate warm-up, so JVM compilation, allocation and timer noise affect the results, especially KMP's 1.04 and 0.35 ratios; the observed decrease does not mean KMP has decreasing complexity.

## Complexity

For the first two rows n and m are the two strain lengths. For KMP n is the text length and m the pattern length. For shared words n and m are the lengths of A and B, k is the window length (12 in Main), and h is the number of reported KMP hits. v is the number of vertices in the tree.

| Algorithm | Time | Extra space | Assumptions |
|---|---|---|---|
| Edit distance | O((n+1)(m+1)) | O((n+1)(m+1)) | Full table, constant-cost insert/delete/replace; O(n^2) for equal lengths |
| LCS | O((n+1)(m+1)) | O((n+1)(m+1)) | Full table; O(n^2) for equal lengths |
| Prefix function and KMP | O(n+m) | O(m+h) | Prefix array plus returned matches/list; overlaps included |
| Rabin-Karp shared words | Expected O(n+m+k+q*k); worst O(n*m*k) | O(m) | q is the number of candidate windows actually compared; expected constant-time map operations, exact comparisons cost up to k; fixed k and rare collisions give near-linear time |
| Tree DP and reconstruction; diameter | O(v) | O(v) | Input is a tree with v-1 edges; arrays and recursive stack, each edge visited a constant number of times |

Tree costs are accumulated in `long`. Recursive tree traversal is intended for the assignment's approximately 100 vertices; an extremely deep tree outside that scope could overflow the call stack. Empty patterns match all text boundaries, and non-positive Rabin-Karp window lengths are rejected.

## Validation

- `java tools/Check.java`: all 9 output lines match for tiny, small and dense.
- `java -cp out tools/AlgorithmChecks.java`: 6541 independent checks passed with fixed seed 253741. Small strings are checked against recursive edit distance, enumerated subsequences and direct matching; random trees are checked against exhaustive vertex covers and all-source BFS diameters.
- Source compilation with `javac --release 17` succeeded. Runtime checks used JDK 25.0.1.
- The individual variant completes within the required one-minute limit.
- The five supplied support classes and `src/lab.html` were left unchanged.

## Before submission

- [x] Individual data files and generated HTML report are included.
- [x] Five algorithm missions have separate commits.
- [x] Example checks and additional checks pass.
- [x] Output, explanations, AI use, timings and complexities are documented.
- [ ] Save and commit `output/outbreak.png` from the generated page.
- [ ] Review and personally revise AI-generated code and explanations.
- [ ] Submit the public repository link in Moodle before the deadline.
