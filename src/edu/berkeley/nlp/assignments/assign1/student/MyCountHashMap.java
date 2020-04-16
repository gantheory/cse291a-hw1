package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;

public class MyCountHashMap {
  private long[] keys;
  private int[] values;
  private int size = 0;
  private final int MAX_LOAD_FACTOR = 100; // will be divided by 100

  public MyCountHashMap() {
    this(10);
  }

  public MyCountHashMap(int initialCapacity) {
    keys = new long[initialCapacity];
    values = new int[initialCapacity];
    Arrays.fill(keys, -1);
  }

  public int size() { return size; }

  public int get(long key) {
    int i = findSlot(key, keys);
    if (keys[i] == key)
      return values[i];
    return 0;
  }

  public void put(long key, int value) {
    int i = findSlot(key, keys);
    if (keys[i] == key) {
      values[i] = value;
      assert(values[i] >= 0);
      return;
    }
    if ((long) size * 100 >= ((long) keys.length * MAX_LOAD_FACTOR)) {
      System.out.println("MyCountHashMap is growing to " + keys.length * 2);
      long[] newKeys = new long[keys.length * 2];
      int[] newValues = new int[keys.length * 2];
      Arrays.fill(newKeys, -1);
      size = 0;
      for (int j = 0; j < keys.length; ++j) {
        if (keys[j] == -1) continue;
        int newI = findSlot(keys[j], newKeys);
        newKeys[newI] = keys[j];
        newValues[newI] = values[j];
        ++size;
      }
      keys = newKeys;
      values = newValues;
      i = findSlot(key, keys);
    }
    keys[i] = key;
    values[i] = value;
    ++size;
  }

  public int get(int[] key) {
    assert(0 < key.length && key.length <= 3);
    if (key.length == 1) {
      return get(hashCode(key[0]));
    } else if (key.length == 2) {
      return get(hashCode(key[0], key[1]));
    } else {
      return get(hashCode(key[0], key[1], key[2]));
    }
  }

  public long hashCode(int c) { return hashCode(0, 0, c); }

  public long hashCode(int b, int c) { return hashCode(0, b, c); }

  public long hashCode(int a, int b, int c) {
    assert(0 <= a && a <= (1 << 20));
    assert(0 <= b && b <= (1 << 20));
    assert(0 <= c && c <= (1 << 20));
    long ans = 0;
    ans = (ans << 20) + a;
    ans = (ans << 20) + b;
    ans = (ans << 20) + c;
    ans = (ans ^ (ans >>> 32)) * 3875239;
    return ans;
  }

  private int findSlot(long key, long[] keyArray) {
    int i = (int)(key % keyArray.length);
    if (i < 0) i += keyArray.length;
    while (keyArray[i] != -1 && keyArray[i] != key) i = (i + 1) % keyArray.length;
    return i;
  }
}
