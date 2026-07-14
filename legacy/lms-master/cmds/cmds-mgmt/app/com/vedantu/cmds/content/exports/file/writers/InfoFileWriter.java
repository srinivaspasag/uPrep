package com.vedantu.cmds.content.exports.file.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.ExportRecordDAO;
import com.vedantu.cmds.pojos.export.ExportRecordInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class InfoFileWriter extends BufferedWriter {

    private static final ALogger LOGGER     = Logger.of(InfoFileWriter.class);
    JSONObject                   jsonObject = new JSONObject();

    public InfoFileWriter(File file) throws IOException {

        super(new FileWriter(file));
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public void write(String exportId, long totalContent, long totalRecords,
            long totalUncompressedSize) throws ExportException {

        try {


            ExportRecordInfo recordInfo = ExportRecordDAO.INSTANCE.getBasicInfo(exportId);
            Map<String, Object> contentJSONmap  = ObjectMapperUtils.convertValue(recordInfo, Map.class);
            JSONObject jsonInfo = new JSONObject(contentJSONmap);
            jsonObject.put("recordInfo", jsonInfo);

            jsonObject.put("total", totalContent);
            jsonObject.put("totalRecords", totalRecords);
            jsonObject.put("totalSize", totalUncompressedSize);
            LOGGER.debug(" Publish " + jsonObject.toString());
            this.write(jsonObject.toString());
            this.newLine();
            this.flush();

        } catch (IOException e) {
            LOGGER.error("Can not write file ", e);
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e.getMessage());
        } catch (JSONException e) {
            LOGGER.error("Can not write file ", e);
            throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e.getMessage());
        } finally {
            try {
                LOGGER.debug("Closing info file  ");

                this.close();

            } catch (IOException e) {
                LOGGER.error("Failed to close info file  ", e);
                throw new ExportException(VedantuErrorCode.FAILED_TO_EXPORT, e.getMessage());
            }
        }
    }

}
