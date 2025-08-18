package chesslib.trash;

import java.util.*;

import java.util.Arrays;
// Elapsed time: 32205 ms << 9
// Elapsed time: 32099 ms << 10
// without init  Elapsed time: 300 ms << 15

 public class RepetitionTableLinkList {
    private static final int SIZE = 1 << 10;
    /*
    table size 1 << 8:Elapsed time without init: 20196.14233201653 ms
    9:Elapsed time without init: 19466.11753500047 ms
    10: Elapsed time without init: 19624.016058004407 ms
    11:Elapsed time without init: 19499.22395801591 ms
    12: Elapsed time without init: 20747.178898019658 ms
     */
    private static final int MASK = SIZE - 1;

    static class Node {
        long key;
        int value;
        Node next;

        Node(long key, int value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    final Node[] table = new Node[SIZE];
    private int size = 0;

    private int index(long key) {
        return (int) (key ^ (key >>> 32)) & MASK;
    }

    public void increment(long key) {
        int idx = index(key);
        Node node = table[idx];

//        int collistion = 0;
        for (Node n = node; n != null; n = n.next) {
            if (n.key == key) {
                n.value++;
//                System.out.println("collision: "+collistion);
                return;
            }
//            collistion++;
        }
//        System.out.println("collision: "+collistion);


        // Not found, add new node to head
        table[idx] = new Node(key, 1, node);
        size++;
    }

    public void decrement(long key) {
        int idx = index(key);
        Node prev = null;
        Node curr = table[idx];

        while (curr != null) {
            if (curr.key == key) {
                curr.value--;
                if (curr.value <= 0) {
                    // Remove node
                    if (prev == null) { // remove from the table itself
                        table[idx] = curr.next;
                    } else {
                        prev.next = curr.next;
                    }
                    size--;
                }
                return;
            }
            prev = curr;
            curr = curr.next;
        }
    }

    public int getCount(long key) {
        int idx = index(key);
        for (Node n = table[idx]; n != null; n = n.next) {
            if (n.key == key) {
                return Math.max(0, n.value);
            }
        }
        return 0;
    }

    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    public int size() {
        return size;
    }
}

 class RepetitionTableTemp {
    private static final int SIZE = 2048;
    private static final int MASK = SIZE - 1;
    private static final int DELETED = -1;
    private int size = 0;

    private final long[] keys = new long[SIZE];
    final int[] values = new int[SIZE];

    /**
     * Increments the count for the given key.
     * If the key already exists, its count is increased.
     * Otherwise, a new entry is inserted.
     */
    public void increment(long key) {
        int index = index(key);
        while (keys[index] != 0 && keys[index] != key) {
            index = (index + 1) & MASK;
        }
        keys[index] = key;
        values[index]++;
    }

    /**
     * Decrements the count for the given key using tombstone deletion.
     * If the count reaches 0, it is marked as deleted.
     */
    public void decrementWithTombstone(long key) {
        int index = index(key);
        while (true) {
            long k = keys[index];
            if (k == 0) return; // Key not found

            if (k == key) {
                values[index]--;
                if (values[index] <= 0) {
                    keys[index] = 0;
                    values[index] = DELETED;
                    size--;
                }
                return;
            }
            index = (index + 1) & MASK;
        }
    }

    /**
     * Decrements the count for the given key using backshift deletion.
     * If the count reaches 0, the entry is removed and shifted.
     */
    public void decrement(long key) {
        int index = index(key);

        while (true) {
            long k = keys[index];
            if (k == 0) return; // Key not found

            if (k == key) {
                values[index]--;
                if (values[index] <= 0) {
                    keys[index] = 0;
                    values[index] = 0;
                    size--;

                    int availableIndex = index;
                    while (true) {
                        index = (index + 1) & MASK;
                        long nextKey = keys[index];
                        if (nextKey == 0) return;

                        int idealIndex = index(nextKey);
                        boolean shouldShift = (idealIndex - availableIndex + SIZE) % SIZE
                                < (index - availableIndex + SIZE) % SIZE;
//                        boolean shouldShift = (idealIndex <= availableIndex && availableIndex < index) ||
//                                (idealIndex > index && (availableIndex < index || availableIndex >= idealIndex)) ||
//                                (index < idealIndex && idealIndex <= availableIndex);

                        if (!shouldShift) return;

                        // Shift entry backward
                        keys[availableIndex] = keys[index];
                        values[availableIndex] = values[index];
                        keys[index] = 0;
                        values[index] = 0;
                        availableIndex = index;
                    }
                }
                return;
            }
            index = (index + 1) & MASK;
        }
    }

    /**
     * Returns the count for the given key.
     */
    public int getCount(long key) {
        int index = index(key);
        while (true) {
            long k = keys[index];
            if (k == 0) return 0;
            if (k == key) return values[index];
            index = (index + 1) & MASK;
        }
    }

    /**
     * Clears the table completely.
     */
    public void clear() {
        Arrays.fill(keys, 0);
        Arrays.fill(values, 0);
        size = 0;
    }

    /**
     * Computes the initial index for a key using a simple hash.
     */
    private int index(long key) {
        return (int) (key ^ (key >>> 32)) & MASK;
    }
}


//
//class RepetitionTableTest {
//
//    private static final int NUM_KEYS = 2000; // For open addressing, should not exceed table size
//    private static final int REPEAT_FACTOR = 10;
//
//    public static void main(String[] args) {
//        System.out.println("=== Testing occupancy for different table strategies ===\n");
//
//        testOccupancy("OpenAddressing with Tombstone", new RepetitionTableOpenAddressing_Tombstone());
//        testOccupancy("OpenAddressing with Backshift", new RepetitionTableOpenAddressing_Backshift());
//        testOccupancy("Linked List Chaining", new RepetitionTableChaining());
//    }
//
//    private static void testOccupancy(String getName, RepetitionTableBase table) {
//        Random rand = new Random(42);
//        Set<Long> inserted = new HashSet<>();
//
//        while (inserted.size() < NUM_KEYS) {
//            long key = rand.nextLong();
//            if (inserted.add(key)) {
//                table.increment(key);
//
//                // Repeat the same key some times
//                for (int r = 0; r < rand.nextInt(REPEAT_FACTOR); r++) {
//                    table.increment(key);
//                }
//
//                // And randomly decrement some keys
//                if (rand.nextDouble() < 0.5) {
//                    table.decrement(key);
//                }
//            }
//        }
//
//        int totalUsed = table.getTotalSlotsUsed();
//        int active = table.getUniqueKeyCount();
//
//        System.out.printf("[%s]\n", getName);
//        System.out.printf("  Inserted Unique Keys : %d\n", inserted.size());
//        System.out.printf("  Active Keys          : %d\n", active);
//        System.out.printf("  Total Slots Occupied : %d\n", totalUsed);
//        System.out.printf("  Load Factor          : %.2f\n\n", totalUsed / (float) table.capacity());
//    }
//
//    interface RepetitionTableBase {
//        void increment(long key);
//        void decrement(long key);
//        int getCount(long key);
//        int getTotalSlotsUsed();
//        int getUniqueKeyCount();
//        int capacity();
//    }
//
//    static class RepetitionTableOpenAddressing_Tombstone implements RepetitionTableBase {
//        RepetitionTableTemp table = new RepetitionTableTemp();
//
//        public void increment(long key) { table.increment(key); }
//        public void decrement(long key) { table.decrementWithTombstone(key); }
//        public int getCount(long key) { return table.getCount(key); }
//        public int getTotalSlotsUsed() {
//            int used = 0;
//            for (int val : table.values) {
//                if (val != 0) used++;
//            }
//            return used;
//        }
//        public int getUniqueKeyCount() {
//            int count = 0;
//            for (int val : table.values) {
//                if (val > 0) count++;
//            }
//            return count;
//        }
//        public int capacity() { return 2048; }
//    }
//
//    static class RepetitionTableOpenAddressing_Backshift implements RepetitionTableBase {
//        RepetitionTableTemp table = new RepetitionTableTemp();
//
//        public void increment(long key) { table.increment(key); }
//        public void decrement(long key) { table.decrement(key); }
//        public int getCount(long key) { return table.getCount(key); }
//        public int getTotalSlotsUsed() {
//            int used = 0;
//            for (int val : table.values) {
//                if (val != 0) used++;
//            }
//            return used;
//        }
//        public int getUniqueKeyCount() {
//            int count = 0;
//            for (int val : table.values) {
//                if (val > 0) count++;
//            }
//            return count;
//        }
//        public int capacity() { return 2048; }
//    }
//
//    static class RepetitionTableChaining implements RepetitionTableBase {
//        RepetitionTable1 table = new RepetitionTable1();
//
//        public void increment(long key) { table.increment(key); }
//        public void decrement(long key) { table.decrement(key); }
//        public int getCount(long key) { return table.getCount(key); }
//        public int getTotalSlotsUsed() {
//            int used = 0;
//            for (Object obj : table.table) {
//                RepetitionTable1.Node node = (RepetitionTable1.Node) obj;
//                while (node != null) {
//                    used++;
//                    node = node.next;
//                }
//            }
//            return used;
//        }
//        public int getUniqueKeyCount() { return table.size(); }
//        public int capacity() { return 4096; }
//    }
//}