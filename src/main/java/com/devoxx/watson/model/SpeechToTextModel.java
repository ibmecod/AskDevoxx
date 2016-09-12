package com.devoxx.watson.model;

/**
 * Created by danieldeluca on 12/09/16.
 */
public class SpeechToTextModel {
    private String recognizedText;
    private Double resultConfidence;

    public SpeechToTextModel(String recognizedText, Double resultConfidence) {
        this.recognizedText = recognizedText;
        this.resultConfidence = resultConfidence;
    }

    public Double getResultConfidence() {
        return resultConfidence;
    }

    public void setResultConfidence(Double resultConfidence) {
        this.resultConfidence = resultConfidence;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }
}
