/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devoxx.watson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

/**
 * Holds the system node of the Conversation context
 *
 * @author James Weaver
 */
@JsonRootName("system")
@JsonPropertyOrder({"dialog_stack", "dialog_turn_counter", "dialog_request_counter"})

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationContextSystem implements Serializable {
  @JsonProperty("dialog_stack")
  private String dialogStack;

  @JsonProperty("dialog_turn_counter")
  private String dialogTurnCounter;

  @JsonProperty("dialog_request_counter")
  private String dialogRequestCounter;

  public ConversationContextSystem() {
  }

  public ConversationContextSystem(String dialogStack, String dialogTurnCounter, String dialogRequestCounter) {
    this.dialogStack = dialogStack;
    this.dialogTurnCounter = dialogTurnCounter;
    this.dialogRequestCounter = dialogRequestCounter;
  }

  public String getDialogStack() {
    return dialogStack;
  }

  public void setDialogStack(String dialogStack) {
    this.dialogStack = dialogStack;
  }

  public String getDialogTurnCounter() {
    return dialogTurnCounter;
  }

  public void setDialogTurnCounter(String dialogTurnCounter) {
    this.dialogTurnCounter = dialogTurnCounter;
  }

  public String getDialogRequestCounter() {
    return dialogRequestCounter;
  }

  public void setDialogRequestCounter(String dialogRequestCounter) {
    this.dialogRequestCounter = dialogRequestCounter;
  }

  @Override
  public String toString() {
    return "ConversationContextSystem{" +
        "dialogStack='" + dialogStack + '\'' +
        ", dialogTurnCounter='" + dialogTurnCounter + '\'' +
        ", dialogRequestCounter='" + dialogRequestCounter + '\'' +
        '}';
  }
}
