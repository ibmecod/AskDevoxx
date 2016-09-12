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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DevoxxQuestion{");
        sb.append("text='").append(text).append('\'');
        sb.append(", context=").append(context);
        sb.append('}');
        return sb.toString();
    }
}
