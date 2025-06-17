package com.example.group56.controller;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.model.LearningPath;

import com.example.group56.repo.LearningPathRepository;
import com.example.group56.service.CourseService;
import com.example.group56.common.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

// Mappings summary:
// /api/learning-path/{id}  - Gets all relevant data for a learning path
@Controller
public class LearningPathController {
    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private CourseService courseService;

    // Returns JSON-friendly data of a learning path and all courses within it.
    @GetMapping("/api/learning-path/{id}")
    @ResponseBody @JsonCompatible
    public Map<String, Object> getLearningPathData(@PathVariable long id) {
        LearningPath learningPath = learningPathRepository.findById(id).orElse(null);
        if (learningPath == null) return null;

        Map<String, Object> data = EntityUtils.getFields(learningPath);
        @SuppressWarnings("unchecked")
        List<Object> courseIds = (List<Object>) data.get("courses");
        courseIds.replaceAll(o -> courseService.getCourseData((Long) o));
        return data;
    }
}
