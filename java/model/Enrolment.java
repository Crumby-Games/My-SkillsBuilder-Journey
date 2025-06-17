package com.example.group56.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/* User-specific course information. In this case, their progress and an abstraction of all their quiz attempts for that particular course.*/

@Entity
@Table(name = "enrolments")
public class Enrolment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private final Course course;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private final User user;

    // Tracking the course progress
    private String status = "NOT_STARTED";  // Can be "NOT_STARTED", "IN_PROGRESS", or "COMPLETED".
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    private LocalDateTime lastQuizAttemptTime;
    private int bestQuizAttemptScore;

    public Enrolment() {
        course = null;
        user = null;
    }

    public Enrolment(Course course, User user) {
        this.course = course;
        this.user = user;
    }

    public long getId() { return id; }
    public String getStatus() { return status; }
    public Course getCourse() {
        return course;
    }
    public User getUser() {
        return user;
    }
    public void setLastQuizAttemptTime(LocalDateTime lastQuizAttemptTime) { this.lastQuizAttemptTime = lastQuizAttemptTime; }
    public void setBestQuizAttemptScore(int bestQuizAttemptScore) { this.bestQuizAttemptScore = bestQuizAttemptScore; }
    public LocalDateTime getLastQuizAttemptTime() { return lastQuizAttemptTime; }
    public int getBestQuizAttemptScore() { return bestQuizAttemptScore; }


    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        updateStatus();
    }

    public LocalDateTime getCompletionTime() { return completionTime; }
    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
        updateStatus();
    }

    void updateStatus() {
        if (startTime == null) {
            status = "NOT_STARTED";
        } else if (completionTime == null) {
            status = "IN_PROGRESS";
        } else {
            status = "COMPLETED";
        }
    }


}
