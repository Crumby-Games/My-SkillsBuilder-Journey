package com.example.group56.service;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.common.EntityUtils;
import com.example.group56.model.Course;
import com.example.group56.model.Question;
import com.example.group56.repo.CourseRepository;
import com.example.group56.repo.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public List<Course> getAllCourses() { return courseRepository.findAll(); }
    public Course getCourseByName(String name) { return courseRepository.findByName(name); }

    // Shorthand for saving both a course and its quiz questions at the same time
    public void saveCourseWithQuiz(Course course, List<Question> questions) {
        courseRepository.save(course);

        if(questions == null) return;

        for (Question question : questions) {
            question.setCourse(course);
            questionRepository.save(question);
        }
    }

    // Returns JSON-friendly generic course data. Referenced in multiple controllers.
    @JsonCompatible
    public Map<String, Object> getCourseData(@RequestParam long id) {
        Course course = courseRepository.findById(id).orElse(null);

        if(course == null) return null;

        return EntityUtils.getFields(course);
    }

    /* Access to repo directly to save injecting both into a controller. */

    public boolean isRepoEmpty() { return courseRepository.count() == 0; }
}
