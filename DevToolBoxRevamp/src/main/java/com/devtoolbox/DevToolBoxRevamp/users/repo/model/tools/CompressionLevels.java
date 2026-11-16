package com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools;

public enum CompressionLevels {
    LOW("/printer"),
    MEDIUM("/ebook"),
    HIGH("/screen");

    private final String compressedLevel;

    CompressionLevels(String compressedLevel) {
        this.compressedLevel = compressedLevel;
    }
    public String getCompressedLevel() {
        return compressedLevel;
    }

}
