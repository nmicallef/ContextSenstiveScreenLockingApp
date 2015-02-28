package com.gcu.ambientunlocker;

import weka.classifiers.trees.J48;
import android.app.Application;

public class CustomClassifiers extends Application{
	
	private boolean currentstate=false;
	private String currentcontext=null;
	
	private String method1=null;
	private String method2=null;
	
	private J48 j48home=null;
	private J48 j48work=null;
	private J48 j48other=null;
	private J48 j48transition=null;
	
	public J48 getHomeClassifier(){
		return j48home;
	}
	public J48 getWorkClassifier(){
		return j48work;
	}
	public J48 getOtherClassifier(){
		return j48other;
	}
	public J48 getTransitionClassifier(){
		return j48transition;
	}
	
	public void setHomeClassifier(J48 x){
		j48home =x;
	}
	
	public void setWorkClassifier(J48 x){
		j48work =x;
	}
	
	public void setOtherClassifier(J48 x){
		j48other =x;
	}
	public void setTransitionClassifier(J48 x){
		j48transition =x;
	}
	
	public boolean getCurrentState(){
		return currentstate;
	}
	
	public void setCurrentState(boolean x){
		currentstate = x;
	}
	
	public String getCurrentContext(){
		return currentcontext;
	}
	
	public void setCurrentContext(String x){
		currentcontext = x;
	}
	
	public String getMethod1(){
		return method1;
	}
	
	public void setMethod1(String x){
		method1 = x;
	}
	
	public String getMethod2(){
		return method2;
	}
	
	public void setMethod2(String x){
		method2 = x;
	}
	
	public boolean checkState(){
		boolean flag = false;
		if ((j48home != null) && (j48work != null) && (j48other != null) && (j48transition != null)){
			flag = true;
		}
		/*if ((j48home != null)  && (j48other != null) && (j48transition != null)){
			flag = true;
		}*/
		return flag;
	}
}
