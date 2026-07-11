package com.vedantu.cmds.content.exports.file.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.models.ContentSearchDetails;
import com.vedantu.content.pojos.responses.GetContentLinkRes;

public class ExportFileWriter extends BufferedWriter {

    public static final String      EXPORT_JSON_CONSTRUCT_LIST = "list";
    public static final String      EXPORT_JSON_TOTAL_HITS     = "totalHits";
    private long                    totalRecords               = 0;

    private List<GetContentLinkRes> accumulatedResource        = new ArrayList<GetContentLinkRes>();

    public ExportFileWriter(File file) throws IOException {

        super(new FileWriter(file));
    }

    public void accumulate(GetContentLinkRes resource) {

        accumulatedResource.add(resource);
    }

    public void clear() {

        accumulatedResource.clear();
    }

    public long writeContent(GetContentLinkRes resource) throws ExportException {

        long writeSize = 0;
        accumulatedResource.add(resource);

        try {
            for (GetContentLinkRes resourceInc : accumulatedResource) {
                JSONObject linkJson = new JSONObject(resourceInc);
                if (resourceInc.content instanceof ContentSearchDetails) {
                    ContentSearchDetails unifiedContentSearchDetails = (ContentSearchDetails) resourceInc.content;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentJSONmap = ObjectMapperUtils.convertValue(
                            unifiedContentSearchDetails, Map.class);
                    JSONObject contentJSON = new JSONObject(contentJSONmap);
                    linkJson.put("content", contentJSON);
                }
                if (resourceInc.target instanceof SrcEntity) {
                    SrcEntity target = (SrcEntity) resourceInc.target;
                    JSONObject targetJSON = target.toJSON();
                    linkJson.put("target", targetJSON);
                }
                if (resource.encLevel != null) {
                    linkJson.put("encLevel", resource.encLevel.name());
                }
                this.write(linkJson.toString());
                this.newLine();
            }
            this.flush();
            totalRecords += accumulatedResource.size();
            accumulatedResource.clear();
            return writeSize;
        } catch (JSONException e) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);
        }

    }

    public void finish() throws ExportException {

        try {
            this.close();
        } catch (IOException e) {
            throw new ExportException(VedantuErrorCode.STORAGE_EXCEPTION);

        }

    }

    public long getTotalRecords() {

        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {

        this.totalRecords = totalRecords;
    }

}
