package com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools;

public enum ImageCompressionLevel {
    HIGH (0.80),
    MEDIUM(0.60),
    LOW(0.30);

    ImageCompressionLevel(Double scale) {
        this.scale = scale;
    }

    private Double scale;
    public Double getCompressionLevel(){
        return this.scale;
    }

}
