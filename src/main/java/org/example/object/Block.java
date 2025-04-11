package org.example.object;

import org.example.Config;

import java.util.ArrayList;
import java.util.List;

public class Block {

    List<Record> records=new ArrayList<Record>();

    int size = 0;

    int remainCapacity = Config.BLOCK_SIZE;

    int blockOffset; //해당 블럭의 시작주소

    public Block(List<Record> records) {
        this.records = records;
    }

    public Block() {}

    public void add(Record record) {
        this.records.add(record);
        this.size+=record.getSize();
        remainCapacity -= record.getSize();
    }

    public void setBlockOffset(int blockOffset) {
        this.blockOffset = blockOffset;
    }

    public List<Record> getRecords() {
        return records;
    }

    public int getRemainCapacity() {
        return remainCapacity;
    }

    public int getSize() {
        return size;
    }

    public int getBlockOffset() {
        return blockOffset;
    }

    public byte[] toByteArray() {
        return null;
    }



}
