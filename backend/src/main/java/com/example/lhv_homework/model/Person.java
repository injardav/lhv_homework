package com.example.lhv_homework.model;

public class Person {
    private Long id;
    private String name;
    private String preprocessedName;

    public Person(Long id, String name, String preprocessedName) {
        this.id = id;
        this.name = name;
        this.preprocessedName = preprocessedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getPreprocessedName() {
        return preprocessedName;
    }
    
    public void setPreprocessedName(String preprocessedName) {
        this.preprocessedName = preprocessedName;
    }
}