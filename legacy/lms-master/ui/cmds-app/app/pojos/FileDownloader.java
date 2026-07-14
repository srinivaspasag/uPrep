package pojos;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pojos.DownloadInfo.DownloadState;

public class FileDownloader {

    public static String        userAgent          = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401";

    private DownloadInfo        info;
    public FilePartDownloader[] parts;
    public String               status;

    private boolean             restarting         = false;
    ExecutorService             EXECUTOR           = new ThreadPoolExecutor(1, 10, 60L,
                                                           TimeUnit.SECONDS,
                                                           new LinkedBlockingQueue<Runnable>());
    CompletionService           COMPLETION_SERVICE = new ExecutorCompletionService(EXECUTOR);

    public FileDownloader(DownloadInfo info) {

        this.info = info;
        status = "Starting...";
    }

    private void makeParts() {

        if (info.parts == null) {
            info.parts = new PartInfo[info.threads];

            long bytesPerThread = info.total / info.threads, firstByte = 0, lastByte = 0;

            for (int i = 0; i < info.threads; i++) {
                lastByte = firstByte + bytesPerThread;
                if (i == info.threads - 1) {
                    lastByte = info.total - 1;
                }

                info.parts[i] = new PartInfo(firstByte, lastByte);
                firstByte = lastByte + 1;
            }
        }
    }

    private int getContentLength(String urlString) {

        URL url;
        try {
            url = new URL(urlString);
            while (true) {
                // Check network connection first
                // if (!list.isConnected) {
                // if (setStatus("Waiting for a network connection..."))
                // return -1;
                // while (!list.isConnected);
                // }

                try {
                    // if (setStatus("Connecting to " + url.getHost()))
                    // return -1;

                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", userAgent);
                    connection.connect();
                    return connection.getContentLength();
                } catch (IOException e) {
                    // PrintError("Could not open connection", e);
                    // setStatus("Could not connect. Retrying...");
                }
            }
        } catch (MalformedURLException e) {
            return -1;
        }
    }

    public DownloadInfo startDownload(String... params) {

        // Check for canelled/finished downloads being loaded from database
        if (info.state == DownloadState.FINISHED || info.state == DownloadState.STOPPED) {
            return info;
        }

        File file = new File(info.fileDir, info.fileName);

        /**
         * Make a connection and partition the download. When recovering from the DB, parts are
         * already existing so we can skip this part TODO: we must check if the file has changed on
         * the server
         */
        if (info.parts == null) {
            // if (setStatus("Getting file size..."))
            // return info;
            info.total = getContentLength(info.url);
            if (info.total == -1) {
                System.out.println("File size not returned by server");
                return null;
            }

            // if (setStatus("Checking multi-part support..."))
            // return info;
            try {
                URL url = new URL(info.url);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Range", "bytes=0-127");
                connection.connect();
                int count = connection.getContentLength();
                if (count != 128) {
                    System.out.println("Multi-part downloads not supported by server");
                    return null;
                }
            } catch (IOException e1) {}

            System.out.println("File is " + info.total + " bytes");

            // if (setStatus("Creating " + info.fileName + "..."))
            // return info;

            try {
                createEmptyFile(file, info.total);
            } catch (IOException e) {
                System.out.println("Could not create output file. Check space and permissions. "
                        + e.getMessage());
                return null;
            }
        }

        // if (setStatus("Spawning threads..."))
        // return info;

        if (info.state == DownloadState.TOSTART || info.state == DownloadState.STARTED) {
            info.state = DownloadState.STARTED;
            info.lastStarted = System.currentTimeMillis();
        }

        makeParts();
        URL url;
        try {
            url = new URL(info.url);
        } catch (MalformedURLException e1) {
            return null;
        }
        parts = new FilePartDownloader[info.threads];
        for (int i = 0; i < info.threads; i++) {
            parts[i] = new FilePartDownloader(info.parts[i], info, url, file, i);
            COMPLETION_SERVICE.submit(parts[i], null);
        }

        while (info.downloaded < info.total
                && (info.state != DownloadState.FINISHED && info.state != DownloadState.STOPPED)) {
            if (info.state == DownloadState.PAUSED) {
                // if (setStatus("Download paused " + Formatter.formatFileSize(info.downloaded) +
                // "/"
                // + Formatter.formatFileSize(info.total)))
                // return info;
                while (info.state == DownloadState.PAUSED) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {}
                }
            } else {
                int downloaded = 0;
                for (int i = 0; i < info.threads; i++) {
                    downloaded += parts[i].info.downloaded;
                }
                info.downloaded = downloaded;
                // if (setStatus(Formatter.formatFileSize(list, info.downloaded) + "/"
                // + Formatter.formatFileSize(list, info.total) + " " + getFormattedSpeed()
                // + " " + getETA()))
                // return info;
            }
        }

        if (info.state == DownloadState.STARTED) {
            info.elapsedTime += System.currentTimeMillis() - info.lastStarted;
            info.lastStarted = 0;
            info.state = DownloadState.FINISHED;
        }

        // if (isCancelled())
        // setStatus("Canceled");
        EXECUTOR.shutdown();
        return info;
    }

    private static void createEmptyFile(File file, long size) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(size);
        } catch (IOException e) {
            throw e;
        } finally {
            raf.close();
        }
    }

    private static String formatDuration(long millis) {

        if (millis > 60000) {
            return String.format("%.1fmins", millis / 60000.0);
        } else {
            return String.format("%.1fs", millis / 1000.0);
        }
    }

    private long getTotalTime() {

        long total = info.elapsedTime;
        if (info.state == DownloadState.STARTED)
            total += System.currentTimeMillis() - info.lastStarted;
        return total;
    }

    public static void main(String[] args) {

        String url = "http://192.168.1.111:19019/viewer/stream/video/vid/4c2022e7-39cb-4e5f-904a-fdded87dadd0.vid.vid.orig.mp4";
        DownloadInfo dInfo = new DownloadInfo(url, "test.mp4", "/home/shankar/ssk", 5);
        FileDownloader downloader = new FileDownloader(dInfo);
        System.out.println("downlod started");
        DownloadInfo udInfo = downloader.startDownload();
        System.out.println("udInfo: " + udInfo);
        System.out.println("downlod completed");
    }
}
