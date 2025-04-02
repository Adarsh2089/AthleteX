package com.example.athletex.user;

public class userModel {
    public String userId, name, sport, goals, coach, experience, email;

    public userModel() {} // Empty constructor required for Firebase

    public userModel(String userId, String name, String sport, String goals, String coach, String experience, String email) {
        this.userId = userId;
        this.name = name;
        this.sport = sport;
        this.goals = goals;
        this.coach = coach;
        this.experience = experience;
        this.email = email;
    }
}
