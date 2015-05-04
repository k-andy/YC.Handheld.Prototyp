package com.youchip.youmobile.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedKeyValueList {
    
    private Map<String, List<TitleContentPair>> groupedList = new LinkedHashMap<>();
    private List<String> idLink = new ArrayList<>();
    
    
    public GroupedKeyValueList() {
        super();
    }
    
    public void addGroup(String group){
        if ( !this.groupedList.containsKey(group)) {
            this.groupedList.put(group, new ArrayList<TitleContentPair>());
            this.idLink.add(group);
        }
    }
    
    public void addElement(String group, String title, String content){
         addGroup(group);
         this.groupedList.get(group).add(new TitleContentPair(title, content));
    }
    
    public List<TitleContentPair> getGroup(int location){
        return this.groupedList.get(idLink.get(location));
    }
    
    public String getGroupName(int location){
        return idLink.get(location);
    }
    
    public int size(){
        return this.groupedList.size();
    }
    
    public void clear(){
        groupedList.clear();
        idLink.clear();
    }
    
}
