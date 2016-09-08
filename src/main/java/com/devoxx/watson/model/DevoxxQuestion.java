package com.devoxx.watson.model;

/**
 * @author Stephan Janssen
 */
public class DevoxxQuestion {

    private String text;

    ConversationContext context;


    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public ConversationContext getContext() {
        return context;
    }

    public void setContext(final ConversationContext context) {
        this.context = context;
    }
}
