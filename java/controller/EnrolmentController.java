package com.example.group56.controller;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.model.Course;
import com.example.group56.model.Enrolment;
import com.example.group56.model.User;
import com.example.group56.repo.CourseRepository;
import com.example.group56.common.EntityUtils;
import com.example.group56.service.EnrolmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Mappings summary:
// /api/course/{id}/enrol               - Mark user as enrolled in database
// /api/course/{id}/start               - Mark user start time in database
// /api/course/{id}/complete            - Mark user completion time in database
// /api/course/{id}/enrolment           - Returns a combination of enrolment and course data of a course in a JSON-friendly format.
@Controller
public class EnrolmentController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrolmentService enrolmentService;

    // Returns course and enrolment data in one JSON-friendly mapping
    @GetMapping("/api/course/{id}/enrolment")
    @ResponseBody @JsonCompatible
    public Map<String, Object> getEnrolmentData(@AuthenticationPrincipal User user, @PathVariable long id) {
        Course course = courseRepository.findById(id).orElse(null);
        Enrolment enrolment = enrolmentService.getEnrolmentByUserAndCourse(user, course).orElse(null);

        if (course == null) return null;

        Map<String, Object> courseData = EntityUtils.getFields(course);
        Map<String, Object> enrolmentData;
        if (enrolment != null) {
            enrolmentData = EntityUtils.getFields(enrolment);
        } else {
            enrolmentData = EntityUtils.getNullFields(Enrolment.class);
            enrolmentData.put("status","NOT_ENROLLED");
        }
        enrolmentData.remove("course");
        courseData.putAll(enrolmentData);

        return courseData;
    }
}
