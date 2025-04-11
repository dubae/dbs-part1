package org.example.system;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;

//DBMS에 테이블 추가, 정보 가져오기 등
public class DbSystem {

    public static void createDbTable(String info, Connection conn2) throws SQLException {

        // MySQL 연결 정보
        String url = "jdbc:mysql://localhost:3306/dbs";
        String user = "root";
        String password = ""; // 비번 있으면 입력

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
            System.out.println("🛠 생성 쿼리:\n" + sql);

            // 쿼리 실행
            stmt.executeUpdate(sql);
            System.out.println("✅ 테이블 생성 완료!");

        } catch (Exception e) {
            System.out.println("❌ 에러 발생:");
            e.printStackTrace();
        }
    }
}
