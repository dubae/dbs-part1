package org.example;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;


//        String url = "jdbc:mysql://localhost:3306/dbs";
//        String user = "root";
//        String password = "";
//
//        Connection conn = DriverManager.getConnection(url, user, password);
//        System.out.println("✅ 연결 성공!");


        while (running) {
            System.out.println("\n===== 메뉴 =====");
            System.out.println("1. 순차파일 생성");
            System.out.println("2. txt 파일을 이용한 레코드 삽입");
            System.out.println("3. 테이블 조인");
            System.out.println("4. 필드 검색");
            System.out.println("5. 레코드 검색");
            System.out.println("6. 종료");
            System.out.print("번호를 입력하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("명령어를 입력하세요 ex) instructor;ID name dept_name salary;5 5 7 5" );
                    String instructor = scanner.nextLine();
                    RDBController.createFile(instructor,null);
                    break;
                case 2:
                    System.out.println("txt 파일의 절대경로를 입력하세요. ex) /Users/duhoe/Desktop/dev/DBSystem/DBS-design1/instructor_input.txt" );
                    String txt = scanner.nextLine();
                    RDBController.insertRecordByTxt(txt);
                    break;
                case 3:

                    System.out.println("Equi Join을 위한 SQL문을 입력하세요. 문법을 준수해야 합니다. ex) select * from instructor join teaches on instructor.ID=teaches.ID");
                    String txt3 = scanner.nextLine();
                    RDBController.join(txt3);
                    break;

                case 4:
                    System.out.println("필드 검색 명령어를 입력하시오. 띄어쓰기가 없어야 합니다. ex) instructor,name" );
                    String txt4 = scanner.nextLine();
                    RDBController.searchField(txt4);
                    break;
                case 5:
                    System.out.println("레코드 검색 명령어를 입력하시오. ex) instructor,ID,00002,00003" );
                    String txt5 = scanner.nextLine();
                    RDBController.searchField(txt5);
                    RDBController.searchRecord(txt5);


                    break;




                    case 6:
                    running = false;
                    break;
                default:
                    System.out.println("⚠ 잘못된 입력입니다. 1~5 중에 선택해주세요.");
            }
        }

        scanner.close();
    }
}