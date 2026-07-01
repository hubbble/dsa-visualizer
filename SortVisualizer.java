import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 資料結構與排序 即時可視化（Java Swing,零外部套件)。
 * 模式:排序(9 種,長條圖) / BST / AVL / 最大堆 / 最小堆(節點圖)。
 * 設計:每個操作先產生「影格序列」,再用 javax.swing.Timer 播放,可播放/暫停/單步/調速,並即時統計比較與交換次數。
 *
 * 編譯:javac SortVisualizer.java
 * 執行:java SortVisualizer
 */
public class SortVisualizer extends JFrame {

    static final Color BG = new Color(0x0f1320), BAR = new Color(0x2d6cdf), CMP = new Color(0xffb74f),
            SWAP = new Color(0xff5d6c), WRITE = new Color(0x9b6cff), DONE = new Color(0x34c98a), VISIT = new Color(0x2bb7c4),
            MUTED = new Color(0x8b97b3), PANEL = new Color(0x1a2032), LINE = new Color(0x39435f), TEXT = new Color(0xe6ebf5);

    // ======================= 排序影格(長條圖) =======================
    static class Frame {
        final int[] a, cmp, swap, write; final int pivot, rlo, rhi; final boolean[] sorted; final String msg;
        Frame(int[] a, int[] cmp, int[] swap, int[] write, int pivot, int rlo, int rhi, boolean[] sorted, String msg) {
            this.a = a; this.cmp = cmp; this.swap = swap; this.write = write; this.pivot = pivot; this.rlo = rlo; this.rhi = rhi; this.sorted = sorted; this.msg = msg;
        }
    }
    static boolean has(int[] arr, int v) { if (arr == null) return false; for (int x : arr) if (x == v) return true; return false; }
    static boolean[] allTrue(int n) { boolean[] s = new boolean[n]; Arrays.fill(s, true); return s; }
    static int[] one(int i) { return new int[]{i}; }
    static int[] two(int i, int j) { return new int[]{i, j}; }

