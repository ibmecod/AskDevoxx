package com.devoxx.watson;

import com.devoxx.watson.model.SpeechToTextModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danieldeluca on 12/09/16.
 */
@Component
public class WatsonSpeechTextController {

    private Log log = LogFactory.getLog(getClass());
    private SpeechToText speechToText;

    @Resource
    private Environment env;

    public SpeechToText speechToText() {
        if (speechToText == null){
            speechToText = new SpeechToText();
            String username = env.getProperty("speech.username");
            String password = env.getProperty("speech.password");
            speechToText.setUsernameAndPassword(username, password);
        }
        return speechToText;
    }

    /**
     * Process the provided Audio File
     * @param tempFile temp audio file to be processed
     * @return list of detected SpeechToTextModel
     */
    List<SpeechToTextModel> speechToText(File tempFile) {
        return processAudioFile(tempFile);
    }

    /**
     * Process the Audio File and return the detected texts
     * @param audioFile the audio file to process
     * @return List of SpeechToTextModel detected
     */
    protected List<SpeechToTextModel> processAudioFile (final File audioFile){
        List<SpeechToTextModel> speechToTextModelList = new ArrayList<>();
        SpeechResults recognitionResult = speechToText().recognize(audioFile).execute();
        recognitionResult.getResults()
                .stream()
                .forEach(
                        result -> result.getAlternatives()
                                .stream()
                                .peek(
                                        alternative -> log.info(
                                                "processAudioFile only : Transcript:"+alternative.getTranscript()+":Confidence:"+alternative.getConfidence().toString()+":"
                                        )
                                )
                                .forEach(
                                        speechAlternative -> speechToTextModelList.add(new SpeechToTextModel(speechAlternative.getTranscript(), speechAlternative.getConfidence()))
                                )
                );
        log.info("processAudioFile:"+speechToTextModelList.toString()+":");
        return speechToTextModelList;
    }

}
