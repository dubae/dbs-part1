package org.example.object;

import java.util.Arrays;

public class ByteBlock {

    private byte[] bytes;
    private int blockOffset;

    public ByteBlock(byte[] bytes, int blockOffset) {
        this.bytes = bytes;
        this.blockOffset = blockOffset;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getBlockOffset() {
        return blockOffset;
    }



    public void setBytes(byte[] bytes) {
        this.bytes = bytes;


    }




}
