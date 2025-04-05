package com.example.athletex.Coach;

public class CoachModel {
    public String coachId, name, associationName, experience, email;
    public CoachModel() {} // Empty constructor required for Firebase
    public CoachModel(String coachId, String name, String associationName, String experience, String email){
        this.coachId = coachId;
        this.name = name;
        this.associationName = associationName;
        this.experience = experience;
        this.email = email;
    }
}
