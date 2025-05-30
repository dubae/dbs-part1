import org.example.system.MysqlSystem;
import org.example.system.SeqFileSystem;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

import org.example.object.Record;


public class test {

    //test
    public static void main(String[] args) throws IOException {


        //test_join_insertion();
        test_join("instructor", "teaches");



    }

    static void test_join_insertion() throws IOException {
        SeqFileSystem.createNewSeqFile("instructor;ID name dept_name salary;5 5 7 5");
        SeqFileSystem.insertRecordsByTxt("/Users/duhoe/Desktop/dev/DBSystem/DBS-design1/instructor_input.txt");

        SeqFileSystem.createNewSeqFile("teaches;ID course_id sec_id semester year;5 4 4 1 4");
        SeqFileSystem.insertRecordsByTxt("/Users/duhoe/Desktop/dev/DBSystem/DBS-design1/teaches_input.txt");

        RandomAccessFile seqFile1=new RandomAccessFile("instructor.dat","rw");
        RandomAccessFile seqFile2=new RandomAccessFile("teaches.dat","rw");

        SeqFileSystem.printAllRecords(seqFile1);
        SeqFileSystem.printAllRecords(seqFile2);
    }

    static void test_join(String table1, String table2) throws IOException {
        SeqFileSystem.join(table1,table2);
        MysqlSystem.join(table1,table2);
    }


    public static void TESTinsertRecord() throws IOException {

        BitSet bitset = new BitSet(8);
        String[] attrs={"00001","Duhoe","compsci","10000"};
        int offset=0;
        Record record=new Record(bitset,attrs,offset);
        SeqFileSystem.insertRecord2("instructor",record );

//        BitSet bitset4 = new BitSet(8);
//        String[] attrs4={"00004","fourr","adsdsds","44000"};
//        int offset4=0;
//        Record record4=new Record(bitset4,attrs4,offset4);
//        SeqFileSystem.insertRecord("instructor",record4 );
//
        BitSet bitset2 = new BitSet(8);
        String[] attrs2={"00002","holly","compsce","12000"};
        int offset2=0;
        Record record2=new Record(bitset2,attrs2,offset2);
        SeqFileSystem.insertRecord("instructor",record2 );

//        BitSet bitset3 = new BitSet(8);
//        bitset3.set(2);
//        String[] attrs3={"00003","holly","13000"};
//        int offset3=0;
//        Record record3=new Record(bitset3,attrs3,offset3);
//        SeqFileSystem.insertRecord("instructor",record3 );





    }

    public static BitSet fromByte(byte b) {
        BitSet bitSet = new BitSet(8);
        for (int i = 0; i < 8; i++) {
            // 상위 비트부터 BitSet에 저장 (BitSet은 LSB 우선이니까 순서 보정)
            if ((b & (1 << (7 - i))) != 0) {
                bitSet.set(7-i);
            }
        }
        return bitSet;
    }

    public static void TESTgetBlockByOffset() throws IOException {
        RandomAccessFile seqFile=new RandomAccessFile("instructor.dat","rw");
        SeqFileSystem.readBlockByOffset(seqFile, 60);
    }


    public static void test_insertTxt(String txtPath) throws IOException {
        SeqFileSystem.createNewSeqFile("instructor;ID name dept_name salary;5 5 7 5");
        SeqFileSystem.insertRecordsByTxt("/Users/duhoe/Desktop/dev/DBSystem/DBS-design1/record_input.txt");
    }


}
