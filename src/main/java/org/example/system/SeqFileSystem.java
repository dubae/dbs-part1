package org.example.system;

import org.example.Config;
import org.example.object.Block;
import org.example.object.ByteBlock;
import org.example.object.Record;
import org.example.object.SeqFileHeader;
import org.example.utility.BitUtility;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.example.system.SeqFileHeaderSystem.*;

//파일의 생성, 관리를 맡음.
public class SeqFileSystem {


    // 두 테이블을 받아 search key로 조인 수행.
    // 테이블의 search key는 고유하므로 따로 인자로 받을 필요 없음.
    // 각 테이블명.
    public static void join(String table1, String table2) throws IOException {

        RandomAccessFile seqFile1=new RandomAccessFile(table1+".dat","rw");
        RandomAccessFile seqFile2=new RandomAccessFile(table2+".dat","rw");

        SeqFileHeader header1=getSeqFileHeader(seqFile1);
        SeqFileHeader header2=getSeqFileHeader(seqFile2);

        int recordPointer1=header1.getRecordPointer(); // 1 테이블을 돌 포인터
        int recordPointer2=header2.getRecordPointer(); // 2 테이블을 돌 포인터

        System.out.println("=== MERGE 조인 결과 ===");
        System.out.println(Arrays.toString(header1.getAttrs()) + "\t" + Arrays.toString(header2.getAttrs()));

        while(recordPointer1!=0 && recordPointer2!=0){ // 끝에 도달하면 순회 멈춰


            Record record2=readRecordByOffset(seqFile2,recordPointer2);
            String key2=record2.getAttrs().getFirst();

            List<Record> sub2=new ArrayList<>();
            sub2.add(record2);
            recordPointer2=record2.getNextOffset();
            boolean done=false;

            // 같은 키 가지는 것 메모리로 올림
            while(!done && recordPointer2!=0){
                Record record2prime=readRecordByOffset(seqFile2,recordPointer2);
                if(record2prime.getAttrs().getFirst().equals(record2.getAttrs().getFirst())){
                    //키가 같으면 부분집합에 추가(같은거끼리 묶음)
                    sub2.add(record2prime);
                    recordPointer2=record2prime.getNextOffset();
                }
                else{
                    done=true;
                }
            }


            Record record1=readRecordByOffset(seqFile1,recordPointer1);
            // 일치하는 곳까지 첫 테이블 포인터 옮김
            while(recordPointer1!=0 && record1.getAttrs().getFirst().compareTo(key2)<0){ //부등호 점검 필요
                recordPointer1=record1.getNextOffset();
                record1=readRecordByOffset(seqFile1,recordPointer1);

            }

            // 각 일치하는 것마다 sub순회하며 출력
            while(recordPointer1!=0 && record1.getAttrs().getFirst().equals(key2)){

                for(Record subRecord2:sub2){
                    //조인 결과 출력
                    for(String attr1:record1.getAttrs()){
                        System.out.print(attr1 + "\t");
                    }
                    for(String attr2:subRecord2.getAttrs()){
                        System.out.print(attr2 + "\t");
                    }
                    System.out.println();
                }
                recordPointer1=record1.getNextOffset();
                record1=readRecordByOffset(seqFile1,recordPointer1);
            }

        }



    }


