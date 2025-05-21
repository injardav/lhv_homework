package com.example.lhv_homework.service;

public class PersonService {
    private boolean isValidName(String name) {
        return name != null &&
           !name.trim().isEmpty() &&
           name.matches("^[A-Za-z\\s'-]{2,50}$"); // letters, space, dash, apostrophe, 2–50 chars
    } 

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }
}
