package de.tubs.cs.ias.asm_test.taintaware.array;

public class ArrayHelper {
    public static int[] concat(int[]... arrays) {
        int length = 0;
        for (int[] array : arrays) {
            length += array.length;
        }

        int[] newArray = new int[length];
        int offset = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, newArray, offset, array.length);
            offset += array.length;
        }
        return newArray;
    }
}