    public static void searchRecord(String info) throws IOException {

        String[] parsed=info.split(",");
        String seqFileName = parsed[0];
        String targetAttr = parsed[1];
        String min=parsed[2];
        String max=parsed[3];


        RandomAccessFile seqFile=new RandomAccessFile(seqFileName+".dat","rw");

        SeqFileHeader header=getSeqFileHeader(seqFile);

        int i=0;

        String[] attrs=header.getAttrs();

        for(;i<attrs.length;i++){
            if(attrs[i].equals(targetAttr)){
                break;
            }

        }

        int recordOffset=header.getRecordPointer();
        Block block=readBlockByOffset(seqFile, recordOffset);

        System.out.println("=== 레코드 검색 결과 ===");
        System.out.println(Arrays.toString(header.getAttrs()));

        while(!block.getRecords().isEmpty()){
            for(Record record:block.getRecords()){
                if(record.getAttrs().get(i).compareTo(min)>=0 && record.getAttrs().get(i).compareTo(max)<=0){
                    System.out.println(Arrays.toString(new List[]{record.getAttrs()}));

                }
                recordOffset=record.getNextOffset(); //갱신
                if(recordOffset==0) return;
            }

            block=readBlockByOffset(seqFile, recordOffset);
        }

    }


    //기능 3: 필드로 검색
    public static void searchField(String info) throws IOException {


        String[] parsed=info.split(",");
        String seqFileName = parsed[0];
        String targetAttr = parsed[1];


        RandomAccessFile seqFile=new RandomAccessFile(seqFileName+".dat","rw");

        SeqFileHeader header=getSeqFileHeader(seqFile);

        int i=0;

        String[] attrs=header.getAttrs();

        for(;i<attrs.length;i++){
            if(attrs[i].equals(targetAttr)){
                break;
            }

        }

        int recordOffset=header.getRecordPointer();
        Block block=readBlockByOffset(seqFile, recordOffset);

        while(!block.getRecords().isEmpty()){
            for(Record record:block.getRecords()){
               // System.out.println(record.getAttrs().get(0) + "   " + record.getAttrs().get(i));
                recordOffset=record.getNextOffset(); //갱신
                if(recordOffset==0) return;
            }

            block=readBlockByOffset(seqFile, recordOffset);
        }

    }


    // 블럭 단위 순회의 기초가 됨
    public static void printAllRecords(RandomAccessFile seqFile) throws IOException {
        SeqFileHeader header=getSeqFileHeader(seqFile);
        printFileHeader(seqFile);

        int recordOffset=header.getRecordPointer(); //첫 레코드 포인터

        while(recordOffset!=0){

            Record record=readRecordByOffset(seqFile, recordOffset);
            System.out.print("bitset:" + record.getBitSet());
            System.out.print("attr:" + record.getAttrs());
            System.out.println("nextOffset:" + record.getNextOffset());
            recordOffset=record.getNextOffset();

        }





//        Block block=readBlockByOffset(seqFile, recordOffset);

//
//        while(!block.getRecords().isEmpty()){
//            System.out.println("<NEW BLOCK>");
//            for(Record record:block.getRecords()){
//                record.print();
//                recordOffset=record.getNextOffset(); //갱신
//            }
//
//            block=readBlockByOffset(seqFile, recordOffset);
//        }



    }



    //파일에 byteblock을 덮어씀.
    public static void writeByteBlock(RandomAccessFile seqFile,ByteBlock byteBlock) throws IOException {

        byte[] bytes=byteBlock.getBytes();

        seqFile.seek(byteBlock.getBlockOffset()); //블럭 오프셋으로 쓰는 시점 시작
        seqFile.write(bytes); //바이트 덮어쓰기

    }


    //레코드 쓰기 버전 2
    // 블럭을 읽고, 해당하는 레코드를 덮어쓴 다음 블럭 전체를 다시 쓰기.
    public static void writeRecord2(RandomAccessFile seqFile,Record record, int offset) throws IOException {

         int blockOffset = offset;

        while(blockOffset >=Config.BLOCK_SIZE){
            blockOffset -=Config.BLOCK_SIZE;
        }

        ByteBlock block=readByteBlockByOffset(seqFile, offset); //해당하는 블록 가져옴
        byte[] byteBlock=block.getBytes(); // 블록을 바이트로 바꿈
        byte[] byteRecord=record.toBytes(); //레코드를 바이트로 바꿈

        //블럭을 레코드로 덮어씀
        for(int i=0; i<byteRecord.length; i++){
            byteBlock[i+blockOffset]=byteRecord[i];
        }

        //파일에 쓰기
        seqFile.seek(offset-blockOffset);
        seqFile.write(byteBlock);


    }



