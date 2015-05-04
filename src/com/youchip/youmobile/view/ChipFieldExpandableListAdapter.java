package com.youchip.youmobile.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ChipFieldExpandableListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater inflater;
    private final GroupedKeyValueList groupData;

    private final int groupCollapsedView;
    private final int groupExpandedView;
    private final int childView;

    private final int groupTitleResourceId;
    private final int itemTitleResourceId;
    private final int itemContentResourceId;

    public ChipFieldExpandableListAdapter(Context context, int groupView,
            int childView, int groupTitleResourceId, int itemTitleResourceId,
            int itemContentResourceId, GroupedKeyValueList data) {
        this(context, groupView, -1, childView, groupTitleResourceId,
                itemTitleResourceId, itemContentResourceId, data);
    }

    public ChipFieldExpandableListAdapter(Context context,
            int groupExpandedView, int groupCollapsedView, int childView,
            int groupTitleResourceId, int itemTitleResourceId,
            int itemContentResourceId, GroupedKeyValueList data) {

        this.inflater = LayoutInflater.from(context);

        this.groupExpandedView = groupExpandedView;
        this.groupCollapsedView = groupCollapsedView;
        this.childView = childView;

        this.groupTitleResourceId = groupTitleResourceId;
        this.itemTitleResourceId = itemTitleResourceId;
        this.itemContentResourceId = itemContentResourceId;
        this.groupData = data;
    }

    @Override
    public TitleContentPair getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ((Integer) childPosition).longValue();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        View row = convertView;
        KeyValueHolder holder = null;

        if (row == null) { // if the view is not cashed
            // inflate the common view from xml file
            row = this.inflater.inflate(getChildView(), parent, false);

            holder = new KeyValueHolder();
            holder.title = (TextView) row.findViewById(getTitleResourceId());
            holder.content = (TextView) row
                    .findViewById(getContentResourceId());

            row.setTag(holder);
        } else {
            holder = (KeyValueHolder) row.getTag();
        }

        holder.title.setText(getChild(groupPosition, childPosition).getTitle());
        holder.content.setText(getChild(groupPosition, childPosition)
                .getContent());

        return row;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.groupData.getGroup(groupPosition).size();
    }

    @Override
    public List<TitleContentPair> getGroup(int groupPosition) {
        return this.groupData.getGroup(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.groupData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return ((Integer) groupPosition).longValue();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {

        if (convertView == null
                || convertView.getId() != (isExpanded ? getGroupExpandedView()
                        : getGroupExpandedView())) {

            if ((this.groupCollapsedView != -1) && !isExpanded) {
                convertView = inflater.inflate(getGroupCollapsedView(), parent,
                        false);
            } else {
                convertView = inflater.inflate(getGroupExpandedView(), parent,
                        false);
            }

            convertView.setTag(getGroup(groupPosition));

        } else {
            // do nothing, we're good to go, nothing has changed.
        }

        String headerTitle = (String) this.groupData
                .getGroupName(groupPosition);
        TextView groupTitle = (TextView) convertView
                .findViewById(getGroupTitleResourceId());
        groupTitle.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    protected LayoutInflater getInflater() {
        return inflater;
    }

    protected Integer getGroupExpandedView() {
        return groupExpandedView;
    }

    protected Integer getGroupCollapsedView() {
        return groupCollapsedView;
    }

    protected Integer getChildView() {
        return childView;
    }

    protected int getGroupTitleResourceId() {
        return groupTitleResourceId;
    }

    protected int getTitleResourceId() {
        return itemTitleResourceId;
    }

    protected int getContentResourceId() {
        return itemContentResourceId;
    }

    protected static class KeyValueHolder {
        TextView title;
        TextView content;
    }
    
    public void addGroup(String group){
        groupData.addGroup(group);
        notifyDataSetChanged();
    }
    
    public void addElement(String group, String title, String content){
        groupData.addElement(group, title, content);
        notifyDataSetChanged();
    }

    
    public String getGroupName(int location){
        return groupData.getGroupName(location);
    }
    
    public int size(){
        return groupData.size();
    }
    
    public void clear(){
        groupData.clear();
        notifyDataSetChanged();
    }
}
