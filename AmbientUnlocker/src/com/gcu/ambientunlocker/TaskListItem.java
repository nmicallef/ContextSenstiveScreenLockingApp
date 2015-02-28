package com.gcu.ambientunlocker;


public class TaskListItem {
	TaskListItem( String d, String des, String tas, String det, int s) {
        this.day = d;
        this.description = des;
        this.tasks = tas;
        this.detailed = det;
        this.state = s;
    }

	String getDetailed() {
        return detailed;
    }

    void setDetailed(String s) {
        this.detailed =s;
    }
	
	
    String getTasks() {
        return tasks;
    }

    void setTasks(String s) {
        this.tasks =s;
    }

    void setDescription( String d ) {
    	this.description = d;
    }

    String getDescription() {
    	return this.description;
    }
    
    void setDay( String da ) {
    	this.day = da;
    }

    String getDay() {
    	return this.day;
    }
    
    void setState( int s ) {
    	this.state = s;
    }

    int getState() {
    	return this.state;
    }
    
    
    private String day;
    private String description;
    private String tasks;
    private String detailed;
    private int state;

}