    static List<Frame> bubble(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length; boolean[] s = new boolean[n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                f.add(new Frame(a.clone(), two(j, j + 1), null, null, -1, -1, -1, s.clone(), "比較 a[" + j + "]=" + a[j] + " 與 a[" + (j + 1) + "]=" + a[j + 1]));
                if (a[j] > a[j + 1]) { f.add(new Frame(a.clone(), null, two(j, j + 1), null, -1, -1, -1, s.clone(), a[j] + " > " + a[j + 1] + " → 交換")); int t = a[j]; a[j] = a[j + 1]; a[j + 1] = t; }
            }
            s[n - 1 - i] = true;
        }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "氣泡排序完成 ✓")); return f;
    }
    static List<Frame> selection(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length; boolean[] s = new boolean[n];
        for (int i = 0; i < n - 1; i++) {
            int m = i;
            for (int j = i + 1; j < n; j++) { f.add(new Frame(a.clone(), two(j, m), null, null, m, -1, -1, s.clone(), "找最小:比較 a[" + j + "]=" + a[j] + " 與目前最小 a[" + m + "]=" + a[m])); if (a[j] < a[m]) m = j; }
            if (m != i) { f.add(new Frame(a.clone(), null, two(i, m), null, -1, -1, -1, s.clone(), "把最小值 " + a[m] + " 換到位置 " + i)); int t = a[i]; a[i] = a[m]; a[m] = t; }
            s[i] = true;
        }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "選擇排序完成 ✓")); return f;
    }
    static List<Frame> insertion(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i], j = i - 1; boolean[] s = new boolean[n]; for (int k = 0; k <= i; k++) s[k] = true;
            f.add(new Frame(a.clone(), null, null, null, i, -1, -1, s.clone(), "取出 a[" + i + "]=" + key + ",往左找插入位置"));
            while (j >= 0 && a[j] > key) {
                f.add(new Frame(a.clone(), two(j, j + 1), null, null, -1, -1, -1, s.clone(), "a[" + j + "]=" + a[j] + " > " + key + " → 後移"));
                a[j + 1] = a[j]; f.add(new Frame(a.clone(), null, null, one(j + 1), -1, -1, -1, s.clone(), "後移 a[" + (j + 1) + "]=" + a[j + 1])); j--;
            }
            a[j + 1] = key; f.add(new Frame(a.clone(), null, null, one(j + 1), -1, -1, -1, s.clone(), "插入 " + key + " 到位置 " + (j + 1)));
        }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "插入排序完成 ✓")); return f;
    }
    static List<Frame> shell(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length;
        for (int gap = n / 2; gap > 0; gap /= 2)
            for (int i = gap; i < n; i++) {
                int tmp = a[i], j = i;
                f.add(new Frame(a.clone(), null, null, null, i, -1, -1, null, "gap=" + gap + ":處理 a[" + i + "]=" + tmp));
                while (j >= gap && a[j - gap] > tmp) { f.add(new Frame(a.clone(), two(j, j - gap), null, null, -1, -1, -1, null, "a[" + (j - gap) + "]=" + a[j - gap] + " > " + tmp + " → 後移 " + gap + " 格")); a[j] = a[j - gap]; f.add(new Frame(a.clone(), null, null, one(j), -1, -1, -1, null, "後移 a[" + j + "]=" + a[j])); j -= gap; }
                a[j] = tmp; f.add(new Frame(a.clone(), null, null, one(j), -1, -1, -1, null, "插入 " + tmp + " 到位置 " + j));
            }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "希爾排序完成 ✓")); return f;
    }
    static void msort(List<Frame> f, int[] a, int lo, int hi) {
        if (hi - lo < 1) return; int mid = (lo + hi) / 2; msort(f, a, lo, mid); msort(f, a, mid + 1, hi);
        int[] tmp = new int[hi - lo + 1]; int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) { f.add(new Frame(a.clone(), two(i, j), null, null, -1, lo, hi, null, "合併 [" + lo + ".." + hi + "]:比較 " + a[i] + " 與 " + a[j])); if (a[i] <= a[j]) tmp[k++] = a[i++]; else tmp[k++] = a[j++]; }
        while (i <= mid) tmp[k++] = a[i++]; while (j <= hi) tmp[k++] = a[j++];
        for (int t = 0; t < tmp.length; t++) { a[lo + t] = tmp[t]; f.add(new Frame(a.clone(), null, null, one(lo + t), -1, lo, hi, null, "寫回 a[" + (lo + t) + "]=" + tmp[t])); }
    }
    static List<Frame> merge(int[] in) { List<Frame> f = new ArrayList<>(); int[] a = in.clone(); msort(f, a, 0, a.length - 1); f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(a.length), "合併排序完成 ✓")); return f; }
    static void qs(List<Frame> f, int[] a, int lo, int hi, boolean[] s) {
        if (lo > hi) return; if (lo == hi) { s[lo] = true; return; }
        int pivot = a[hi], i = lo;
        f.add(new Frame(a.clone(), null, null, null, hi, lo, hi, s.clone(), "選 pivot = a[" + hi + "] = " + pivot));
        for (int j = lo; j < hi; j++) {
            f.add(new Frame(a.clone(), two(j, hi), null, null, hi, lo, hi, s.clone(), "比較 a[" + j + "]=" + a[j] + " 與 pivot " + pivot));
            if (a[j] < pivot) { if (i != j) { f.add(new Frame(a.clone(), null, two(i, j), null, hi, lo, hi, s.clone(), a[j] + " < pivot → 交換到左區")); int t = a[i]; a[i] = a[j]; a[j] = t; } i++; }
        }
        if (i != hi) { f.add(new Frame(a.clone(), null, two(i, hi), null, -1, lo, hi, s.clone(), "把 pivot 換到正確位置 " + i)); int t = a[i]; a[i] = a[hi]; a[hi] = t; }
        s[i] = true; f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, s.clone(), "pivot " + a[i] + " 定位於 " + i));
        qs(f, a, lo, i - 1, s); qs(f, a, i + 1, hi, s);
    }
    static List<Frame> quick(int[] in) { List<Frame> f = new ArrayList<>(); int[] a = in.clone(); qs(f, a, 0, a.length - 1, new boolean[a.length]); f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(a.length), "快速排序完成 ✓")); return f; }
    static void siftDownS(List<Frame> f, int[] a, int i, int sz, boolean[] s) {
        while (true) {
            int l = 2 * i + 1, r = 2 * i + 2, t = i;
            if (l < sz) { f.add(new Frame(a.clone(), two(i, l), null, null, -1, -1, -1, s.clone(), "下沉:比較 a[" + i + "] 與左子 a[" + l + "]")); if (a[l] > a[t]) t = l; }
            if (r < sz) { f.add(new Frame(a.clone(), two(t, r), null, null, -1, -1, -1, s.clone(), "比較與右子 a[" + r + "]")); if (a[r] > a[t]) t = r; }
            if (t == i) break;
            f.add(new Frame(a.clone(), null, two(i, t), null, -1, -1, -1, s.clone(), "交換 a[" + i + "] 與 a[" + t + "]")); int tmp = a[i]; a[i] = a[t]; a[t] = tmp; i = t;
        }
    }
    static List<Frame> heapSort(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length; boolean[] s = new boolean[n];
        for (int i = n / 2 - 1; i >= 0; i--) siftDownS(f, a, i, n, s);
        for (int end = n - 1; end > 0; end--) { f.add(new Frame(a.clone(), null, two(0, end), null, -1, -1, -1, s.clone(), "把最大值換到尾端位置 " + end)); int t = a[0]; a[0] = a[end]; a[end] = t; s[end] = true; siftDownS(f, a, 0, end, s); }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "堆積排序完成 ✓")); return f;
    }
    static List<Frame> counting(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length; int max = 0; for (int v : a) max = Math.max(max, v); int[] cnt = new int[max + 1];
        for (int i = 0; i < n; i++) { cnt[a[i]]++; f.add(new Frame(a.clone(), one(i), null, null, -1, -1, -1, null, "統計:count[" + a[i] + "] 加 1")); }
        int[] out = a.clone(); int idx = 0; boolean[] s = new boolean[n];
        for (int v = 0; v <= max; v++) while (cnt[v]-- > 0) { out[idx] = v; s[idx] = true; f.add(new Frame(out.clone(), null, null, one(idx), -1, -1, -1, s.clone(), "依大小寫回:a[" + idx + "]=" + v)); idx++; }
        f.add(new Frame(out.clone(), null, null, null, -1, -1, -1, allTrue(n), "計數排序完成 ✓")); return f;
    }
    static List<Frame> radix(int[] in) {
        List<Frame> f = new ArrayList<>(); int[] a = in.clone(); int n = a.length; int max = 0; for (int v : a) max = Math.max(max, v);
        for (int exp = 1; max / exp > 0; exp *= 10) {
            List<List<Integer>> b = new ArrayList<>(); for (int d = 0; d < 10; d++) b.add(new ArrayList<>());
            for (int i = 0; i < n; i++) { int d = (a[i] / exp) % 10; b.get(d).add(a[i]); f.add(new Frame(a.clone(), one(i), null, null, -1, -1, -1, null, "位數(" + exp + "):a[" + i + "]=" + a[i] + " → 桶 " + d)); }
            int idx = 0; for (int d = 0; d < 10; d++) for (int v : b.get(d)) { a[idx] = v; f.add(new Frame(a.clone(), null, null, one(idx), -1, -1, -1, null, "收集桶 " + d + ":a[" + idx + "]=" + v)); idx++; }
        }
        f.add(new Frame(a.clone(), null, null, null, -1, -1, -1, allTrue(n), "基數排序完成 ✓")); return f;
    }
    static final String[] SORT_NAMES = {"氣泡 Bubble", "選擇 Selection", "插入 Insertion", "希爾 Shell", "合併 Merge", "快速 Quick", "堆積 Heap", "計數 Counting", "基數 Radix"};
    static final String[] SORT_BIG = {"O(n²)", "O(n²)", "O(n²)", "~O(n^1.3)", "O(n log n)", "O(n log n) 平均", "O(n log n)", "O(n+k)", "O(d·(n+k))"};
    static List<Frame> sortGen(int i, int[] a) {
        switch (i) { case 0: return bubble(a); case 1: return selection(a); case 2: return insertion(a); case 3: return shell(a); case 4: return merge(a); case 5: return quick(a); case 6: return heapSort(a); case 7: return counting(a); default: return radix(a); }
    }

    // ======================= 樹/堆積影格(節點圖) =======================
    int nodeSeq = 0;
    class Node { int val; Node left, right; int height = 1; int id; Node(int v) { val = v; id = ++nodeSeq; } }
    static class TFrame {
        int[] id, val, depth, inorder, state, bf; int[][] edges; boolean heap, hasCmp, hasSwap; String msg;
    }
    static int height(Node n) { return n == null ? 0 : n.height; }
    static void updateH(Node n) { n.height = 1 + Math.max(height(n.left), height(n.right)); }
    static int bf(Node n) { return n == null ? 0 : height(n.left) - height(n.right); }
    static int log2(int x) { return (int) Math.floor(Math.log(x) / Math.log(2)); }
    Node rotR(Node y) { Node x = y.left, t = x.right; x.right = y; y.left = t; updateH(y); updateH(x); return x; }
    Node rotL(Node x) { Node y = x.right, t = y.left; y.left = x; x.right = t; updateH(x); updateH(y); return y; }

    // 當前結構狀態
    String structType = "sort";      // sort / bst / avl / maxheap / minheap
    Node root = null;
    List<Integer> hp = new ArrayList<>();
    boolean heapMode() { return structType.equals("maxheap") || structType.equals("minheap"); }
    boolean treeMode() { return structType.equals("bst") || structType.equals("avl"); }
    boolean heapViolates(int parent, int child) { return structType.equals("maxheap") ? parent < child : parent > child; }

    static Map<Integer, Integer> stateOf(int id, int s) { Map<Integer, Integer> m = new HashMap<>(); m.put(id, s); return m; }
    static int[] toArr(List<Integer> l) { int[] a = new int[l.size()]; for (int i = 0; i < a.length; i++) a[i] = l.get(i); return a; }

    // 把目前的樹(root)依 state 快照成 TFrame
    TFrame snapTree(Map<Integer, Integer> state, String msg) {
        List<Integer> id = new ArrayList<>(), val = new ArrayList<>(), dep = new ArrayList<>(), ino = new ArrayList<>(), st = new ArrayList<>(), bfl = new ArrayList<>();
        List<int[]> edges = new ArrayList<>(); int[] ord = {0};
        snapRec(root, 0, -1, id, val, dep, ino, st, bfl, edges, ord, state);
        TFrame t = new TFrame(); t.heap = false; t.msg = msg;
        t.id = toArr(id); t.val = toArr(val); t.depth = toArr(dep); t.inorder = toArr(ino); t.state = toArr(st);
        t.bf = structType.equals("avl") ? toArr(bfl) : null; t.edges = edges.toArray(new int[0][]);
        for (int s : t.state) { if (s == 1) t.hasCmp = true; if (s == 3) t.hasSwap = true; }
        return t;
    }
    void snapRec(Node n, int d, int parentId, List<Integer> id, List<Integer> val, List<Integer> dep, List<Integer> ino, List<Integer> st, List<Integer> bfl, List<int[]> edges, int[] ord, Map<Integer, Integer> state) {
        if (n == null) return;
        snapRec(n.left, d + 1, n.id, id, val, dep, ino, st, bfl, edges, ord, state);
        id.add(n.id); val.add(n.val); dep.add(d); ino.add(ord[0]++); st.add(state.getOrDefault(n.id, 0)); bfl.add(bf(n));
        if (parentId != -1) edges.add(new int[]{parentId, n.id});
        snapRec(n.right, d + 1, n.id, id, val, dep, ino, st, bfl, edges, ord, state);
    }
    int[] heapInorder(int n) { int[] ord = new int[n]; int[] c = {0}; heapDfs(0, n, ord, c); return ord; }
    void heapDfs(int i, int n, int[] ord, int[] c) { if (i >= n) return; heapDfs(2 * i + 1, n, ord, c); ord[i] = c[0]++; heapDfs(2 * i + 2, n, ord, c); }
    TFrame snapHeap(int[] hi, int[] sw, boolean[] done, int newIdx, String msg) {
        int n = hp.size(); TFrame t = new TFrame(); t.heap = true; t.msg = msg;
        t.id = new int[n]; t.val = new int[n]; t.depth = new int[n]; t.inorder = new int[n]; t.state = new int[n]; t.bf = null;
        int[] ino = heapInorder(n); List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            t.id[i] = i; t.val[i] = hp.get(i); t.depth[i] = log2(i + 1); t.inorder[i] = ino[i];
            int s = 0; if (has(sw, i)) s = 3; else if (done != null && done[i]) s = 5; else if (has(hi, i)) s = 1; else if (i == newIdx) s = 4;
            t.state[i] = s; if (s == 1) t.hasCmp = true; if (s == 3) t.hasSwap = true;
            if (i > 0) edges.add(new int[]{(i - 1) / 2, i});
        }
        t.edges = edges.toArray(new int[0][]); return t;
    }
    boolean[] doneAt(int i) { boolean[] d = new boolean[hp.size()]; if (i >= 0 && i < d.length) d[i] = true; return d; }

    // ---- BST ----
    List<Object> bstInsert(int v) {
        List<Object> f = new ArrayList<>(); Node nw = new Node(v);
        if (root == null) { root = nw; f.add(snapTree(stateOf(nw.id, 4), "樹為空," + v + " 成為根 ✓")); return f; }
        Node cur = root; Map<Integer, Integer> path = new HashMap<>();
        while (true) {
            Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 1);
            f.add(snapTree(st, "比較 " + v + " 與 " + cur.val + ":" + (v < cur.val ? "較小 → 左" : v > cur.val ? "較大 → 右" : "相等(已存在)")));
            path.put(cur.id, 2);
            if (v == cur.val) { f.add(snapTree(path, v + " 已存在")); return f; }
            if (v < cur.val) { if (cur.left != null) cur = cur.left; else { cur.left = nw; break; } }
            else { if (cur.right != null) cur = cur.right; else { cur.right = nw; break; } }
        }
        Map<Integer, Integer> st = new HashMap<>(path); st.put(nw.id, 4); f.add(snapTree(st, "找到空位,插入 " + v + " ✓")); return f;
    }
    List<Object> bstSearch(int v) {
        List<Object> f = new ArrayList<>(); if (root == null) { f.add(snapTree(new HashMap<>(), "樹為空")); return f; }
        Node cur = root; Map<Integer, Integer> path = new HashMap<>();
        while (cur != null) {
            if (cur.val == v) { Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 5); f.add(snapTree(st, "找到 " + v + " ✓")); return f; }
            Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 1);
            f.add(snapTree(st, "比較 " + v + " 與 " + cur.val + " → 往" + (v < cur.val ? "左" : "右"))); path.put(cur.id, 2); cur = v < cur.val ? cur.left : cur.right;
        }
        f.add(snapTree(path, "找不到 " + v + " ✗")); return f;
    }
    List<Object> bstDelete(int v) {
        List<Object> f = new ArrayList<>(); if (root == null) { f.add(snapTree(new HashMap<>(), "樹為空")); return f; }
        Node cur = root, parent = null; Map<Integer, Integer> path = new HashMap<>();
        while (cur != null && cur.val != v) { Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 1); f.add(snapTree(st, "尋找 " + v + ":與 " + cur.val + ",往" + (v < cur.val ? "左" : "右"))); path.put(cur.id, 2); parent = cur; cur = v < cur.val ? cur.left : cur.right; }
        if (cur == null) { f.add(snapTree(path, "找不到 " + v)); return f; }
        { Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 3); f.add(snapTree(st, "找到 " + v + ",準備刪除")); }
        if (cur.left != null && cur.right != null) {
            f.add(snapTree(stateOf(cur.id, 3), "有兩個子節點:找中序後繼取代"));
            Node sp = cur, s = cur.right;
            while (s.left != null) { Map<Integer, Integer> st = new HashMap<>(); st.put(cur.id, 3); st.put(s.id, 1); f.add(snapTree(st, "往左找最小值,目前 " + s.val)); sp = s; s = s.left; }
            { Map<Integer, Integer> st = new HashMap<>(); st.put(cur.id, 3); st.put(s.id, 5); f.add(snapTree(st, "後繼 " + s.val + ",複製到刪除位置")); }
            cur.val = s.val; if (sp.left == s) sp.left = s.right; else sp.right = s.right;
            f.add(snapTree(stateOf(cur.id, 5), "刪除完成 ✓")); return f;
        }
        Node child = cur.left != null ? cur.left : cur.right;
        if (parent == null) root = child; else if (parent.left == cur) parent.left = child; else parent.right = child;
        f.add(snapTree(child != null ? stateOf(child.id, 4) : new HashMap<>(), child != null ? "以子節點 " + child.val + " 接上 ✓" : "葉節點,直接移除 ✓"));
        return f;
    }
    // ---- AVL ----
    List<Object> avlInsert(int v) {
        List<Object> f = new ArrayList<>(); Node node = new Node(v);
        if (root == null) { root = node; f.add(snapTree(stateOf(node.id, 4), "空樹," + v + " 成為根")); return f; }
        List<Node> stack = new ArrayList<>(); Node cur = root; Map<Integer, Integer> path = new HashMap<>();
        while (true) {
            Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 1); f.add(snapTree(st, "比較 " + v + " 與 " + cur.val + ",往" + (v < cur.val ? "左" : "右")));
            path.put(cur.id, 2); stack.add(cur);
            if (v == cur.val) { f.add(snapTree(path, v + " 已存在")); return f; }
            if (v < cur.val) { if (cur.left != null) cur = cur.left; else { cur.left = node; break; } }
            else { if (cur.right != null) cur = cur.right; else { cur.right = node; break; } }
        }
        { Map<Integer, Integer> st = new HashMap<>(path); st.put(node.id, 4); f.add(snapTree(st, "插入 " + v + ",由下往上更新高度與 BF")); }
        avlRebalance(f, stack); f.add(snapTree(new HashMap<>(), "AVL 插入 " + v + " 完成 ✓")); return f;
    }
    List<Object> avlDelete(int v) {
        List<Object> f = new ArrayList<>(); if (root == null) { f.add(snapTree(new HashMap<>(), "樹為空")); return f; }
        List<Node> stack = new ArrayList<>(); Node cur = root, parent = null; Map<Integer, Integer> path = new HashMap<>();
        while (cur != null && cur.val != v) { Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 1); f.add(snapTree(st, "尋找 " + v + ":與 " + cur.val + ",往" + (v < cur.val ? "左" : "右"))); path.put(cur.id, 2); stack.add(cur); parent = cur; cur = v < cur.val ? cur.left : cur.right; }
        if (cur == null) { f.add(snapTree(path, "找不到 " + v)); return f; }
        { Map<Integer, Integer> st = new HashMap<>(path); st.put(cur.id, 3); f.add(snapTree(st, "找到 " + v + ",準備刪除")); }
        if (cur.left != null && cur.right != null) {
            f.add(snapTree(stateOf(cur.id, 3), "有兩個子節點:用中序後繼取代"));
            stack.add(cur); Node sp = cur, s = cur.right;
            while (s.left != null) { stack.add(s); sp = s; s = s.left; }
            { Map<Integer, Integer> st = new HashMap<>(); st.put(cur.id, 3); st.put(s.id, 5); f.add(snapTree(st, "後繼 " + s.val + ",複製到刪除位置")); }
            cur.val = s.val; if (sp.left == s) sp.left = s.right; else sp.right = s.right;
        } else {
            Node child = cur.left != null ? cur.left : cur.right;
            if (parent == null) root = child; else if (parent.left == cur) parent.left = child; else parent.right = child;
            f.add(snapTree(new HashMap<>(), child != null ? "以子節點接上" : "葉節點,直接移除"));
        }
        avlRebalance(f, stack); f.add(snapTree(new HashMap<>(), "AVL 刪除 " + v + " 完成 ✓")); return f;
    }
    void avlRebalance(List<Object> f, List<Node> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Node nd = stack.get(i); updateH(nd); int b = bf(nd); Node par = i > 0 ? stack.get(i - 1) : null;
            f.add(snapTree(stateOf(nd.id, 1), "回溯:節點 " + nd.val + " 高度=" + nd.height + ",BF=" + b));
            Node ns = nd;
            if (b > 1 && bf(nd.left) >= 0) { f.add(snapTree(stateOf(nd.id, 3), "LL 失衡 → 右旋")); ns = rotR(nd); }
            else if (b > 1 && bf(nd.left) < 0) { f.add(snapTree(stateOf(nd.id, 3), "LR 失衡 → 左子左旋後右旋")); nd.left = rotL(nd.left); ns = rotR(nd); }
            else if (b < -1 && bf(nd.right) <= 0) { f.add(snapTree(stateOf(nd.id, 3), "RR 失衡 → 左旋")); ns = rotL(nd); }
            else if (b < -1 && bf(nd.right) > 0) { f.add(snapTree(stateOf(nd.id, 3), "RL 失衡 → 右子右旋後左旋")); nd.right = rotR(nd.right); ns = rotL(nd); }
            if (ns != nd) { if (par == null) root = ns; else if (par.left == nd) par.left = ns; else par.right = ns; f.add(snapTree(stateOf(ns.id, 5), "旋轉完成,子樹根變為 " + ns.val)); }
        }
    }
    // ---- Heap ----
    List<Object> heapInsert(int v) {
        List<Object> f = new ArrayList<>(); hp.add(v); int i = hp.size() - 1;
        f.add(snapHeap(null, null, null, i, "插入 " + v + " 到末端索引 " + i + ",開始上浮"));
        while (i > 0) { int p = (i - 1) / 2;
            f.add(snapHeap(two(i, p), null, null, i, "比較 " + hp.get(i) + " 與父 " + hp.get(p)));
            if (heapViolates(hp.get(p), hp.get(i))) { f.add(snapHeap(null, two(i, p), null, i, "不符合 → 交換")); Collections.swap(hp, i, p); i = p; }
            else { f.add(snapHeap(null, null, doneAt(i), -1, "定位於索引 " + i + " ✓")); return f; }
        }
        f.add(snapHeap(null, null, doneAt(0), -1, "成為新頂端 ✓")); return f;
    }
    List<Object> heapExtract() {
        List<Object> f = new ArrayList<>(); int n = hp.size();
        if (n == 0) { f.add(snapHeap(null, null, null, -1, "堆積為空")); return f; }
        int top = hp.get(0); f.add(snapHeap(one(0), null, null, -1, "取出頂端 " + top));
        if (n == 1) { hp.remove(0); f.add(snapHeap(null, null, null, -1, "移除完成 ✓")); return f; }
        f.add(snapHeap(null, two(0, n - 1), null, -1, "把末端 " + hp.get(n - 1) + " 移到根"));
        hp.set(0, hp.get(n - 1)); hp.remove(n - 1);
        f.add(snapHeap(one(0), null, null, -1, "開始下沉")); heapSiftDown(f, 0);
        f.add(snapHeap(null, null, null, -1, "取出 " + top + " 完成 ✓")); return f;
    }
    void heapSiftDown(List<Object> f, int i) {
        int n = hp.size();
        while (true) {
            int l = 2 * i + 1, r = 2 * i + 2, t = i;
            int[] cmp = (r < n) ? new int[]{i, l, r} : (l < n ? two(i, l) : one(i));
            f.add(snapHeap(cmp, null, null, -1, "檢查索引 " + i + " 與子節點"));
            if (l < n && heapViolates(hp.get(t), hp.get(l))) t = l;
            if (r < n && heapViolates(hp.get(t), hp.get(r))) t = r;
            if (t == i) { f.add(snapHeap(null, null, doneAt(i), -1, "定位於索引 " + i + " ✓")); break; }
            f.add(snapHeap(null, two(i, t), null, -1, "與較" + (structType.equals("maxheap") ? "大" : "小") + "子交換")); Collections.swap(hp, i, t); i = t;
        }
    }
    List<Object> heapBuild(List<Integer> base) {
        List<Object> f = new ArrayList<>(); hp = new ArrayList<>(base);
        f.add(snapHeap(null, null, null, -1, "Build Heap:從最後一個非葉節點開始下沉"));
        for (int i = hp.size() / 2 - 1; i >= 0; i--) { f.add(snapHeap(one(i), null, null, -1, "處理索引 " + i)); heapSiftDown(f, i); }
        f.add(snapHeap(null, null, allTrue(hp.size()), -1, "Build Heap 完成 ✓")); return f;
    }
    // ---- 走訪 ----
    List<Object> traverse(int kind) {
        List<Object> f = new ArrayList<>(); String label = new String[]{"前序", "中序", "後序", "層序"}[kind];
        if (heapMode()) {
            int n = hp.size(); if (n == 0) { f.add(snapHeap(null, null, null, -1, "空")); return f; }
            List<Integer> order = heapOrder(kind, n); boolean[] done = new boolean[n]; List<Integer> seq = new ArrayList<>();
            for (int idx : order) { seq.add(hp.get(idx)); f.add(snapHeap(one(idx), null, done.clone(), -1, label + ":走訪 " + hp.get(idx) + "  序列 = " + seq)); done[idx] = true; }
            f.add(snapHeap(null, null, allTrue(n), -1, label + " 完成:" + seq + " ✓"));
        } else {
            if (root == null) { f.add(snapTree(new HashMap<>(), "空")); return f; }
            List<Node> order = treeOrder(kind); Map<Integer, Integer> vis = new HashMap<>(); List<Integer> seq = new ArrayList<>();
            for (Node nd : order) { Map<Integer, Integer> st = new HashMap<>(vis); st.put(nd.id, 1); seq.add(nd.val); f.add(snapTree(st, label + ":走訪 " + nd.val + "  序列 = " + seq)); vis.put(nd.id, 5); }
            f.add(snapTree(vis, label + " 完成:" + seq + " ✓"));
        }
        return f;
    }
    List<Node> treeOrder(int kind) {
        List<Node> out = new ArrayList<>();
        if (kind == 3) { Deque<Node> q = new ArrayDeque<>(); if (root != null) q.add(root); while (!q.isEmpty()) { Node n = q.poll(); out.add(n); if (n.left != null) q.add(n.left); if (n.right != null) q.add(n.right); } }
        else recOrder(root, kind, out);
        return out;
    }
    void recOrder(Node n, int kind, List<Node> out) { if (n == null) return; if (kind == 0) out.add(n); recOrder(n.left, kind, out); if (kind == 1) out.add(n); recOrder(n.right, kind, out); if (kind == 2) out.add(n); }
    List<Integer> heapOrder(int kind, int n) { List<Integer> out = new ArrayList<>(); if (kind == 3) { for (int i = 0; i < n; i++) out.add(i); } else recHeapOrder(0, n, kind, out); return out; }
    void recHeapOrder(int i, int n, int kind, List<Integer> out) { if (i >= n) return; if (kind == 0) out.add(i); recHeapOrder(2 * i + 1, n, kind, out); if (kind == 1) out.add(i); recHeapOrder(2 * i + 2, n, kind, out); }

    // ======================= 繪圖面板 =======================
    class DSPanel extends JPanel {
        Object current; DSPanel() { setBackground(BG); }
        void setFrame(Object o) { current = o; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (current instanceof Frame) drawBars(g2, (Frame) current);
            else if (current instanceof TFrame) drawTree(g2, (TFrame) current);
        }
        void drawBars(Graphics2D g2, Frame fr) {
            int n = fr.a.length; if (n == 0) return; int max = 1; for (int v : fr.a) max = Math.max(max, v);
            int top = 28, bottom = 44, side = 18, W = getWidth() - 2 * side, H = getHeight() - top - bottom; double bw = (double) W / n;
            if (fr.rlo >= 0) { g2.setColor(new Color(79, 156, 255, 22)); g2.fillRect((int) (side + fr.rlo * bw), top, (int) ((fr.rhi - fr.rlo + 1) * bw), H); }
            int gap = (int) Math.min(4, bw * 0.18);
            for (int i = 0; i < n; i++) {
                int h = Math.max(3, (int) ((double) fr.a[i] / max * H)), x = (int) (side + i * bw), y = top + H - h;
                Color c = BAR; if (has(fr.swap, i)) c = SWAP; else if (has(fr.write, i)) c = WRITE; else if (has(fr.cmp, i)) c = CMP; else if (i == fr.pivot) c = WRITE; else if (fr.sorted != null && fr.sorted[i]) c = DONE;
                g2.setColor(c); g2.fillRect(x + gap / 2, y, Math.max(1, (int) bw - gap), h);
                if (n <= 40) { g2.setColor(MUTED); g2.setFont(new Font("Dialog", Font.PLAIN, Math.min(11, Math.max(8, (int) (bw * 0.5))))); String sv = String.valueOf(fr.a[i]); FontMetrics fm = g2.getFontMetrics(); g2.drawString(sv, x + (int) (bw / 2) - fm.stringWidth(sv) / 2, top + H + 14); }
            }
        }
        void drawTree(Graphics2D g2, TFrame f) {
            int n = f.id.length;
            if (n == 0) { g2.setColor(MUTED); g2.setFont(new Font("Dialog", Font.PLAIN, 16)); g2.drawString("空 — 請插入或建立", 30, getHeight() / 2); return; }
            int maxIn = 0, maxDp = 0; for (int i = 0; i < n; i++) { maxIn = Math.max(maxIn, f.inorder[i]); maxDp = Math.max(maxDp, f.depth[i]); }
            int LEVEL_H = 84, NODE_DX = 52, MARGIN = 44, TOP = 46, BOTTOM = 44;
            int w = maxIn * NODE_DX, h = maxDp * LEVEL_H, availW = getWidth() - 2 * MARGIN, availH = getHeight() - TOP - BOTTOM;
            double scale = 1; if (w > availW && w > 0) scale = Math.min(scale, (double) availW / w); if (h > availH && h > 0) scale = Math.min(scale, (double) availH / h);
            double dx = NODE_DX * scale, lh = LEVEL_H * scale; int R = (int) Math.max(11, Math.min(20, 20 * scale)); double offX = (getWidth() - w * scale) / 2.0;
            Map<Integer, int[]> pos = new HashMap<>();
            for (int i = 0; i < n; i++) pos.put(f.id[i], new int[]{(int) (offX + f.inorder[i] * dx), (int) (TOP + f.depth[i] * lh)});
            g2.setStroke(new BasicStroke(2)); g2.setColor(LINE);
            for (int[] e : f.edges) { int[] a = pos.get(e[0]), b = pos.get(e[1]); if (a != null && b != null) g2.drawLine(a[0], a[1], b[0], b[1]); }
            for (int i = 0; i < n; i++) {
                int[] p = pos.get(f.id[i]); int s = f.state[i];
                Color c = s == 1 ? CMP : s == 2 ? VISIT : s == 3 ? SWAP : s == 4 ? WRITE : s == 5 ? DONE : BAR;
                g2.setColor(c); g2.fillOval(p[0] - R, p[1] - R, 2 * R, 2 * R);
                g2.setColor(s != 0 ? c.brighter() : new Color(255, 255, 255, 70)); g2.setStroke(new BasicStroke(s != 0 ? 3 : 2)); g2.drawOval(p[0] - R, p[1] - R, 2 * R, 2 * R);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Dialog", Font.BOLD, (int) (R * 0.95))); String sv = String.valueOf(f.val[i]); FontMetrics fm = g2.getFontMetrics(); g2.drawString(sv, p[0] - fm.stringWidth(sv) / 2, p[1] + fm.getAscent() / 2 - 1);
                String lab = f.heap ? ("[" + f.id[i] + "]") : (f.bf != null ? ("bf " + f.bf[i]) : null);
                if (lab != null) { g2.setColor(MUTED); g2.setFont(new Font("Dialog", Font.PLAIN, 10)); FontMetrics fm2 = g2.getFontMetrics(); g2.drawString(lab, p[0] - fm2.stringWidth(lab) / 2, p[1] - R - 4); }
            }
        }
    }

    // ======================= 主視窗 =======================
    final DSPanel panel = new DSPanel();
    final JComboBox<String> modeBox = new JComboBox<>(new String[]{"排序 Sorting", "二元搜尋樹 BST", "AVL 平衡樹", "最大堆 Max-Heap", "最小堆 Min-Heap"});
    final JComboBox<String> algoBox = new JComboBox<>(SORT_NAMES);
    final JTextField sortInput = new JTextField(20);
    final JTextField valField = new JTextField(6);
    final JSlider speed = new JSlider(1, 100, 60);
    final JButton playBtn = new JButton("▶ 播放");
    final JLabel statusLabel = new JLabel("就緒"), counterLabel = new JLabel(" "), bigLabel = new JLabel(" ");
    final CardLayout cards = new CardLayout(); final JPanel cardPanel = new JPanel(cards);
    List<Object> frames = new ArrayList<>(); int idx = 0; javax.swing.Timer timer;

    SortVisualizer() {
        super("資料結構與排序 即時可視化 — Java Swing");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1000, 620); setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        timer = new javax.swing.Timer(120, e -> { if (idx < frames.size() - 1) { idx++; refresh(); } else stopPlay(); });

        // 第一列:模式 + 各模式控制(CardLayout)
        JPanel rowA = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)); rowA.setBackground(PANEL);
        rowA.add(tag("模式")); rowA.add(modeBox); rowA.add(cardPanel); cardPanel.setBackground(PANEL);

        JPanel sortCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); sortCard.setBackground(PANEL);
        sortCard.add(tag("演算法")); sortCard.add(algoBox); sortCard.add(tag("數值")); sortCard.add(sortInput);
        JButton shuffle = btn("隨機"); sortCard.add(shuffle);
        JButton runSort = btn("執行排序"); runSort.setBackground(BAR); runSort.setForeground(Color.WHITE); sortCard.add(runSort);

        JPanel structCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); structCard.setBackground(PANEL);
        structCard.add(tag("數值")); structCard.add(valField);
        JButton insBtn = btn("插入"); JButton delBtn = btn("刪除"); JButton extBtn = btn("取出頂端"); JButton srchBtn = btn("搜尋"); JButton buildBtn = btn("建立(批次)");
        JButton preB = btn("前序"), inB = btn("中序"), postB = btn("後序"), lvlB = btn("層序");
        for (JButton b : new JButton[]{insBtn, delBtn, extBtn, srchBtn, buildBtn, preB, inB, postB, lvlB}) structCard.add(b);

        cardPanel.add(sortCard, "sort"); cardPanel.add(structCard, "struct");

        // 第二列:共用播放控制
        JPanel rowB = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6)); rowB.setBackground(PANEL);
        JButton first = btn("⏮"), prev = btn("◀"), next = btn("▶▶"), last = btn("⏭");
        rowB.add(first); rowB.add(prev); rowB.add(playBtn); rowB.add(next); rowB.add(last); rowB.add(tag("速度")); rowB.add(speed);

        JPanel north = new JPanel(); north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS)); north.setBackground(PANEL);
        rowA.setAlignmentX(0); rowB.setAlignmentX(0); north.add(rowA); north.add(rowB);

        JPanel bottom = new JPanel(new BorderLayout(12, 0)); bottom.setBackground(PANEL); bottom.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        statusLabel.setForeground(TEXT); statusLabel.setFont(new Font("Dialog", Font.PLAIN, 14)); counterLabel.setForeground(MUTED); bigLabel.setForeground(MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0)); right.setBackground(PANEL); right.add(counterLabel); right.add(bigLabel);
        bottom.add(statusLabel, BorderLayout.CENTER); bottom.add(right, BorderLayout.EAST);

        add(north, BorderLayout.NORTH); add(panel, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);

        // 事件
        modeBox.addActionListener(e -> changeMode());
        shuffle.addActionListener(e -> showSortBase(randomArr()));
        runSort.addActionListener(e -> runSort());
        playBtn.addActionListener(e -> { if (timer.isRunning()) stopPlay(); else startPlay(); });
        next.addActionListener(e -> { stopPlay(); if (idx < frames.size() - 1) { idx++; refresh(); } });
        prev.addActionListener(e -> { stopPlay(); if (idx > 0) { idx--; refresh(); } });
        first.addActionListener(e -> { stopPlay(); idx = 0; refresh(); });
        last.addActionListener(e -> { stopPlay(); idx = Math.max(0, frames.size() - 1); refresh(); });
        speed.addChangeListener(e -> timer.setDelay(Math.max(5, 420 - speed.getValue() * 4)));
        algoBox.addActionListener(e -> { if (structType.equals("sort")) bigLabel.setText("複雜度 " + SORT_BIG[algoBox.getSelectedIndex()]); });

        insBtn.addActionListener(e -> structOp("insert"));
        delBtn.addActionListener(e -> structOp("delete"));
        extBtn.addActionListener(e -> structOp("extract"));
        srchBtn.addActionListener(e -> structOp("search"));
        buildBtn.addActionListener(e -> structOp("build"));
        preB.addActionListener(e -> trav(0)); inB.addActionListener(e -> trav(1)); postB.addActionListener(e -> trav(2)); lvlB.addActionListener(e -> trav(3));

        sortInput.setText("5, 3, 8, 1, 9, 2, 7, 4, 6, 0, 12, 11, 15, 10"); bigLabel.setText("複雜度 " + SORT_BIG[0]);
        showSortBase(parseSort());
    }

    JLabel tag(String s) { JLabel l = new JLabel(s); l.setForeground(MUTED); return l; }
    JButton btn(String s) { JButton b = new JButton(s); b.setFocusPainted(false); return b; }

    void changeMode() {
        stopPlay();
        switch (modeBox.getSelectedIndex()) { case 0: structType = "sort"; break; case 1: structType = "bst"; break; case 2: structType = "avl"; break; case 3: structType = "maxheap"; break; default: structType = "minheap"; }
        cards.show(cardPanel, structType.equals("sort") ? "sort" : "struct");
        root = null; hp = new ArrayList<>(); frames = new ArrayList<>();
        if (structType.equals("sort")) { showSortBase(parseSort()); bigLabel.setText("複雜度 " + SORT_BIG[algoBox.getSelectedIndex()]); }
        else { showStructBase(); bigLabel.setText("複雜度 " + (structType.equals("bst") ? "O(h)" : "O(log n)")); }
    }

    int[] randomArr() { int n = 14 + (int) (Math.random() * 26); int[] a = new int[n]; for (int i = 0; i < n; i++) a[i] = 5 + (int) (Math.random() * 94); sortInput.setText(join(a)); return a; }
    String join(int[] a) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(", "); sb.append(a[i]); } return sb.toString(); }
    int[] parseSort() { return parseList(sortInput.getText()); }
    int[] parseList(String t) { t = t.trim(); if (t.isEmpty()) return new int[0]; String[] p = t.split("[,\\s]+"); List<Integer> l = new ArrayList<>(); for (String x : p) { try { l.add(Integer.parseInt(x)); } catch (Exception ex) { } } return toArr(l); }
    Integer parseVal() { try { return Integer.parseInt(valField.getText().trim()); } catch (Exception e) { JOptionPane.showMessageDialog(this, "請輸入一個整數。"); return null; } }

    void showSortBase(int[] a) { stopPlay(); frames = new ArrayList<>(); frames.add(new Frame(a.clone(), null, null, null, -1, -1, -1, null, "就緒:選排序按「執行排序」或 ▶")); idx = 0; refresh(); }
    void showStructBase() { stopPlay(); frames = new ArrayList<>(); frames.add(heapMode() ? snapHeap(null, null, null, -1, "就緒:輸入數值後插入 / 建立") : snapTree(new HashMap<>(), "就緒:輸入數值後插入 / 建立")); idx = 0; refresh(); }

    void runSort() {
        int[] base = parseSort(); if (base.length == 0) base = randomArr();
        int sel = algoBox.getSelectedIndex();
        if (sel >= 7) for (int v : base) if (v < 0) { JOptionPane.showMessageDialog(this, "計數 / 基數排序需要非負整數。"); return; }
        frames = new ArrayList<>(sortGen(sel, base)); idx = 0; refresh(); startPlay();
    }
    void structOp(String op) {
        List<Object> fr = null; String big = structType.equals("bst") ? "O(h)" : "O(log n)";
        if (op.equals("build")) {
            int[] vals = parseList(valField.getText()); if (vals.length == 0) { vals = randomArr(); } // randomArr 寫到 sortInput;改填 valField
            if (heapMode()) { List<Integer> l = new ArrayList<>(); for (int v : vals) l.add(v); fr = heapBuild(l); }
            else { root = null; fr = new ArrayList<>(); for (int v : vals) fr.addAll(treeMode() && structType.equals("avl") ? avlInsert(v) : bstInsert(v)); }
            big = "O(n)";
        } else if (op.equals("extract")) { if (!heapMode()) { JOptionPane.showMessageDialog(this, "只有堆積能取出頂端。"); return; } fr = heapExtract(); }
        else {
            Integer v = parseVal(); if (v == null) return;
            if (heapMode()) { if (op.equals("insert")) fr = heapInsert(v); else { JOptionPane.showMessageDialog(this, "堆積請用「取出頂端」或「建立」。"); return; } }
            else {
                if (op.equals("insert")) fr = structType.equals("avl") ? avlInsert(v) : bstInsert(v);
                else if (op.equals("delete")) fr = structType.equals("avl") ? avlDelete(v) : bstDelete(v);
                else if (op.equals("search")) fr = bstSearch(v);
            }
        }
        if (fr == null || fr.isEmpty()) return; bigLabel.setText("複雜度 " + big); frames = fr; idx = 0; refresh(); startPlay();
    }
    void trav(int kind) {
        if (structType.equals("sort")) return;
        List<Object> fr = traverse(kind); if (fr.isEmpty()) return; bigLabel.setText("複雜度 O(n)"); frames = fr; idx = 0; refresh(); startPlay();
    }

    void startPlay() { if (idx >= frames.size() - 1) idx = 0; timer.start(); playBtn.setText("❚❚ 暫停"); }
    void stopPlay() { timer.stop(); playBtn.setText("▶ 播放"); }
    boolean isCmp(Object o) { if (o instanceof Frame) { Frame f = (Frame) o; return f.cmp != null && f.swap == null && f.write == null; } TFrame t = (TFrame) o; return t.hasCmp && !t.hasSwap; }
    boolean isMove(Object o) { if (o instanceof Frame) { Frame f = (Frame) o; return f.swap != null || f.write != null; } return ((TFrame) o).hasSwap; }
    String msgOf(Object o) { return o instanceof Frame ? ((Frame) o).msg : ((TFrame) o).msg; }
    void refresh() {
        if (frames.isEmpty()) return; Object f = frames.get(idx); panel.setFrame(f);
        int c = 0, m = 0; for (int k = 0; k <= idx && k < frames.size(); k++) { Object x = frames.get(k); if (isMove(x)) m++; else if (isCmp(x)) c++; }
        statusLabel.setText(msgOf(f)); counterLabel.setText("比較 " + c + "    交換/移動 " + m + "    步驟 " + (idx + 1) + "/" + frames.size());
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new SortVisualizer().setVisible(true)); }
}
