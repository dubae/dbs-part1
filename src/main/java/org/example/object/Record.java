package org.example.object;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Record {

    BitSet bitSet = new BitSet();

    List<String> attrs=new ArrayList<String>();

    int nextOffset; //다음 레코드의 오프셋(파일 시작:0)

    int size;
    public Record(BitSet bitSet, String[] attrs) {
        size=5; //비트맵 (1) + 포인터 (4)
        this.bitSet = bitSet;
        for(String attr : attrs){
            if(!attr.equals("null")) {
                this.attrs.add(attr);
                size+=attr.length();
            }
        }
    }

    public Record(BitSet bitSet, String[] attrs, int nextOffset) {
        size=5; //비트맵 (1) + 포인터 (4)
        this.bitSet = bitSet;
        for(String attr : attrs){
            if(!attr.equals("null")) {
                this.attrs.add(attr);
                size+=attr.length();
            }
        }
        this.nextOffset = nextOffset;
    }

//    public byte[] toByteArray() {
//        byte[] bytes = new byte[size];
//
//
//        //널비트맵
//        bytes[0] = bitSet.toByteArray()[0];
//
//    }

    public void print(){
        System.out.println("=== Record Info ===");

        // BitSet 출력
        System.out.print("BitSet: ");
        for (int i = 0; i < bitSet.length(); i++) {
            System.out.print(bitSet.get(i) ? "1" : "0");
        }
        System.out.println();

        // attrs 리스트 출력
        System.out.println("Attributes: " + String.join(", ", attrs));

        // nextOffset, size 출력
        System.out.println("Next Offset: " + nextOffset);
        System.out.println("Size: " + size);

    }

    public void printAttr(String attr){

    }


    //offset 설정
    public void setNextOffset(int nextOffset){
        this.nextOffset = nextOffset;
    }


    // ===========GETTER==============

    //크기를 바이트 단위로 반환.
    public int getSize(){
        return size;
    }
    public int getNextOffset(){
        return nextOffset;
    }

    public BitSet getBitSet(){
        return bitSet;
    }

    public List<String> getAttrs(){
        return attrs;
    }


}
