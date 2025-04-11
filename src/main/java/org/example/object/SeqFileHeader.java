package org.example.object;

public class SeqFileHeader {
/**
 * 메타데이터 추가
 * 첫 포인터(int)
 * 속성개수(int)
 * 각 속성별 char크기(int[])
 * 속성명(String[]) 순으로 적용
 */
    int recordPointer;
    int attrNum;
    int recordNum;
    int lastBlockOffset;
    int[] charSize;
    String[] attrs;

    public SeqFileHeader(int recordPointer, int attrNum, int recordNum, int lastBlockOffset, int[] charSize, String[] attrs) {
        this.recordPointer = recordPointer;
        this.attrNum = attrNum;
        this.charSize = charSize;
        this.attrs = attrs;
        this.recordNum = recordNum;
        this.lastBlockOffset = lastBlockOffset;


    }

    public int getRecordPointer() {
        return recordPointer;
    }
    public int getAttrNum() {
        return attrNum;
    }
    public int getRecordNum() {
        return recordNum;
    }
    public int[] getCharSize() {
        return charSize;
    }

    public int getLastBlockOffset() {
        return lastBlockOffset;
    }
    public String[] getAttrs() {
        return attrs;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    public void setLastBlockOffset(int lastBlockOffset) {
        this.lastBlockOffset = lastBlockOffset;
    }
}