    //파일에 블럭을 덮어씀.
    // 헤더파일의 갱신이 필요
    public static void writeBlock(RandomAccessFile seqFile, Block block) throws IOException {
        int recordOffset=block.getBlockOffset(); //블럭의 처음에 레코드가 등장



        //각 레코드를 기록
        for(Record record:block.getRecords()){

            writeRecord(seqFile, record, recordOffset);
            recordOffset=record.getNextOffset(); //다음으로 오프셋을 변경
        }

    }


    /// 새로운 블럭
    /// 헤더파일의 갱신이 필요함
    public static void writeNewBlock(RandomAccessFile seqFile, Block block) throws IOException {
        int recordOffset=block.getBlockOffset(); //블럭의 처음에 레코드가 등장



        //각 레코드를 기록
        for(Record record:block.getRecords()){

            writeRecord(seqFile, record, recordOffset);
            recordOffset=record.getNextOffset(); //다음으로 오프셋을 변경
        }

    }

    // 특정 오프셋의 레코드를 가져옴
    // 잘 작동하는 것 확인.
    public static Record readRecordByOffset(RandomAccessFile seqFile, int recordOffset) throws IOException {

        // 해당하는 블록을 통채로 읽어옴
        ByteBlock byteBlock=readByteBlockByOffset(seqFile, recordOffset);

        // 오프셋을 블록 내의 오프셋으로 변경
        while(recordOffset>=Config.BLOCK_SIZE){
            recordOffset-=Config.BLOCK_SIZE;
        }

        // 읽어온 블록에서 레코드만 파싱
        byte[] block=byteBlock.getBytes();
        SeqFileHeader header=getSeqFileHeader(seqFile); //헤더 읽어옴.
        int[] attrSize=header.getAttrSize();//각 속성별 사이즈
        int attrNum=header.getAttrNum();

        BitSet bitset=BitUtility.getBitSetfromByte(block[recordOffset++]); //null bitmap

        //속성값 설정
        String[] attrs=new String[attrNum];
        for(int i=0;i<attrNum;i++){
            /**
             * 이 부분 체크할 것(null)
             */
            // null이 맞는 경우(속성이 없음)
            if(bitset.get(i)) attrs[i]=null;
            else attrs[i]=new String(Arrays.copyOfRange(block,recordOffset,recordOffset+attrSize[i]));
            recordOffset+=attrSize[i];
        }

        //다음 오프셋 계산
        byte[] subBytes= Arrays.copyOfRange(block, recordOffset,recordOffset+4);
        ByteBuffer buffer = ByteBuffer.wrap(subBytes);
        buffer.order(ByteOrder.BIG_ENDIAN); // 또는 LITTLE_ENDIAN
        int nextOffset = buffer.getInt();

        return new Record(bitset,attrs,nextOffset);


    }


    // 특정 오프셋의 블럭을 바이트 형태로 가져옴.
    public static ByteBlock readByteBlockByOffset(RandomAccessFile seqFile, int offset) throws IOException {
        int blockOffset=0;
        byte[] blockBytes = new byte[Config.BLOCK_SIZE];
        SeqFileHeader header=SeqFileHeaderSystem.getSeqFileHeader(seqFile); //속성 계산을 위한 헤더 가져옴

        //해당 오프셋 레코드가 어느 블럭에 위치하는지
        //1.  그 블럭의 시작 주소를 계산
        for(; blockOffset<=offset; blockOffset+=Config.BLOCK_SIZE) {

        }
        blockOffset-=Config.BLOCK_SIZE;



        // 2. 시작 주소와 블럭 크기를 알고 있으니 블럭을 읽음 -> 블럭 객체로 만들어서 메인 메모리에 저장?
        seqFile.seek(blockOffset);
        seqFile.read(blockBytes,0,Config.BLOCK_SIZE);

        return new ByteBlock(blockBytes, blockOffset);
    }

