package com.codealpha.flashcard;

public class Flashcard {
    private String id;
    private String category;
    private String question;
    private String answer;

    public Flashcard(String id, String category, String question, String answer) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}
