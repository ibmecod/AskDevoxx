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

import com.devoxx.watson.model.ConversationContext;
import com.devoxx.watson.model.ConversationContextSystem;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Realizes a REST service endpoint that, given an inquiry such as a question, returns a response from the Devoxx
 * corpus and conversation logic.
 *
 * @author James Weaver
 */
@RestController
@RequestMapping("/inquiry")
public class AskDevoxxController {
  private Log log = LogFactory.getLog(getClass());

  private final AskDevoxxProperties askDevoxxProperties;

  @Autowired
  public AskDevoxxController(AskDevoxxProperties askDevoxxProperties) {
    this.askDevoxxProperties = askDevoxxProperties;
  }

  @Autowired
  RetrieveAndRankController retrieveAndRankController;

  /**
   * Example endpoint usage is inquiry?text=Java modularity&context=abc123
   *
   * @param inquiryText
   * @param conversationId
   * @return Response to the client
   */
  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> inquiry(@RequestParam(value = "text") String inquiryText,
                                        @RequestParam(value = "conversation_id", defaultValue="") String conversationId,
                                        @RequestParam(value = "dialog_stack", defaultValue="") String dialogStack,
                                        @RequestParam(value = "dialog_turn_counter", defaultValue="") String dialogTurnCounter,
                                        @RequestParam(value = "dialog_request_counter", defaultValue="") String dialogRequestCounter) {

    log.info("Received request, inquiryText: " + inquiryText + ", conversationId: " + conversationId +
        ", dialogStack: " + dialogStack + ", dialogTurnCounter: " + dialogTurnCounter + ", dialogRequestCounter: " + dialogRequestCounter);

    InquiryResponseNear inquiryResponseNear = callDevoxxWatsonServices(inquiryText, conversationId,
        dialogStack, dialogTurnCounter, dialogRequestCounter);

    return Optional.ofNullable(inquiryResponseNear)
        .map(cr -> new ResponseEntity<>((Object)cr, HttpStatus.OK))
        .orElse(new ResponseEntity<>("AskDevoxx inquiry request unsuccessful", HttpStatus.INTERNAL_SERVER_ERROR));
  }

  /**
   * Calls the appropriate Watson services to get an answer to the client's inquiry
   * @param inquiryText
   * @return An answer to the client's inquiry
   */
  private InquiryResponseNear callDevoxxWatsonServices(String inquiryText, String conversationId, String dialogStack,
                                                       String dialogTurnCounter, String dialogRequestCounter) {
    String workspaceId = askDevoxxProperties.getWorkspaceId();
    String conversationUsername = askDevoxxProperties.getConversationUsername();
    String conversationPassword = askDevoxxProperties.getConversationPassword();
    String conversationUrl = askDevoxxProperties.getConversationUrl();

    InquiryResponseNear inquiryResponseNear = new InquiryResponseNear();

    Map<String, Object> requestContext = new LinkedTreeMap<>();
    Map<String, Object> requestContextSystem = new LinkedTreeMap<>();

    if (dialogStack.length() > 2) {
      List<String> dialogStackList = new ArrayList<>();
      if (dialogStack.charAt(0) == '[' && dialogStack.charAt(dialogStack.length() - 1) == ']') {
        dialogStack = dialogStack.substring(1, dialogStack.length() - 1);
      }
      dialogStackList.add(dialogStack);
      requestContextSystem.put("dialog_stack", dialogStackList);
    }

    if (dialogTurnCounter.length() > 0) {
      requestContextSystem.put("dialog_turn_counter", new Double(dialogTurnCounter));
    }
    if (dialogRequestCounter.length() > 0) {
      requestContextSystem.put("dialog_request_counter", new Double(dialogRequestCounter));
    }

    if (dialogStack.length() > 0 || dialogTurnCounter.length() > 0 || dialogRequestCounter.length() > 0) {
      requestContext.put("system", requestContextSystem);
    }

    if (conversationId.length() > 0) {
      requestContext.put("conversation_id", conversationId);
    }

    MessageRequest request = new MessageRequest.Builder()
        .inputText(inquiryText)
        .context(conversationId.length() > 1 ? requestContext : null)
        .build();

    ConversationService service =
        new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
    if (conversationUsername != null || conversationPassword != null) {
      service.setUsernameAndPassword(conversationUsername, conversationPassword);
    }
    if (conversationUrl != null) {
      service.setEndPoint(conversationUrl);
    }

    // Use the previously configured service object to make a call to the conversational service
    MessageResponse response = service.message(workspaceId, request).execute();

    // Determine if conversation's response is sufficient to answer the user's question or if we
    // should call the retrieve and rank service to obtain better answers
    if (response.getOutput().toString().contains("callRetrieveAndRank")) {
      log.info("Calling retrieve and rank with inquiry: " + inquiryText);
      JsonObject retrieveAndRankResponseJson = retrieveAndRankController.call(inquiryText);
    }


    inquiryResponseNear.setInquiryText(inquiryText);

    inquiryResponseNear.setResponseText(response.getTextConcatenated(", "));

    Map<String, Object> responseContext = response.getContext();
    Map<String, Object> responseContextSystem = (Map)responseContext.get("system");
    ConversationContextSystem conversationContextSystem = null;
    if (responseContextSystem != null) {
      conversationContextSystem = new ConversationContextSystem(
          responseContextSystem.get("dialog_stack") != null ? responseContextSystem.get("dialog_stack").toString() : "",
          responseContextSystem.get("dialog_turn_counter") != null ? responseContextSystem.get("dialog_turn_counter").toString() : "",
          responseContextSystem.get("dialog_request_counter") != null ? responseContextSystem.get("dialog_request_counter").toString() : "");
    }
    ConversationContext conversationContext = new ConversationContext(
        responseContext.get("conversation_id") != null ? responseContext.get("conversation_id").toString() : "",
        conversationContextSystem);
    inquiryResponseNear.setContext(conversationContext);
    log.info("response.toString() " + response.toString());

    return inquiryResponseNear;
  }
}