    // 특정 오프셋에 해당하는 블럭 offset을 가져옴.
    // 블럭 단위 I/O의 핵심 요소
    public static Block readBlockByOffset(RandomAccessFile seqFile, int offset) throws IOException {

        int blockOffset=0;
        byte[] blockBytes = new byte[Config.BLOCK_SIZE];
        SeqFileHeader header=SeqFileHeaderSystem.getSeqFileHeader(seqFile); //속성 계산을 위한 헤더 가져옴

        //해당 오프셋 레코드가 어느 블럭에 위치하는지
        //1.  그 블럭의 시작 주소를 계산
        for(; blockOffset<=offset; blockOffset+=Config.BLOCK_SIZE) {

        }
        blockOffset-=Config.BLOCK_SIZE;



        // 2. 시작 주소와 블럭 크기를 알고 있으니 블럭을 읽음 -> 블럭 객체로 만들어서 메인 메모리에 저장?
        seqFile.seek(blockOffset);
        seqFile.read(blockBytes,0,Config.BLOCK_SIZE);


        // 3. byte[]형태의 블럭을 BLOCK 객체로 변경
        /**
         * 항상 offset에서부터 읽기를 시작한다고 가정함.
         * 즉 첫 번째 레코드라고 가정한다. 임의의 포인트가 들어온다고 생각하기 힘듬.
         */
        // i: byte[] 내부의 인덱스.
        int attrsNum=header.getAttrNum();
        int[] charSize=header.getAttrSize();
        Block block=new Block();
        block.setBlockOffset(blockOffset);

        for(int i=offset-blockOffset; i<Config.BLOCK_SIZE; ) {


            //null bitset 읽기.
            //읽고 1칸 증가
            BitSet bitSet= BitUtility.getBitSetfromByte( blockBytes[i++]);

            //속성값 추출하기
            List<String> attrs=new ArrayList<>();
            // j: 몇 번째 속성을 계산하는지를 나타냄
            for(int j=0;j<attrsNum;j++) {
                if(!bitSet.get(j)) { //false -> 속성 값이 있을 때(null X)
                    //사이즈만큼 읽어서 저장함
                    byte[] subBytes= Arrays.copyOfRange(blockBytes, i,i+charSize[j]);
                    String attr=new String(subBytes);
                    /**
                     * 종료조건
                     */
                    if(attr.equals("\0".repeat(charSize[j]))) { //속성 값이 0000 -> 비어있는 부분이므로 종료하고 block리턴
                        return block;
                    }
                    attrs.add(attr);
                    i+=charSize[j];
                }
            }
            String[] attrsArr=attrs.toArray(new String[attrsNum]);

            //다음 레코드 포인터 저장하기
            byte[] subBytes= Arrays.copyOfRange(blockBytes, i,i+4);
            ByteBuffer buffer = ByteBuffer.wrap(subBytes);
            buffer.order(ByteOrder.BIG_ENDIAN); // 또는 LITTLE_ENDIAN
            int nextOffset = buffer.getInt();

            /**
             * 마지막 레코드인지 검사
             */


            i=nextOffset-Config.BLOCK_SIZE;

            Record record=new Record(bitSet,attrsArr,nextOffset );

            block.add(record);

            if(nextOffset==0) return block;

        }



        return block;



    }



