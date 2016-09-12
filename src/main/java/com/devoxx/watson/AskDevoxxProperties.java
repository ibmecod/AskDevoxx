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

package com.devoxx.watson;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author James Weaver
 */
@ConfigurationProperties(prefix = "askdevoxx")
@Component
public class AskDevoxxProperties {

  // Watson conversation properties
  private String workspaceId;
  private String conversationUsername;
  private String conversationPassword;
  private String conversationUrl;

  // Retrieve and Rank properties
  private String retrieveUsername;
  private String retrievePassword;
  private String retrieveClusterName;
  private String retrieveCollectionName;
  private String retrieveUrl;

  public String getTmpFileStorageLocation() {
    return tmpFileStorageLocation;
  }

  public void setTmpFileStorageLocation(String tmpFileStorageLocation) {
    this.tmpFileStorageLocation = tmpFileStorageLocation;
  }

  private String tmpFileStorageLocation;

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public String getConversationUsername() {
    return conversationUsername;
  }

  public void setConversationUsername(String conversationUsername) {
    this.conversationUsername = conversationUsername;
  }

  public String getConversationPassword() {
    return conversationPassword;
  }

  public void setConversationPassword(String conversationPassword) {
    this.conversationPassword = conversationPassword;
  }

  public String getConversationUrl() {
    return conversationUrl;
  }

  public void setConversationUrl(String conversationUrl) {
    this.conversationUrl = conversationUrl;
  }

  public String getRetrieveUsername() {
    return retrieveUsername;
  }

  public void setRetrieveUsername(final String retrieveUsername) {
    this.retrieveUsername = retrieveUsername;
  }

  public String getRetrievePassword() {
    return retrievePassword;
  }

  public void setRetrievePassword(final String retrievePassword) {
    this.retrievePassword = retrievePassword;
  }

  public String getRetrieveClusterName() {
    return retrieveClusterName;
  }

  public void setRetrieveClusterName(final String retrieveClusterName) {
    this.retrieveClusterName = retrieveClusterName;
  }

  public String getRetrieveCollectionName() {
    return retrieveCollectionName;
  }

  public void setRetrieveCollectionName(final String retrieveCollectionName) {
    this.retrieveCollectionName = retrieveCollectionName;
  }

  public String getRetrieveUrl() {
    return retrieveUrl;
  }

  public void setRetrieveUrl(final String retrieveUrl) {
    this.retrieveUrl = retrieveUrl;
  }
}
