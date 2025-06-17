package com.example.group56.service;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.common.EntityUtils;
import com.example.group56.model.Course;
import com.example.group56.model.Enrolment;
import com.example.group56.model.User;
import com.example.group56.repo.CourseRepository;
import com.example.group56.repo.EnrolmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EnrolmentService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrolmentRepository enrolmentRepository;

    // Creates a new enrolment to signify the new relationship between a user and course.
    public void enrolUserOnCourse(User user, Course course) {
        enrolmentRepository.save(new Enrolment(course, user));
    }

    // Shorthand way of enrolling a user with minimal other information. Used to generate sample data.
    public void enrolUserOnCourseById(User user, long courseId) {
        enrolUserOnCourse(user, courseRepository.findById(courseId).orElse(null));
    }

    // Marks an enrolment's startTime as now() and updates database. If the user is not enrolled yet, a new enrolment is made.
    public void startUserOnCourse(User user, Course course) {
        Enrolment enrolment = getEnrolmentByUserAndCourse(user, course).orElse(new Enrolment(course, user));
        enrolment.setStartTime(LocalDateTime.now());
        enrolmentRepository.save(enrolment);
    }

    // Shorthand way of starting a user on a course with minimal other information. Used to generate sample data.
    public void startUserOnCourseById(User user, long courseId) {
        startUserOnCourse(user, courseRepository.findById(courseId).orElse(null));
    }

    // Marks an enrolment's completionTime as now() and updates database. If the user is not enrolled yet, a new enrolment is made.
    public void completeUserOnCourse(User user, Course course) {
        Enrolment enrolment = getEnrolmentByUserAndCourse(user, course).orElse(new Enrolment(course, user));
        enrolment.setCompletionTime(LocalDateTime.now());
        enrolmentRepository.save(enrolment);
    }

    // Shorthand way of marking a user as having completed a course, with minimal other information. Used to generate sample data.
    public void completeUserOnCourseById(User user, long courseId) {
        completeUserOnCourse(user, courseRepository.findById(courseId).orElse(null));
    }

    // Json-friendly combination of course and enrolment data for ease-of-access.
    @JsonCompatible
    public Map<String, Object> getEnrolmentData(Enrolment enrolment) {
        Course course = enrolment.getCourse();

        Map<String, Object> courseData = EntityUtils.getFields(course);
        Map<String, Object> enrolmentData = EntityUtils.getFields(enrolment);
        enrolmentData.remove("user");
        courseData.remove("id");
        enrolmentData.putAll(courseData);
        return enrolmentData;
    }

    /* Access to repo directly to save injecting both into a controller. */

    public List<Enrolment> getEnrolmentByUser(User user) {
        return enrolmentRepository.findByUser(user);
    }

    public Optional<Enrolment> getEnrolmentByUserAndCourse(User user, Course course) {
        return enrolmentRepository.findByUserAndCourse(user, course);
    }
}