    //레코드 삽입(byte version)
    public static void insertRecord2(String seqFileName, Record newRecord) throws IOException {
        RandomAccessFile seqFile=new RandomAccessFile(seqFileName+".dat","rw");
        boolean isAppend=false;

        // 1. 파일의 레코드를 순회하며 삽입할 레코드 앞에 위치한 레코드를 찾음
        String key=newRecord.getAttrs().get(0); //search key는 not null 조건이므로 무조건 찾음.
        SeqFileHeader header=SeqFileHeaderSystem.getSeqFileHeader(seqFile); //헤더-> 시작포인터 등등..


        int lastOffset=header.getRecordPointer(); //이전 레코드
        int currentOffset=header.getRecordPointer(); //현재 바라보는 것

        if(header.getRecordNum()==0){
            seqFile.seek(Config.FILE_HEADER_SIZE);
            byte[] nullbit=newRecord.getBitSet().toByteArray();
            if(nullbit.length==0){
                seqFile.writeByte(0);
            }
            else{

                seqFile.writeByte(nullbit[0]);
            }
            for(String attr:newRecord.getAttrs()) {
                seqFile.writeBytes(attr);
            }

            seqFile.writeInt(newRecord.getNextOffset()); //오프셋. -> -0으로 설정해야 할 듯


            //헤더 업데이트
            header.setRecordNum(header.getRecordNum()+1);
            header.setLastBlockOffset(header.getLastBlockOffset()+Config.BLOCK_SIZE);
            writeHeader(seqFile,header);

            return;

        }

        while(currentOffset!=0) { //0 되면 끝에 도달했다는 뜻

            Record CurrentRecord =readRecordByOffset(seqFile, currentOffset);
            String currentKey= CurrentRecord.getAttrs().get(0);
            if(key.compareTo(currentKey)<0) { //삽입될 레코드보다 큰 최초의 레코드를 발견했다면 삽입


                /**
                 * 새 블록을 할당한 뒤 linked list 중간에 삽입하는 알고리즘 적용
                 * 중간 삽입 케이스 중 자리가 없어 새로운 블럭 사용하는 케이스
                 */

                // 1. 삽입될 위치 계산
                int newBlockOffset= header.getLastBlockOffset()+Config.BLOCK_SIZE; //마지막 블럭보다 한 칸 뒤에 생성


                // 2. 새 블록 생성 --> 불필요
                byte[] newBlockBytes=new byte[Config.BLOCK_SIZE];

                byte[] byteRecord= newRecord.toBytes();
                //copy
                System.arraycopy(byteRecord, 0, newBlockBytes, 0, byteRecord.length);
                ByteBlock newBlock=new ByteBlock(newBlockBytes,newBlockOffset);


                /**
                 * 레코드 삽입 함수 변경(record 2)
                 */
                Record lastRecord=readRecordByOffset(seqFile, lastOffset);
                lastRecord.setNextOffset(newBlockOffset); //이전 레코드는 새로운 레코드를 가리키도록 함

                newRecord.setNextOffset(currentOffset); //새로운 레코드는 지금 레코드를 가리키도록 함

                writeRecord2(seqFile, lastRecord, lastOffset);
                writeRecord2(seqFile,newRecord,newBlockOffset);

                //헤더 파일 업데이트
                header.setRecordNum(header.getRecordNum()+1); //레코드 개수 증가
                header.setLastBlockOffset(newBlockOffset);
                writeHeader(seqFile,header);


                isAppend=true;
                break; //삽입 완료 시 탈출
            }
            else{
                lastOffset=currentOffset;
                currentOffset=CurrentRecord.getNextOffset();

            }

        }
        if(!isAppend) { //삽입이 안 됨 -> 맨 마지막에 삽입하는 뜻. 끝에 도달한 것임

            Record lastRecord=readRecordByOffset(seqFile, lastOffset);

            /**
             * 여유공간 계산에 오류가 있음.
             * 1->3->2 순으로 삽입할 때, 마지막 레코드는 3이지만, 마지막 블록은 2(140)에 해당해서 계산 제대로 안 됨.
             * lastOffset<lastBlockOffset이면 오류가 발생함.
             *
             */
            int remained=header.getLastBlockOffset()+Config.BLOCK_SIZE-lastOffset-lastRecord.getSize(); //마지막 블록 여유공간

            int newRecordOffset;
            // 1. 삽입될 위치 계산
            if(lastOffset< header.getLastBlockOffset()||remained<newRecord.getSize()) {//새로운 블럭 할당하는 케이스
                //새 블록 할당
                newRecordOffset= header.getLastBlockOffset()+Config.BLOCK_SIZE; //마지막 블럭보다 한 칸 뒤에 생성
                header.setLastBlockOffset(newRecordOffset);

            }
            else{
                //같은 블록에 넣기
                newRecordOffset= lastOffset+newRecord.getSize();

            }



            // 2. 레코드 삽입

            lastRecord.setNextOffset(newRecordOffset); //이전 레코드는 새로운 레코드를 가리키도록 함

            newRecord.setNextOffset(currentOffset); //새로운 레코드는 지금 레코드를 가리키도록 함 -> 0값이 들어갈거임.

            writeRecord2(seqFile, lastRecord, lastOffset);
            writeRecord2(seqFile,newRecord,newRecordOffset);

            // 헤더 update
            header.setRecordNum(header.getRecordNum()+1); //레코드 개수 증가
            writeHeader(seqFile,header);

        }
    }
// 주어진 파일명에 하나의 레코드를 삽입.
    public static void insertRecord(String seqFileName, Record newRecord) throws IOException {

        RandomAccessFile seqFile=new RandomAccessFile(seqFileName+".dat","rw");


        // 1. 파일의 레코드를 순회하며 삽입할 레코드 앞에 위치한 레코드를 찾음
        String key=newRecord.getAttrs().get(0); //search key는 not null 조건이므로 무조건 찾음.
        SeqFileHeader header=SeqFileHeaderSystem.getSeqFileHeader(seqFile); //헤더-> 시작포인터 등등..


        /**
         * 빈 파일일 때 -> 그냥 바로 삽입.
         */
        if(header.getRecordNum()==0){
            seqFile.seek(Config.FILE_HEADER_SIZE);
            byte[] nullbit=newRecord.getBitSet().toByteArray();
            if(nullbit.length==0){
                seqFile.writeByte(0);
            }
            else{

                seqFile.writeByte(nullbit[0]);
            }
            for(String attr:newRecord.getAttrs()) {
                seqFile.writeBytes(attr);
            }

            seqFile.writeInt(newRecord.getNextOffset()); //오프셋. -> -0으로 설정해야 할 듯


            //헤더 업데이트
            header.setRecordNum(header.getRecordNum()+1);
            header.setLastBlockOffset(header.getLastBlockOffset()+Config.BLOCK_SIZE);
            writeHeader(seqFile,header);

        }
        else{ //빈 파일이 아닐때 -> 검색으로 위치를 찾아야 함

            // 1. 순차 파일을 순회하며 올바른 위치 찾기
            // 반복해서 블럭을 가져온 다음, 해당 블럭에 레코드가 0개라면 종료
            int offset= header.getRecordPointer();
            boolean isAppend=false; //삽입이 완료되면 true로 변경
            while(!isAppend){
                Block block= readBlockByOffset(seqFile,offset);
                List<Record> records=block.getRecords();

                if(records.isEmpty()) break;

                //각 레코드별로 key와 비교함
                /**
                 * 수정필요(블럭의 첫 번째 위치에 삽입이 가능할 때 예외처리 불가)
                 * 1. 맨 뒤에 삽입이 가능할 때(같은 블럭)
                 * 2. 중간에 삽입하지만 공간이 없어 새로운 블럭을 할당
                 */
                for(int i=0;i<records.size();i++){

                    Record curRecord=records.get(i);
                    if(curRecord.getAttrs().getFirst().compareTo(key)>0){ //현재 레코드보다 앞에 위치해야 할 때 -> 중간 삽입
                        /**
                         * i==0이면 에러날 듯.
                         */
                        Record lastRecord=records.get(i-1); //이전 레코드를 불러와서 연결 시도
                        if(block.getRemainCapacity() < newRecord.getSize()){ //현재 블럭에 자리가 없을 때

                            //레코드 연결


                            //파일의 뒤에 새로운 블럭을 생성
                            Block newBlock=new Block();
                            int newOffset=header.getLastBlockOffset()+Config.BLOCK_SIZE;
                            newBlock.setBlockOffset(newOffset);

                            newRecord.setNextOffset(lastRecord.getNextOffset()); //이전 노드가 가리키던 것을 가리켜야 함
                            lastRecord.setNextOffset(newOffset); //이전 노드는 나를 가리켜야 함

                            newBlock.add(newRecord);

                            //헤더값 변경
                            header.setRecordNum(header.getRecordNum()+1);
                            header.setLastBlockOffset(newOffset);


                            //파일에 쓰기
                            writeHeader(seqFile,header);
                            writeNewBlock(seqFile,newBlock);
                            writeBlock(seqFile,block);

                            isAppend=true;
                            break;





                        }

                    }
                    else if(curRecord.getNextOffset()==0){ //파일의 마지막 레코드라면
                        // a. 같은 블럭에 저장이 가능할 때(뒤의 공간에 저장)
                        if(block.getRemainCapacity()>= newRecord.getSize()){

                            newRecord.setNextOffset(0);

                            curRecord.setNextOffset(block.getBlockOffset()+block.getSize()); //이어서 저장하겠다는 뜻
                            block.add(newRecord);

                            //헤더값 변경
                            header.setRecordNum(header.getRecordNum()+1);
                            //쓰기

                            writeHeader(seqFile,header);
                            writeBlock(seqFile,block);
                            isAppend=true;
                            break;


                        }

                        // b. 새로운 블럭에 저장해야 할 때
                        else{

                            Block newBlock=new Block();
                            int newOffset=header.getRecordPointer()+Config.BLOCK_SIZE;
                            newBlock.setBlockOffset(newOffset);
                            newRecord.setNextOffset(0);

                            //연결
                            curRecord.setNextOffset(newOffset);
                            /**
                             * 이전 블록의 값이 변경되는지 체크해야 함
                             */

                            newBlock.add(newRecord);

                            //헤더값 변경
                            header.setRecordNum(header.getRecordNum()+1);
                            header.setLastBlockOffset(newOffset);

                            //쓰기
                            writeHeader(seqFile,header);
                            writeBlock(seqFile,block);
                            writeNewBlock(seqFile,newBlock);
                            isAppend=true;
                            break;

                        }




                    }



                }










            }




        }




    }

