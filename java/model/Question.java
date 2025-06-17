package com.example.group56.model;

import jakarta.persistence.*;

/* Collection of information for each question in a quiz. Since there is no quiz entity, each question stores which course it relates to. */

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private final String prompt;
    private final byte answer;

    // Stored as a string so that it can be stored in the database without creating more entities
    private String options;

    public Question() {
        this("", new String[]{}, (byte)0);
    }

    public Question(String prompt, String[] options, int answer) {
        this.prompt = prompt;
        this.answer = (byte)answer;
        setOptions(options);
    }

    public long getId() { return id; }
    public String getPrompt() { return prompt; }
    public byte getAnswer() { return answer; }
    public void setCourse(Course course) { this.course = course; }

    // Always preferred in array format, so split when getting.
    public String[] getOptions() {
        return options.split(",");
    }

    private void setOptions(String[] options) {
        this.options = String.join(",", options);
    }
}
