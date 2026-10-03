package com.example.cgpa;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CgpaController {

    private static final String SESSION_COURSES = "courses";
    private static final String SESSION_PREVIOUS = "previous";

    private final CgpaService service;

    public CgpaController(CgpaService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        return showForm(session, model);
    }

    /** Forget saved entries and go back to the pre-filled course list. */
    @GetMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(SESSION_COURSES);
        session.removeAttribute(SESSION_PREVIOUS);
        return "redirect:/";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam("name") List<String> names,
                            @RequestParam("credits") List<Double> credits,
                            @RequestParam("grade") List<String> grades,
                            @RequestParam(name = "prevSgpa", required = false) List<String> prevSgpa,
                            @RequestParam(name = "prevCredits", required = false) List<String> prevCredits,
                            HttpSession session,
                            Model model) {
        List<CgpaService.Course> courses = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i).isBlank() ? "Course " + (i + 1) : names.get(i).trim();
            courses.add(new CgpaService.Course(name, credits.get(i), grades.get(i)));
        }

        List<CgpaService.PreviousEntry> previous = new ArrayList<>();
        if (prevSgpa != null && prevCredits != null) {
            for (int i = 0; i < Math.min(prevSgpa.size(), prevCredits.size()); i++) {
                previous.add(new CgpaService.PreviousEntry(prevSgpa.get(i), prevCredits.get(i)));
            }
        }

        // Remember what was entered so "Edit" shows it again.
        session.setAttribute(SESSION_COURSES, courses);
        session.setAttribute(SESSION_PREVIOUS, previous);

        try {
            model.addAttribute("result", service.calculate(courses, service.parsePrevious(previous)));
            return "result";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return showForm(session, model);
        }
    }

    @SuppressWarnings("unchecked")
    private String showForm(HttpSession session, Model model) {
        List<CgpaService.Course> saved = (List<CgpaService.Course>) session.getAttribute(SESSION_COURSES);
        List<CgpaService.PreviousEntry> prev = (List<CgpaService.PreviousEntry>) session.getAttribute(SESSION_PREVIOUS);
        if (prev == null || prev.isEmpty()) {
            prev = List.of(new CgpaService.PreviousEntry("", ""));
        }
        model.addAttribute("grades", CgpaService.GRADE_POINTS);
        model.addAttribute("courses", saved != null ? saved : service.defaultCourses());
        model.addAttribute("previous", prev);
        return "index";
    }
}
