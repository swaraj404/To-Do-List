package com.swaraj.todolist.dataModel;

import java.time.LocalDate;

public class ToDoItem {

    private String shortDescription;
    private String details;
    private LocalDate deadline;

    public ToDoItem(String shortDescription, String details, LocalDate deadline) {
        this.shortDescription = shortDescription;
        this.details = details;
        this.deadline = deadline;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

//    @Override
//    public String toString() {
//        return shortDescription;   //Its work is done in the cellfactory overloaded method.
//    }
}
