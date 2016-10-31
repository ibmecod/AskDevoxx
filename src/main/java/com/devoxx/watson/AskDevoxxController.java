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

import com.devoxx.watson.exception.FileException;
import com.devoxx.watson.model.*;
import com.google.gson.internal.LinkedTreeMap;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Realizes a REST service endpoint that, given an inquiry such as a question, returns a response from the Devoxx
 * corpus and conversation logic.
 *
 * @author James Weaver
 */
@RestController
@RequestMapping("/inquiry")
public class AskDevoxxController {

  @Autowired
  RetrieveAndRankController retrieveAndRankController;
  private Log log = LogFactory.getLog(getClass());

  private String workspaceId;
  private String conversationUsername;
  private String conversationPassword;
  private String conversationUrl;
  private String tmpFileStorageLocation;

  @Autowired
  public AskDevoxxController(AskDevoxxProperties askDevoxxProperties) {
    workspaceId = askDevoxxProperties.getWorkspaceId();
	//workspaceId = "cd3ceb1d-d327-426f-be47-a837aac2b0f8";
    conversationUsername = askDevoxxProperties.getConversationUsername();
    conversationPassword = askDevoxxProperties.getConversationPassword();
    //conversationUsername = "3096f919-5cc5-4e96-bc78-53b71fd92dbb";
    //conversationPassword = "a3MUXvnxHAMH";
    conversationUrl = askDevoxxProperties.getConversationUrl();
    tmpFileStorageLocation = askDevoxxProperties.getTmpFileStorageLocation();
  }

  @Autowired
  private WatsonSpeechTextController watsonController;


  private File storeTmpFile(MultipartFile file) throws FileException {
    // check if all form parameters are provided
    if (file == null) {
      throw new FileException("MultipartFile File Required");
    }
    // create our destination folder, if it not exists
    log.info("Storing: tmpFileStorageLocation:"+tmpFileStorageLocation+":");
    File uploadPath = new File(tmpFileStorageLocation);

    // Now do something with file...
    File tmpFile;
    try {
      tmpFile = File.createTempFile("devoxx-speechtext", UUID.randomUUID().toString()+".ogg",uploadPath);
      log.info(tmpFile.getAbsolutePath());
      FileOutputStream tempFileOutputStream = new FileOutputStream(tmpFile);
      FileCopyUtils.copy(file.getBytes(), tempFileOutputStream);
    } catch (IOException e) {
      log.fatal("Can not save file tmpFile",e);
      throw new FileException("Can not save file tmpFile",e);
    }
    return tmpFile;
  }
  private List<SpeechToTextModel> getSpeechToTextModels(MultipartFile file) throws FileException {
    File tmpFile = storeTmpFile(file);
    List<SpeechToTextModel> analysisResults = watsonController.speechToText(tmpFile);
    if (! tmpFile.delete()){
      log.fatal( "Can't Delete File:{0}:"+ tmpFile.getAbsolutePath()+":");
    }
    return analysisResults;
  }

