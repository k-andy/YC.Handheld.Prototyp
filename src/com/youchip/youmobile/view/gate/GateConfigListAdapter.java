package com.youchip.youmobile.view.gate;

import java.text.SimpleDateFormat;
import java.util.List;

import com.youchip.youmobile.model.gate.VisitorRole;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GateConfigListAdapter extends BaseAdapter{
    
    private final LayoutInflater inflater;
    private final List<VisitorRole> itemList;
    private final int childView;
    private final int roleNameResourceId;
    private final int startTimeResourceId;
    private final int endTimeResourceId;

    private boolean isCancelationMode = false;
    private final SimpleDateFormat quickDate= new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
    
    public GateConfigListAdapter(Context context, int childView, int roleNameResourceId, int startTimeResourceId, int endTimeResourceId, List<VisitorRole> itemList){
        super();
        this.inflater = LayoutInflater.from(context);
        this.itemList = itemList;
        this.childView = childView;
        this.roleNameResourceId = roleNameResourceId;
        this.startTimeResourceId = startTimeResourceId;
        this.endTimeResourceId = endTimeResourceId;

    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View element = convertView;
        GateItemHolder holder = null;
        
        if (element == null){ //if the view is not cashed
            //inflate the common view from xml file
            element = this.inflater.inflate(getChildView(), parent, false);
            holder = new GateItemHolder();
            holder.roleName             = (TextView) element.findViewById(roleNameResourceId);
            holder.roleValidStartTime   = (TextView) element.findViewById(startTimeResourceId);
            holder.roleValidEndTime     = (TextView) element.findViewById(endTimeResourceId);
            
            element.setTag(holder);
        } else {
            holder = (GateItemHolder) element.getTag();
        }
        
        VisitorRole gateItem = (VisitorRole) getItem(position);
        
        
        holder.roleName.setText(gateItem.getRoleName());
        holder.roleValidStartTime.setText(quickDate.format(gateItem.getValidTimeStart()));
        holder.roleValidEndTime.setText(quickDate.format(gateItem.getValidTimeStop()));
        
        return element;
    }
    
    protected Integer getChildView() {
        return childView;
    }
    
    
    static class GateItemHolder{
        TextView roleName;
        TextView roleValidStartTime;
        TextView roleValidEndTime;
    }
    
    public boolean isCancelationMode(){
        return isCancelationMode;
    }
    
    public void setCancelationMode(boolean mode){
        this.isCancelationMode = mode;
    }

}
