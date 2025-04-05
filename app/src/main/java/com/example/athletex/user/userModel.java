package com.example.athletex.user;

public class userModel {
    public String userId, name, sport, goals, coach,coachNumber, experience, email;

    public userModel() {} // Empty constructor required for Firebase

    public userModel(String userId, String name, String sport, String goals, String coach,String coachNumber, String experience, String email) {
        this.userId = userId;
        this.name = name;
        this.sport = sport;
        this.goals = goals;
        this.coach = coach;
        this.coachNumber=coachNumber;
        this.experience = experience;
        this.email = email;
    }
}