    public static void writeRecord(RandomAccessFile seqFile, Record newRecord, int recordOffset) throws IOException {

        seqFile.seek(recordOffset);
        byte[] nullbit=newRecord.getBitSet().toByteArray();
        if(nullbit.length==0){
            seqFile.writeByte(0);
        }
        else{

            seqFile.writeByte(nullbit[0]);
        }
        for(String attr:newRecord.getAttrs()) {
            seqFile.writeBytes(attr);
        }

        seqFile.writeInt(newRecord.getNextOffset());
    }

    // 텍스트 파일을 받아서 튜플 삽입
    public static void insertRecordsByTxt(String recordInputPath) throws IOException {

        String seqFileName=null; //삽입할 파일명
        int recordNum; //삽입할 레코드 수



        BufferedReader reader = new BufferedReader(new FileReader(recordInputPath));
        String line;
        int lineNum = 1; //몇 번째 줄을 읽고 있는가?

        List<Record> records = new ArrayList<Record>();



        while ((line = reader.readLine()) != null) {

            switch (lineNum){
                    case 1:
                        //첫 번째 줄: 순차 파일의 이름이 저장되어 있음.
                        seqFileName = line;
                        break;


                    case 2:
                        // 두 번째 줄: 레코드 수
                        recordNum = Integer.parseInt(line);
                        break;

                   default:
                       // 나머지 줄: 레코드에 대한 정보가 들어있음.
                        String[] attrs = line.split(";");

                        //null bitmap 계산
                        BitSet bitSet = new BitSet(8); //bitset : null-true
                        for(int i=0;i<attrs.length;i++) {
                            bitSet.set(i,attrs[i].equals("null")); //null값과 일치하면 해당 인덱스의 비트를 1(true)로 변경
                        }

                        Record record=new Record(bitSet,attrs);
                        records.add(record);

                        insertRecord2(seqFileName, record); //하나의 레코드를 삽입
                        MysqlSystem.insertRecord(seqFileName, record); //mysql에도 삽입


                        break;

            }

            lineNum++;

        }

        //레코드가 리스트에 다 담긴 상태임. -> 잘 되는 것 확인.







        //리스트를 순회하며 블록 단위로 조립
//        List<Block> blocks=new ArrayList<Block>();
//        int curOffset=Config.FILE_HEADER_SIZE;
//        int curTotalSize=0; //현재 인덱스까지 레코드 크기의 합을 계산.
//        Block block=new Block();
//        for (Record record : records) {
//            record.setOffset(curOffset + record.getSize()); //다음 레코드의 오프셋 설정.
//            curTotalSize += record.getSize();
//            if (curTotalSize <= Config.BLOCK_SIZE) { //넣을 수 있으면 넣는다
//                block.add(record);
//            } else { //넣을 수 없다면 지금까지 넣은 블럭을 리스트에 저장하고 초기화
//                blocks.add(block);
//                curTotalSize = 0;
//                block = new Block();
//            }
//        }

        //블럭의 리스트가 있는 상태임.


    }

