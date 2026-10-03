package com.example.cgpa;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CgpaService {

    /** Common 10-point scale. Adjust to match your university. */
    public static final Map<String, Integer> GRADE_POINTS = new LinkedHashMap<>();
    static {
        GRADE_POINTS.put("O", 10);
        GRADE_POINTS.put("A+", 9);
        GRADE_POINTS.put("A", 8);
        GRADE_POINTS.put("B+", 7);
        GRADE_POINTS.put("B", 6);
        GRADE_POINTS.put("C", 5);
        GRADE_POINTS.put("P", 4);
        GRADE_POINTS.put("F", 0);
    }

    /** Multiplier commonly used to approximate percentage from CGPA. */
    public static final double PERCENTAGE_FACTOR = 9.5;

    /** A course in the current semester. */
    public record Course(String name, double credits, String grade) {}

    /** A finished earlier semester: its SGPA and total credits. */
    public record Semester(double sgpa, double credits) {}

    /** Raw text typed for an earlier semester (kept so the form can be re-shown as entered). */
    public record PreviousEntry(String sgpa, String credits) {}

    public record Row(String name, double credits, String grade, int points, double weighted) {}

    /** cgpa is null when no earlier semesters were entered. */
    public record Result(List<Row> rows, double totalCredits, double totalWeighted,
                         double sgpa, Double cgpa, int semesterCount, List<Semester> previous) {

        public double percentage() {
            return (cgpa != null ? cgpa : sgpa) * PERCENTAGE_FACTOR;
        }

        public String percentageBasis() {
            return cgpa != null ? "CGPA" : "SGPA";
        }
    }

    /** Pre-filled course list (name + credits). Grades are left for the user to choose. */
    public List<Course> defaultCourses() {
        return List.of(
                new Course("Basics of Financial Services", 2, ""),
                new Course("Computer Organization & Architecture", 3, ""),
                new Course("Computer Organization & Architecture Lab", 1, ""),
                new Course("Applied Mathematics Thinking-II", 3, ""),
                new Course("Operating System", 3, ""),
                new Course("Computer Network & Network Design", 3, ""),
                new Course("Unix Lab", 1, ""),
                new Course("Network Design Lab", 1, ""),
                new Course("Mini-Project - Programming Paradigm", 2, ""),
                new Course("Business Model Development", 2, ""),
                new Course("Design Thinking", 2, ""));
    }

    /** Turns typed text into semesters. Fully blank rows are skipped. */
    public List<Semester> parsePrevious(List<PreviousEntry> entries) {
        List<Semester> out = new ArrayList<>();
        for (PreviousEntry e : entries) {
            String s = e.sgpa() == null ? "" : e.sgpa().trim();
            String c = e.credits() == null ? "" : e.credits().trim();
            if (s.isEmpty() && c.isEmpty()) continue;
            if (s.isEmpty() || c.isEmpty()) {
                throw new IllegalArgumentException("Enter both SGPA and credits for each earlier semester.");
            }
            double sgpa, credits;
            try {
                sgpa = Double.parseDouble(s);
                credits = Double.parseDouble(c);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Earlier semester SGPA and credits must be numbers.");
            }
            out.add(new Semester(sgpa, credits));
        }
        return out;
    }

    public Result calculate(List<Course> courses, List<Semester> previous) {
        if (courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("Add at least one course.");
        }

        List<Row> rows = new ArrayList<>();
        double totalCredits = 0;
        double totalWeighted = 0;

        for (Course c : courses) {
            if (c.credits() <= 0) {
                throw new IllegalArgumentException("Credits must be greater than 0 for every course.");
            }
            Integer points = GRADE_POINTS.get(c.grade());
            if (points == null) {
                throw new IllegalArgumentException("Choose a grade for " + c.name() + ".");
            }
            double weighted = points * c.credits();
            rows.add(new Row(c.name(), c.credits(), c.grade(), points, weighted));
            totalCredits += c.credits();
            totalWeighted += weighted;
        }

        double sgpa = totalWeighted / totalCredits;

        Double cgpa = null;
        if (previous != null && !previous.isEmpty()) {
            double allCredits = totalCredits;
            double allWeighted = totalWeighted;
            for (Semester s : previous) {
                if (s.sgpa() < 0 || s.sgpa() > 10) {
                    throw new IllegalArgumentException("Earlier SGPA must be between 0 and 10.");
                }
                if (s.credits() <= 0) {
                    throw new IllegalArgumentException("Earlier semester credits must be greater than 0.");
                }
                allCredits += s.credits();
                allWeighted += s.sgpa() * s.credits();
            }
            cgpa = allWeighted / allCredits;
        }

        int semesters = 1 + (previous == null ? 0 : previous.size());
        return new Result(rows, totalCredits, totalWeighted, sgpa, cgpa, semesters,
                previous == null ? List.of() : List.copyOf(previous));
    }
}
