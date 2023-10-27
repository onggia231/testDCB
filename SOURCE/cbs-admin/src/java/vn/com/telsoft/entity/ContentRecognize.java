package vn.com.telsoft.entity;

import java.io.Serializable;

public class ContentRecognize implements Serializable {
    private static final long serialVersionUID = -7509209252874245528L;

    private Integer contentRecognizeId;
    private Integer contentId;
    private String matchingType;
    private String keyWord;
    private Integer caseSensitive;

    public ContentRecognize(ContentRecognize ett) {
        this.contentRecognizeId = ett.getContentRecognizeId();
        this.contentId = ett.getContentId();
        this.matchingType = ett.getMatchingType();
        this.keyWord = ett.getKeyWord();
        this.caseSensitive = ett.getCaseSensitive();
    }

    public ContentRecognize() {
    }

    public Integer getContentRecognizeId() {
        return contentRecognizeId;
    }

    public void setContentRecognizeId(Integer contentRecognizeId) {
        this.contentRecognizeId = contentRecognizeId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getMatchingType() {
        return matchingType;
    }

    public void setMatchingType(String matchingType) {
        this.matchingType = matchingType;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public Integer getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(Integer caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
}
