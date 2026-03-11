package com.github.cloudgyb.ai.knowledge.server.modules.commons.dto;

/**
 *
 * @author cloudgyb
 * @since 2026/3/11 16:56
 */
public class AntDesignSelectOption {
    private String value;
    private String label;
    private boolean disabled;
    private String title;
    private String key;

    public AntDesignSelectOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public AntDesignSelectOption(String value, String label, boolean disabled) {
        this.value = value;
        this.label = label;
        this.disabled = disabled;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
