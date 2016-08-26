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
import java.util.List;

/**
 * Holds a Conversation context
 *
 * @author James Weaver
 */
@JsonRootName("context")
@JsonPropertyOrder({"conversation_id", "system"})

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationContext implements Serializable {
  @JsonProperty("conversation_id")
  private String conversationId;

  @JsonProperty("system")
  private ConversationContextSystem system;

  public ConversationContext() {
  }

  public ConversationContext(String conversationId, ConversationContextSystem system) {
    this.conversationId = conversationId;
    this.system = system;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public ConversationContextSystem getSystem() {
    return system;
  }

  public void setSystem(ConversationContextSystem system) {
    this.system = system;
  }

  @Override
  public String toString() {
    return "ConversationContext{" +
        "conversationId='" + conversationId + '\'' +
        ", system=" + system +
        '}';
  }
}
