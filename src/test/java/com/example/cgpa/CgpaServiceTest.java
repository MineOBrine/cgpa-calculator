package com.example.cgpa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CgpaServiceTest {

    private final CgpaService service = new CgpaService();

    private final List<CgpaService.Course> sem = List.of(
            new CgpaService.Course("Maths", 4, "A"),   // 8 * 4 = 32
            new CgpaService.Course("Physics", 3, "O"), // 10 * 3 = 30
            new CgpaService.Course("Lab", 1, "B"));    // 6 * 1 = 6

    @Test
    void sgpaWeightsGradePointsByCredits() {
        var result = service.calculate(sem, List.of());
        assertEquals(8, result.totalCredits());
        assertEquals(68, result.totalWeighted());
        assertEquals(8.5, result.sgpa(), 0.0001);
        assertNull(result.cgpa());
        assertEquals(80.75, result.percentage(), 0.0001);
        assertEquals("SGPA", result.percentageBasis());
    }

    @Test
    void cgpaCombinesEarlierSemesters() {
        // (68 + 9.0*20) / (8 + 20) = 248 / 28
        var result = service.calculate(sem, List.of(new CgpaService.Semester(9.0, 20)));
        assertEquals(8.5, result.sgpa(), 0.0001);
        assertEquals(248.0 / 28.0, result.cgpa(), 0.0001);
        assertEquals(2, result.semesterCount());
        assertEquals(1, result.previous().size());
        assertEquals(9.0, result.previous().get(0).sgpa(), 0.0001);
        assertEquals("CGPA", result.percentageBasis());
    }

    @Test
    void blankPreviousRowsAreIgnored() {
        var parsed = service.parsePrevious(List.of(new CgpaService.PreviousEntry("", " ")));
        assertTrue(parsed.isEmpty());
    }

    @Test
    void halfFilledPreviousRowIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.parsePrevious(List.of(new CgpaService.PreviousEntry("8.5", ""))));
    }

    @Test
    void rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(List.of(), List.of()));
    }

    @Test
    void rejectsZeroCredits() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(List.of(new CgpaService.Course("X", 0, "A")), List.of()));
    }
}
