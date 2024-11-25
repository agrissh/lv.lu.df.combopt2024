package lv.lu.df.combopt.defsched.chainbased.domain;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RealDataExtractor {
    public static void main(String[] args) {
        extractData("data/studenti2023kval.xlsx");
        testData("data/example_real_problem.json");
    }

    private static void testData(String filename) {
        DefenseScheduleJsonIO io = new DefenseScheduleJsonIO();
        DefenseSchedule problem = io.read(new File(filename));

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RealDataExtractor.class);

    static private Integer ID = 0;
    static private Integer NEXT_ID() { ID++;return ID;}

    public static String getStringValue(Cell cell) {
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : "";
    }
    private static void extractData(String filename) {
        try {
            DefenseSchedule problem = new DefenseSchedule();
            problem.setScheduleId(2023);

            Program LV = new Program("LV");
            Program LV_B = new Program("LV_B");
            Program EN = new Program("EN");
            problem.getPrograms().addAll(List.of(LV,LV_B,EN));

            createCommission(15,problem,"12");
            createCommission(15,problem,"13");
            createCommission(15,problem,"16");
            createCommission(15,problem,"18");
            createCommission(16,problem,"12");
            createCommission(16,problem,"13");
            createCommission(16,problem,"16");
            createCommission(17,problem,"12");
            createCommission(17,problem,"13");
            createCommission(17,problem,"16");
            createCommission(18,problem,"12");
            createCommission(18,problem,"13");
            createCommission(18,problem,"16");
            createCommission(18,problem,"18");
            createCommission(18,problem,"14");
            createCommission(19,problem,"12");
            createCommission(19,problem,"13");
            createCommission(19,problem,"16");

            Map<String, Person> allThePossiblePersons = new HashMap<>();
            List<Member> allThePossibleMembers = new ArrayList<>();
            FileInputStream fileInputStream = new FileInputStream(new File(filename));
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet VadSheet = workbook.getSheet("Vad");
            for(Row row : VadSheet) {
                if (row.getRowNum() > 0 && row.getRowNum() < 664) {
                    Person person = new Person();
                    // 0 - id, 1 - uzvards, 2 - vards, 3 - LU amats, 4 - grads, 5 - pkods, 6 - telefons, 7 - epasts,
                    // 8 - darba vieta, 9 - amats ne LU,
                    String personId = getStringValue(row.getCell(0));
                    person.setPersonId(NEXT_ID());
                    person.setName(getStringValue(row.getCell(2)) + " " + getStringValue(row.getCell(1)));
                    //person.setEmail(getStringValue(row.getCell(7)));
                    //person.setPhone(getStringValue(row.getCell(6)));
                    //person.setTitle(getStringValue(row.getCell(4)));
                    //person.setEmployer(getStringValue(row.getCell(8)));
                    //person.setAffiliation(getStringValue(row.getCell(3)).equals("") ?
                    //        getStringValue(row.getCell(9)) : getStringValue(row.getCell(3)));
                    //person.setAvailableTimeSlots(sch.timeslotList);
                    allThePossiblePersons.put(personId, person);
                }
            }

            // Now look for time constraints
            Sheet mEMSheet = workbook.getSheet("mEMBERS");
            for(Row row : mEMSheet) {
                if (row.getRowNum() > 1 && row.getRowNum() < 106) {
                    String personId = getStringValue(row.getCell(1));
                    Person person = allThePossiblePersons.get(personId);
                    if (person == null) {
                        LOGGER.error("No such person: " + personId);
                    } else {
                        // 3,4,5,6,7 --> cells for dates 15,16,17,18,19
                        // 2- available, -1 - not available, 1 - available but not preffered, 0 - no data
                        createTimeConstraint(3, person, row);
                        createTimeConstraint(4, person, row);
                        createTimeConstraint(5, person, row);
                        createTimeConstraint(6, person, row);
                        createTimeConstraint(7, person, row);
                    }
                }
            }

            // Now we should identify which members are in the commission tab cOMMISSION
            Sheet cOMSheet = workbook.getSheet("cOMMISSION");
            for(Row row : cOMSheet) {
                if (row.getRowNum() >= 0 && row.getRowNum() < 60) {
                    String personId = getStringValue(row.getCell(1));
                    Person person = allThePossiblePersons.get(personId);
                    if (person == null) {
                        LOGGER.error("No such person: " + personId);
                    } else {
                        Member member = new Member();
                        double memberType = row.getCell(0).getNumericCellValue();
                        // 4 HEAD, industry
                        // 3 MEMBER, industry
                        // 2 SECRETARY, academic
                        // 1 MEMBER, academic
                        member.setMemberId(NEXT_ID());
                        member.setRole(memberType == 4 ? MemberRole.CHIEF
                                : (memberType == 2 ? MemberRole.SECRETARY
                                : MemberRole.MEMBER));
                        member.setFromIndustry(memberType >= 3);
                        member.setProgram(LV);
                        allThePossibleMembers.add(member);
                        person.getMembership().add(member);
                    }
                }
            }
            // Now go through thesis list and create thesis + Student + Add reviewer and supervisor
            // Tab sTUDENTS 0 - uzvārds, 1 - vārds, 2 - id, 3 - email, 4 - e or L, 5 - e,k,B 6 - Nosaukums,
            // 7 - Title_EN, 8 - vadītājs, 9 - recenzents
            Sheet sTUDSheet = workbook.getSheet("sTUDENTS");
            for(Row row : sTUDSheet) {
                if (row.getRowNum() > 0 && row.getRowNum() < 133) {
                    Person student = new Person();
                    String studentId = getStringValue(row.getCell(2));
                    student.setPersonId(NEXT_ID());
                    student.setName(getStringValue(row.getCell(1)) + " " + getStringValue(row.getCell(0)));
                    //student.setEmail(getStringValue(row.getCell(3)));

                    //student.setAvailableTimeSlots(sch.timeslotList);
                    //sch.nonMemberList.add(student);
                    allThePossiblePersons.put(studentId,student);

                    Thesis thesis = new Thesis();
                    thesis.setAuthor(student);
                    thesis.setThesisId(NEXT_ID());
                    thesis.setTitle(getStringValue(row.getCell(6)));
                    //thesis.setTitle_EN(getStringValue(row.getCell(7)));

                    thesis.setProgram(getStringValue(row.getCell(5)).equals("e") ? EN : (
                            getStringValue(row.getCell(5)).equals("B") ? LV_B : LV
                    ));

                    String supervisorId = getStringValue(row.getCell(8));
                    String reviewerId = getStringValue(row.getCell(9));

                    Person supervisor = allThePossiblePersons.get(supervisorId);
                    if (supervisor == null) {
                        LOGGER.error("No such person: " + supervisorId);
                    } else {
                        thesis.setSupervisor(supervisor);
                    };

                    Person reviewer = allThePossiblePersons.get(reviewerId);
                    if (reviewer == null) {
                        LOGGER.error("No such person: " + reviewerId);
                    } else {
                        thesis.setReviewer(reviewer);
                    };

                    problem.getThesis().add(thesis);
                }
            }

            problem.setPersons(allThePossiblePersons.values().stream().collect(Collectors.toList()));
            problem.setMembers(allThePossibleMembers);

            DefenseScheduleJsonIO io = new DefenseScheduleJsonIO();
            io.write(problem, new File("data/example_real_problem.json"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void createCommission(int i, DefenseSchedule problem, String room) {
        Session session = new Session();
        session.setSessionStart(LocalDateTime.of(2024, 1, i, 15,0,0));
        session.setRoom(room);
        session.setSlotDurationMinutes(30);
        session.setSessionId(NEXT_ID());
        // HEAD + SEC + 3 mandatory
        SessionMember hseat = new SessionMember(NEXT_ID(), null, MemberRole.CHIEF);
        SessionMember sseat = new SessionMember(NEXT_ID(), null, MemberRole.SECRETARY);
        SessionMember seat1 = new SessionMember(NEXT_ID(), null, null);
        SessionMember seat2 = new SessionMember(NEXT_ID(), null, null);
        SessionMember seat3 = new SessionMember(NEXT_ID(), null, null);
        problem.getSessionMembers().addAll(List.of(hseat, sseat, seat1, seat2, seat3));
        session.getMembers().addAll(List.of(hseat, sseat, seat1, seat2, seat3));
        problem.getSessions().add(session);
    }

    private static void createTimeConstraint(int column, Person person, Row row) {
        Integer[] column2dates = {0, 0, 0, 15, 16, 17, 18, 19};
        double tc = row.getCell(column).getNumericCellValue();
        if (tc == -1) {
            person.getTimeConstraints().add(new TimeConstraint(
                    NEXT_ID(),
                    LocalDateTime.of(2024, 1, column2dates[column], 0,0,0),
                    LocalDateTime.of(2024, 1, column2dates[column], 23,59,59)
            ));
        }
    }
}
