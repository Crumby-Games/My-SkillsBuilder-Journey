package com.example.group56.model;

import jakarta.persistence.*;

import java.util.List;

/* Collection of learning path data for ease-of-access. Name is the only unique data. */

@Entity
@Table(name = "learning_paths")
public class LearningPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "learningPath")
    List<Course> courses;

    public LearningPath() {}

    public LearningPath(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Course> getCourses() { return courses; }
}
