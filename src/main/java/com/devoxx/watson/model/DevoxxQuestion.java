package com.devoxx.watson.model;

/**
 * @author Stephan Janssen
 */
public class DevoxxQuestion {

    private String text;
    private String dialogStack;
    private String dialogTurnCounter;
    private String dialogRequestCounter;
    private String conversationId;

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getDialogStack() {
        return dialogStack;
    }

    public void setDialogStack(final String dialogStack) {
        this.dialogStack = dialogStack;
    }

    public String getDialogTurnCounter() {
        return dialogTurnCounter;
    }

    public void setDialogTurnCounter(final String dialogTurnCounter) {
        this.dialogTurnCounter = dialogTurnCounter;
    }

    public String getDialogRequestCounter() {
        return dialogRequestCounter;
    }

    public void setDialogRequestCounter(final String dialogRequestCounter) {
        this.dialogRequestCounter = dialogRequestCounter;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(final String conversationId) {
        this.conversationId = conversationId;
    }
}