    // 파일에 주어진 블럭을 삽입
    public static void insertBlock(String seqFileName, Block block) throws IOException {

        RandomAccessFile seqFile=new RandomAccessFile(seqFileName+".dat","rw");

        for(Record record : block.getRecords()) {

        }
    }


    // create new sequence file

    // instructor;ID name dept_name salary;5 5 7 5
    public static void createNewSeqFile(String info) throws IOException {

        // 1. info 파싱
        String[] parsed=info.split(";");

        String fileName=parsed[0]; //파일명
        String[] attrs=parsed[1].split(" "); //속성 이름
        String[] charSize=parsed[2].split(" "); //각 속성 별 char 크기



        // 3. randomAccessFile 클래스를 이용한 .dat 파일 생성 및 저장.
        RandomAccessFile seqFile=new RandomAccessFile(fileName+".dat","rw");


        // 4. 그 파일을 가리키는 헤더 블록 생성 -> 파일의 맨 처음에 위치?
        createFileHeader(seqFile, attrs, charSize);

    }

    private static void createFileHeader(RandomAccessFile seqFile, String[] attrs, String[] charSize) throws IOException {

        seqFile.seek(0);
        seqFile.writeInt(Config.FILE_HEADER_SIZE); //맨 상단에는 헤더 블록의 크기를 오프셋으로 지정하여 다음 블록의 위치를 알게 함.

        /**
         * 메타데이터 추가
         * 첫 포인터(int)
         * 속성개수(int)
         * 포함된 레코드의 개수(int)
         * 각 속성별 char크기(int[])
         * 속성명(String[]) 순으로 적용
         */

        seqFile.writeInt(attrs.length); //속성개수
        seqFile.writeInt(0); //처음엔 0개로 시작함. 레코드 개수
        seqFile.writeInt(0); //가장 마지막에 있는 블럭의 시작주소(blockOffset)
        for(String size:charSize){ //각 char 크기
            seqFile.writeInt(Integer.parseInt(size));
        }
        for(String attr : attrs) {
            seqFile.writeUTF(attr); //근데 이거 크기가 얼마임?
        }

        printFileHeader(seqFile);

    }



}
