package org.example.system;


import org.example.object.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//DBMS에 테이블 추가, 정보 가져오기 등
public class MysqlSystem {

    static String url = "jdbc:mysql://localhost:3306/dbs";
    static String user = "root";
    static String password = ""; // 비번 있으면 입력



    public static void join(String table1, String table2) {
        String key1 = getColumnList(table1).getFirst(); // 테이블 컬럼 목록
        String key2 = getColumnList(table2).getFirst(); // 테이블 컬럼 목록

        String sql = "SELECT * FROM " + table1 + " JOIN " + table2 + " ON " + table1 + "."+key1+" = " + table2 + "."+key2;

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
        ) {
            int columnCount = rs.getMetaData().getColumnCount();

            System.out.println("\n===MYSQL JOIN 결과===");
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();
            while (rs.next()) {

                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println(); // 줄 바꿈
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertRecord(String table, Record record) {

        List<String> columns = getColumnList(table); // 테이블 컬럼 목록
        List<String> values = record.getAttrs();     // 실제 값 목록

        if (columns.size() != values.size()) {
            throw new IllegalArgumentException("컬럼 수와 값 수가 일치하지 않습니다.");
        }

        // INSERT INTO table (col1, col2, ...) VALUES (?, ?, ...)
        String columnPart = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO " + table + " (" + columnPart + ") VALUES (" + placeholders + ")";

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i)); // 1-based index
            }

            int rows = pstmt.executeUpdate();
            System.out.println(rows + " row(s) inserted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getColumnList(String tableName) {

        List<String> columnList = new ArrayList<>();
        // MySQL 연결 정보


        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM "+tableName+" WHERE 1 != 0");
            ResultSetMetaData rsmd =  rs.getMetaData();

            // 컬럼의 인덱스가 1부터 시작하기 때문에 for-loop도 1부터 시작하도록 함
            for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                // columnType은 java.sql.Types 에 선언되어있다.
                int columnType = rsmd.getColumnType(i);
                String columnName = rsmd.getColumnName(i);
                columnList.add(columnName);
            }


        } catch (Exception e) {
            System.out.println("에러 발생:");
            e.printStackTrace();
        }

        return columnList;

    }


    public static void createDbTable(String info, Connection conn2) throws SQLException {



        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 파싱
            // 1. info 파싱
            String[] parsed=info.split(";");

            String fileName=parsed[0]; //파일명
            String[] attrs=parsed[1].split(" "); //속성 이름
            String[] charSize=parsed[2].split(" "); //각 속성 별 char 크기



            // CREATE TABLE 쿼리 생성
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS ").append(fileName).append(" (\n");
            for (int i = 0; i < attrs.length; i++) {
                sb.append("  ").append(attrs[i])
                        .append(" CHAR(").append(charSize[i]).append(")");
                if (i != attrs.length - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append(");");

            String sql = sb.toString();
            System.out.println("JDBC 생성 쿼리:\n" + sql);

            // 쿼리 실행
            stmt.executeUpdate(sql);
            System.out.println("JDBC 테이블 생성 완료!");

        } catch (Exception e) {
            System.out.println("에러 발생:");
            e.printStackTrace();
        }
    }
}