  /**
   * Analyse the OGG audio file to determine the sentence and process the inquiry from the detected text
   * @param file the OGG audio file
   * @return response to the client
   */
  @RequestMapping(value = "/speech"
          , method = RequestMethod.POST
          , produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> speechToResponse(
            @RequestParam("file") MultipartFile file
          , @RequestHeader("conversationId") String conversationId
          ){
    List<SpeechToTextModel> analysisResults;
    try {
      analysisResults = getSpeechToTextModels(file);
    } catch (FileException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    DevoxxQuestion question = new DevoxxQuestion();
    ConversationContext conversationContext = null;
    question.setText(analysisResults.stream().findFirst().map(SpeechToTextModel::getRecognizedText).get());
    if (!conversationId.equalsIgnoreCase("0")){
      conversationContext = new ConversationContext();
      conversationContext.setConversationId(conversationId);
      ConversationContextSystem conversationContextSystem = new ConversationContextSystem();
      // TODO: Daniel: No Idead what are the following 3 properties for. But seems to be required
      conversationContextSystem.setDialogRequestCounter("1.0");
      conversationContextSystem.setDialogTurnCounter("1.0");
      conversationContextSystem.setDialogStack("[node_2_1472838558087]");
      conversationContext.setSystem(conversationContextSystem);
      question.setContext(conversationContext);
    }
    log.info("Speech To Question:"+question.toString()+":");
    return processInquiry(question);
  }


  protected ResponseEntity<Object> processInquiry (DevoxxQuestion devoxxQuestion) {
    InquiryResponseNear inquiryResponseNear = callDevoxxWatsonServices(devoxxQuestion);

    return Optional.ofNullable(inquiryResponseNear)
            .map(cr -> new ResponseEntity<>((Object)cr, HttpStatus.OK))
            .orElse(new ResponseEntity<>("AskDevoxx inquiry request unsuccessful", HttpStatus.INTERNAL_SERVER_ERROR));
  }

  /**
   * The inquiry REST endpoint.
   *
   * @param question
   * @return Response to the client
   */
  @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> inquiry(@RequestBody DevoxxQuestion question) {
    return processInquiry(question);
  }

  /**
   * Calls the appropriate Watson services to get an answer to the client's inquiry
   * @param question
   * @return An answer to the client's inquiry
   */
  private InquiryResponseNear callDevoxxWatsonServices(final DevoxxQuestion question) {

    InquiryResponseNear inquiryResponseNear = new InquiryResponseNear();
    List<RetrieveAndRankDocument> retrieveAndRankDocumentList = new ArrayList<>();

    Map<String, Object> requestContext = new LinkedTreeMap<>();
    Map<String, Object> requestContextSystem = new LinkedTreeMap<>();

    String dialogStack;
    String dialogTurnCounter;
    String dialogRequestCounter;
    String conversationId = null;

    final ConversationContext context = question.getContext();
    if (context != null) {

        dialogStack = context.getSystem().getDialogStack();
        dialogTurnCounter = context.getSystem().getDialogTurnCounter();
        dialogRequestCounter = context.getSystem().getDialogRequestCounter();
        conversationId = context.getConversationId();

        if (dialogStack != null && dialogStack.length() > 2) {
          List<String> dialogStackList = new ArrayList<>();
          if (dialogStack.charAt(0) == '[' && dialogStack.charAt(dialogStack.length() - 1) == ']') {
            dialogStack = dialogStack.substring(1, dialogStack.length() - 1);
          }
          dialogStackList.add(dialogStack);
          requestContextSystem.put("dialog_stack", dialogStackList);
        }

        if (dialogTurnCounter != null && dialogTurnCounter.length() > 0) {
          requestContextSystem.put("dialog_turn_counter", new Double(dialogTurnCounter));
        }

        if (dialogRequestCounter != null && dialogRequestCounter.length() > 0) {
          requestContextSystem.put("dialog_request_counter", new Double(dialogRequestCounter));
        }

        if (dialogStack != null && dialogStack.length() > 0 ||
                dialogTurnCounter != null && dialogTurnCounter.length() > 0 ||
                dialogRequestCounter != null && dialogRequestCounter.length() > 0) {
          requestContext.put("system", requestContextSystem);
        }

        if (conversationId != null && conversationId.length() > 0) {
          requestContext.put("conversation_id", conversationId);
        }
    }

    MessageRequest request = new MessageRequest.Builder()
            .inputText(question.getText())
            .context(conversationId != null && conversationId.length() > 1 ? requestContext : null)
        .build();

    ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
    if (conversationUsername != null || conversationPassword != null) {
      service.setUsernameAndPassword(conversationUsername, conversationPassword);
    }
    if (conversationUrl != null) {
      service.setEndPoint(conversationUrl);
    }

    // Use the previously configured service object to make a call to the conversational service
    MessageResponse response = service.message(workspaceId, request).execute();

    String responseText = response.getTextConcatenated(", ");

    // Determine if conversation's response is sufficient to answer the user's question or if we
    // should call the retrieve and rank service to obtain better answers
    if (response.getOutput().toString().contains("callRetrieveAndRank")) {
      log.info("Calling retrieve and rank with inquiry: " + question.getText());
      retrieveAndRankDocumentList = retrieveAndRankController.call(question.getText());

      // Truncate the extra JSON from the responseText that indicated that retrieve and rank should be called
      int indexOfLeftCurly = responseText.indexOf('{');
      if (indexOfLeftCurly > 1){
        responseText = responseText.substring(0, indexOfLeftCurly);
      }
    }


    inquiryResponseNear.setInquiryText(question.getText());
    inquiryResponseNear.setResponseText(responseText);
    inquiryResponseNear.setResources(retrieveAndRankDocumentList);

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
