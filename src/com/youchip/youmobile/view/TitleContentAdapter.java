package com.youchip.youmobile.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class TitleContentAdapter extends ArrayAdapter<TitleContentPair> {
    
    private LayoutInflater inflater;
    private List<TitleContentPair> data;
    private int layoutResourceId;
    private int keyResourceId;
    private int valueResourceId;
    
    public TitleContentAdapter(Context context, int layoutResourceId, int keyResourceId, int valueResourceId, List<TitleContentPair> data){
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.keyResourceId = keyResourceId;
        this.valueResourceId = valueResourceId;
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        String rawBlockAsString = getItem(position);
        
        View row = convertView;
        KeyValueHolder holder = null;
        
        if (row == null){ //if the view is not cashed
            //inflate the common view from xml file
            row = this.inflater.inflate(layoutResourceId, parent, false);
            
            holder = new KeyValueHolder();
            holder.key = (TextView) row.findViewById(keyResourceId);
            holder.value = (TextView) row.findViewById(valueResourceId);
            
            row.setTag(holder);
        } else {
            holder = (KeyValueHolder) row.getTag();
        }
        
        
        holder.key.setText(data.get(position).getTitle());
        holder.value.setText(data.get(position).getContent());
        
        
        return row;
    }
    

//    @Override
//    public int getCount() {
//        return this.data.size();
//    }
//
//
//
//    @Override
//    public String getItem(int position) {
//        return this.data.get(position);
//    }
//
//    @Override
//    public int getViewTypeCount(){
//        return 1;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        if(position < getCount() && position >= 0 ){
//            return position;
//        } else {
//            return 0;
//        }
//    }
//    
    static class KeyValueHolder{
        TextView key;
        TextView value;
    }
    


}
