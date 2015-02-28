package com.gcu.ambientunlocker;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TaskListAdapter extends BaseAdapter {
	
	public TaskListAdapter(Context context,List<TaskListItem> f ) {
		inflater = LayoutInflater.from( context );
		this.context = context;
		this.files = f;
	}

	public int getCount() {
		return files.size();
	}

	public Object getItem(int arg0) {
		return files.get(arg0);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TaskListItem item = files.get(position);
        View v = null;
        if( convertView != null ){
        	v = convertView;
        }else{
        	v = inflater.inflate( R.layout.tasklist_row, parent, false);
        }
        //if (item.getState() == 0){
        	//v.setBackgroundColor(Color.WHITE);
        //}
        
        if (item.getState() == 1){
        	v.setBackgroundColor(Color.GREEN);
        }else if (item.getState() == 2){
        	v.setBackgroundColor(Color.RED);
        }else{
        	v.setBackgroundColor(Color.TRANSPARENT);
        }
        
        String day = item.getDay();
        TextView dayTV = (TextView)v.findViewById( R.id.tasklistday);
        dayTV.setText( day);
        String description = item.getDescription();
        TextView fileNameTV = (TextView)v.findViewById( R.id.tasklistdescription);
        fileNameTV.setText( description );
        String tasks = item.getTasks();
        TextView fileStatusTV = (TextView)v.findViewById( R.id.tasklisttasks);
        fileStatusTV.setText( tasks );
        return v;
	}
	
	private Context context;
    private List<TaskListItem> files;
	private LayoutInflater inflater;

}
