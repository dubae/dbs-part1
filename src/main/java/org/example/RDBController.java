package org.example;

import org.example.system.DbSystem;
import org.example.system.SeqFileSystem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

//최상위 레이어로서, 화일 생성/레코드 삽입/레코드 검색/ 필드 조회의 업무를 최상단에서 수행
public class RDBController {

    //instructor;ID name dept_name salary;5 5 7 5
    public static void createFile(String info, Connection conn) throws IOException, SQLException {

        DbSystem.createDbTable(info, conn); //DBMS상에서 테이블을 생성

        SeqFileSystem.createNewSeqFile(info); //실제 파일시스템으로, 하드디스크에 저장
    }


    public static void insertRecordByTxt(String recordInputPath) throws IOException { //주소(절대주소?)를 받아서 파싱하기


        SeqFileSystem.insertRecordsByTxt(recordInputPath);


    }

    public static void searchField(String info) throws IOException {
        SeqFileSystem.searchField(info);

    }

    public static void searchRecord(String info) throws IOException {
        SeqFileSystem.searchRecord(info);
    }
}
