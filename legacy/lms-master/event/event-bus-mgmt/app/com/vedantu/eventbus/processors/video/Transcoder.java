package com.vedantu.eventbus.processors.video;

import java.io.File;

import com.vedantu.commons.enums.EntityType;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

public class Transcoder {

    public void transcode(EntityType type, String id, File inputFile, File outputFile) {

        // create a media reader
        IMediaReader mediaReader = ToolFactory.makeReader(inputFile.getAbsolutePath());

        // create a media writer
        IMediaWriter mediaWriter = ToolFactory.makeWriter(outputFile.getAbsolutePath());

        // add a writer to the reader, to create the output file
        mediaReader.addListener(mediaWriter);

        // create a media viewer with stats enabled
        IMediaViewer mediaViewer = ToolFactory.makeViewer(true);

        // add a viewer to the reader, to see the decoded media
        mediaReader.addListener(mediaViewer);

        // read and decode packets from the source file and
        // and dispatch decoded audio and video to the writer
        while (mediaReader.readPacket() == null);
        //
    }

    public static void main(String[] args) {

        File inputFile = new File(
                "/home/vikram/Documents/upload_doc_test_input/Transcoding/test.mp4");
        File outputFile = new File(
                "/home/vikram/Documents/upload_doc_test_input/Transcoding/test.webm");
        Transcoder coder = new Transcoder();
        coder.transcode(EntityType.VIDEO, "videoId", inputFile, outputFile);
    }
}
