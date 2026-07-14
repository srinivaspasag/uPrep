package com.lms.common.vedantu.entity.media;

public class MediaPresets {

    public String codecType;
    public String codecName;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("MediaPresets [codecType=");
        builder.append(codecType);
        builder.append(", codecName=");
        builder.append(codecName);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
