package com.example.group56.controller;

import com.example.group56.repo.LearningPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Mappings summary:
// /                                    - Redirects to /dashboard.
// /dashboard                           - Displays basic welcoming basic and prompts user to select learning path.
// /dashboard?select={learningPathId}   - Displays interactive learning path.
@Controller
public class HomeController {
    @Autowired
    private LearningPathRepository learningPathRepository;

    @GetMapping("/")
    public String start() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @RequestParam(defaultValue = "0") long select) {
        model.addAttribute("selectedPathId", select);

        // Adds all learningPaths to the model for the dropdown.
        model.addAttribute("learningPaths", learningPathRepository.findAll());
        return "dashboard";
    }

}
