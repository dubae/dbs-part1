package org.example;

import org.example.system.MysqlSystem;
import org.example.system.SeqFileSystem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

//최상위 레이어로서, 화일 생성/레코드 삽입/레코드 검색/ 필드 조회의 업무를 최상단에서 수행
public class RDBController {

    public static void join(String userSql) {
        try {
            // 소문자로 변환 후 공백 기준 분리
            String normalizedSql = userSql.trim().toLowerCase();
            String[] tokens = normalizedSql.split("\\s+");

            // 기본 구문 체크: select * from table1 join table2 on table1.ID = table2.ID
            if (!normalizedSql.startsWith("select * from") || !normalizedSql.contains("join") || !normalizedSql.contains("on")) {
                throw new IllegalArgumentException("지원되지 않는 SQL 형식입니다.");
            }

            // 파싱
            String table1 = tokens[3]; // from 다음
            String table2 = tokens[5]; // join 다음

            // join 함수 호출
            SeqFileSystem.join(table1, table2); //내 파일로 조인
            MysqlSystem.join(table1, table2); // mysql join

        } catch (Exception e) {
            System.err.println("파싱 오류: " + e.getMessage());
        }
    }

    //instructor;ID name dept_name salary;5 5 7 5
    public static void createFile(String info, Connection conn) throws IOException, SQLException {

        MysqlSystem.createDbTable(info, conn); //DBMS상에서 테이블을 생성

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
