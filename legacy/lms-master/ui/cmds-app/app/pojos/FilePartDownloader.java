package pojos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

public class FilePartDownloader implements Runnable {

    public PartInfo     info;
    public DownloadInfo downloadInfo;
    private URL         url;
    private File        file;
    int                 tId;

    public FilePartDownloader(PartInfo info, DownloadInfo downloadInfo, URL url, File file, int tId) {

        this.info = info;
        this.downloadInfo = downloadInfo;
        this.url = url;
        this.file = file;
        this.tId = tId;
        System.out.println("Thread[" + tId + "] # " + info.rowId + " firstByte = " + info.firstByte
                + " lastByte = " + info.lastByte);
    }

    public URLConnection makeConnection() {

        URLConnection connection = null;
        boolean connected = false;
        long startByte = info.firstByte + info.downloaded;

        // Keep retrying until connection is established
        while (!connected && !info.cancelled) {
            try {
                // Check network connection first
                // while (!list.isConnected);

                connection = url.openConnection();
                connection.setRequestProperty("User-Agent", FileDownloader.userAgent);
                connection.setRequestProperty("Range", "bytes=" + startByte + "-" + info.lastByte);
                connection.connect();
                connected = true;
            } catch (IOException e) {
                System.err.print("Connection to " + url + " failed. Retrying...");
            }
        }
        if (!info.cancelled)
            System.out.print("Thread[" + tId + "] Downloading file part "
                    + connection.getContentLength() + " bytes");
        return connection;
    }

    @Override
    public void run() {

        URLConnection connection;

        boolean finished = false;
        while (!finished && !info.cancelled) {
            try {
                connection = makeConnection();
                InputStream is = connection.getInputStream();
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(info.firstByte + info.downloaded);

                byte[] b = new byte[128];
                int n = 0;
                while ((n = is.read(b)) != -1 && !info.cancelled) {
                    if (info.paused) {
                        is.close();
                        raf.close();
                        while (info.paused) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {}
                        }
                    }
                    raf.write(b, 0, n);
                    info.downloaded += n;
                    info.setDirty(true);
                    downloadInfo.incrementBytes(n);
                }
                raf.close();
                is.close();

                finished = true;

                if (info.cancelled) {
                    System.out.print("Thread[" + tId + "] #" + info.rowId + " info.cancelled ");
                } else {
                    System.out.print("Thread[" + tId + "] #" + info.rowId + " info.downloaded "
                            + info.total + " bytes successfully");
                }
            } catch (IOException e) {
                System.err.print("Connection to " + url.getHost() + " broke. Reconnecting...");
            }
        }
        System.out.println("\nThread[" + tId + "] finished thread ");
    }
}
