package com.example.group56.model;

import jakarta.persistence.*;

import java.util.List;

/* Generic information relating to a specific course. */

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private final LearningPath learningPath;

    @ManyToOne
    private final Course prerequisite;

    // Basic course info
    @Column(unique = true, nullable = false)
    private final String name;
    private final String url; // Link to the SkillsBuild course
    private final String icon;
    @Lob // (allows large amounts of text to be stored)
    private final String description;

    @OneToMany(mappedBy = "course", orphanRemoval = true)
    private List<Question> quiz = null;


    // Base constructor (used if part of learning path)
    public Course(String name, String url, String description, String icon, LearningPath learningPath, Course prerequisite) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.icon = icon;
        this.learningPath = learningPath;
        this.prerequisite = prerequisite;
    }

    // Constructor if not part of learning path
    public Course(String name, String url, String description, String icon) {
        this(name, url, description, icon, null, null);
    }

    // Empty constructor for Spring
    public Course() {
        this("", "", "", "");
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public LearningPath getLearningPath() { return learningPath; }
    public Course getPrerequisite() { return prerequisite; }
    public List<Question> getQuiz() { return quiz; }
    public void setQuiz(List<Question> questions) { this.quiz = questions; }
}