package com.ntnt.httpserver.models;

public class ScheduleEntity {
    private long id;
    private String code;
    private String name;
    private float credit;
    private boolean clc;
    private String lecturer;
    private String schedule;
    private String studyingWeek;

    public ScheduleEntity() {
    }

    public ScheduleEntity(long id, String code, String name, float credit, boolean clc, String lecturer, String schedule, String studyingWeek) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.credit = credit;
        this.clc = clc;
        this.lecturer = lecturer;
        this.schedule = schedule;
        this.studyingWeek = studyingWeek;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public boolean isClc() {
        return clc;
    }

    public void setClc(boolean clc) {
        this.clc = clc;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getStudyingWeek() {
        return studyingWeek;
    }

    public void setStudyingWeek(String studyingWeek) {
        this.studyingWeek = studyingWeek;
    }
}
