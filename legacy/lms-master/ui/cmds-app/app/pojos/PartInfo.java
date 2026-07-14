package pojos;

public class PartInfo {

    public long         rowId, downloadRowId;
    public long         firstByte, lastByte, downloaded;
    public boolean      paused, cancelled;

    public long         total;                          // redundant, used to avoid calculations;

    public DownloadInfo info;

    /**
     * This is true if any data has been changed in this part
     */
    private boolean     dirty;

    public PartInfo(long rowId, long downloadRodId, long firstByte, long lastByte, long downloaded,
            boolean paused, boolean cancelled) {

        this.rowId = rowId;
        this.firstByte = firstByte;
        this.lastByte = lastByte;
        this.downloaded = downloaded;
        this.paused = paused;
        this.cancelled = cancelled;
        this.total = lastByte - firstByte;
        this.info = null;

        dirty = true;
    }

    public PartInfo(long firstByte, long lastByte) {

        this(Long.MAX_VALUE, Long.MAX_VALUE, firstByte, lastByte, 0, false, false);
    }

    public boolean isDirty() {

        return dirty;
    }

    public void setDirty(boolean dirty) {

        if (info != null)
            info.setDirty(true);
        this.dirty = dirty;
    }

    public static void main(String[] args) {

        partition(11, 5);
        // try {
        // setPassowrdToPdf();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    // public static void setPassowrdToPdf() throws Exception {
    //
    // FileOutputStream fio = new FileOutputStream(new File(
    // "/home/shankar/Desktop/Neer/test/hw3_enc.pdf"));
    // PdfReader pReader = new PdfReader(new FileInputStream(new File(
    // "/home/shankar/Desktop/Neer/test/hw3.pdf")));
    // PdfStamper pStamper = new PdfStamper(pReader, fio);
    // pStamper.setEncryption("kotuli123".getBytes(), "kotuli321".getBytes(),
    // ~(PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING),
    // PdfWriter.STANDARD_ENCRYPTION_128);
    // pStamper.close();
    // pReader.close();
    // fio.close();
    // }

    public static void partition(int n, int m) {

        partition(n, n - m + 1, m, "");
    }

    // This method will do recurrsive call and calculate all the partitions and print only //which
    // are of desired length
    public static void partition(int n, int maxVal, int m, String suffix) {

        if (n == 0 && suffix.length() == m) {
            System.out.println(suffix);
        } else {
            if (maxVal > 1) {
                partition(n, maxVal - 1, m, suffix);
            }
            if (maxVal <= n) {
                partition(n - maxVal, maxVal, m, maxVal + suffix);
            }
        }
    }

}
