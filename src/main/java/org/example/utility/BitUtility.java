package org.example.utility;

import java.util.BitSet;

public class BitUtility {

    public static BitSet getBitSetfromByte(byte b) {
        BitSet bitSet = new BitSet(8);
        for (int i = 0; i < 8; i++) {
            // 상위 비트부터 BitSet에 저장 (BitSet은 LSB 우선이니까 순서 보정)
            if ((b & (1 << (7 - i))) != 0) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

}
