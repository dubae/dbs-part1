package org.example.system;

import org.example.Config;
import org.example.object.SeqFileHeader;

import java.io.IOException;
import java.io.RandomAccessFile;

//헤더를 다루는 클래스만 따로 분리.
public class SeqFileHeaderSystem {

    public static void writeHeader(RandomAccessFile seqFile, SeqFileHeader header) throws IOException {
        seqFile.seek(0);

        seqFile.writeInt(Config.FILE_HEADER_SIZE); //맨 상단에는 헤더 블록의 크기를 오프셋으로 지정하여 다음 블록의 위치를 알게 함.

        /**
         * 메타데이터 추가
         * 첫 포인터(int)
         * 속성개수(int)
         * 포함된 레코드의 개수(int)
         * 마지막 블럭의 offset
         * 각 속성별 char크기(int[])
         * 속성명(String[]) 순으로 적용
         */

        seqFile.writeInt(header.getAttrNum()); //속성개수
        seqFile.writeInt(header.getRecordNum()); //처음엔 0개로 시작함. 레코드 개수
        seqFile.writeInt(header.getLastBlockOffset()); //가장 마지막에 있는 블럭의 시작주소(blockOffset)


        printFileHeader(seqFile);

    }


    //디버깅용, 헤더 읽어서 출력하는 형식.
    public static void printFileHeader(RandomAccessFile seqFile) throws IOException {
        seqFile.seek(0);
        System.out.println("첫 포인터 : " + seqFile.readInt());
        int attrNum=seqFile.readInt();
        System.out.println("속성 개수 : " + attrNum);
        int recordNum=seqFile.readInt();
        System.out.println("레코드 개수 : " + recordNum);
        int lastBlockOffset=seqFile.readInt();
        System.out.println( "마지막 블럭 오프셋 : " + lastBlockOffset);

        for(int i=0;i<attrNum;i++){
            System.out.println("속성char 크기 : " + seqFile.readInt());
        }

        // ======여기까지는 완벽 구현 됨=========
        for(int i=0;i<attrNum;i++){
            System.out.println("attr name: " + seqFile.readUTF());
        }

        System.out.println("포인터 위치 : " + seqFile.getFilePointer());


    }

    //헤더를 반환
    public static SeqFileHeader getSeqFileHeader(RandomAccessFile seqFile) throws IOException {
        seqFile.seek(0);
        int recordPointer = seqFile.readInt();
        int attrNum = seqFile.readInt();
        int recordNum = seqFile.readInt();
        int lastBlockOffset = seqFile.readInt();
        int[] charSize = new int[attrNum];
        for(int i=0;i<attrNum;i++){
            charSize[i] = seqFile.readInt();
        }
        String[] attrs = new String[attrNum];
        for(int i=0;i<attrNum;i++){

            attrs[i] = seqFile.readUTF();
        }
        return new SeqFileHeader(recordPointer, attrNum, recordNum,lastBlockOffset,charSize, attrs);
    }

}
