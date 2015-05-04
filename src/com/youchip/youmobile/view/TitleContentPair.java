package com.youchip.youmobile.view;

import java.io.Serializable;

public class TitleContentPair implements Serializable{
    
    private static final long serialVersionUID = 3134729799880023888L;
    private String title;
    private String content;

    public TitleContentPair(String title, String content){
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CharSequence getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
