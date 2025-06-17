package com.example.group56.controller;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.model.Course;
import com.example.group56.model.Enrolment;
import com.example.group56.model.User;
import com.example.group56.repo.CourseRepository;
import com.example.group56.repo.EnrolmentRepository;
import com.example.group56.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Mappings summary:
// /courses           - shows a grid of all possible courses the user can enrol on
// /api/course/{id}   - (JSON-friendly) returns all relevant data of a course
@Controller
@RequestMapping
public class CourseController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrolmentRepository enrolmentRepository;

    // Displays template that allows the user to search and filter between all courses
    @GetMapping("/courses")
    public String showCourses(Model model) {
        List<Course> courses = courseRepository.findAll();

        List<Course> enrolledCourses = new ArrayList<>();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = (User) principal;
        List<Enrolment> enrolments = enrolmentRepository.findByUser(user);
        for (Enrolment enrolment : enrolments) {
            enrolledCourses.add(enrolment.getCourse());
        }

        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourses", enrolledCourses);

        return "courses";
    }

    // Returns all relevant data of a course in a JSON-friendly format.
    @GetMapping("/api/course/{id}")
    @ResponseBody @JsonCompatible
    public Map <String, Object> getCourseData(@PathVariable long id) {
        return courseService.getCourseData(id);
    }
}