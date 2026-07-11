package actors;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import constants.ConstantGlobal;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;
import utils.CommonUtils;
import utils.ElasticSearchUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Raghu Teja on 28-06-2017.
 */
@Singleton
public class ProcessLogs extends UntypedAbstractActor {


    public static Props getProps(Config config) {
        return Props.create(ProcessLogs.class, config);
    }

    private final Config config;

    @Inject
    public ProcessLogs(Config config) {
        this.config = config;
    }

    private static final Logger.ALogger LOGGER = Logger.of("ProcessLogs");

    private void unzipFile(File file){
        BufferedReader reader = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            String esServer = config.getString(ConstantGlobal.ES_SERVER_ADDRESS);
            int port = config.getInt(ConstantGlobal.ES_SERVER_PORT);
            InetAddress address = InetAddress.getByName(esServer);
            Settings settings = Settings.builder()
                    .put(ConstantGlobal.CLUSTER_NAME, config.getString(ConstantGlobal.CLUSTER_NAME))
                    .put(ConstantGlobal.NODE_NAME, config.getString(ConstantGlobal.NODE_NAME)).build();

            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(address, port));

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            while(entries.hasMoreElements()) {
                try {
                    entry = entries.nextElement();

                    reader = new BufferedReader(
                            new InputStreamReader(zipFile.getInputStream(entry), "UTF-8"));
                    String index = entry.getName();
                    if(ElasticSearchUtil.indexExists(index, client)) {
                        LOGGER.debug("Index already exists");
                    }
                    else {
                        ElasticSearchUtil.createIndex(index, client);
                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            new JSONObject(line);
                            bulkRequest.add(client.prepareIndex(index, index)
                                    .setSource(line, XContentType.JSON));
                        }
                        catch (JSONException e) {
                            LOGGER.error("JSON exception for line::::" + line, e);
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Error reading file", e);
                }
                finally {
                    CommonUtils.close(reader);
                }
            }
            zipFile.close();
            if(bulkRequest.numberOfActions() <= 0) {
                LOGGER.error("No actions, deleting file");
                LOGGER.debug("Deleted file " + file.getName() + "?: " + file.delete());
                return;
            }
            String lineFeed = "  ::::  ";
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse itemResponse : bulkResponse) {
                    BulkItemResponse.Failure failure = itemResponse.getFailure();
                    if (failure != null) {
                        String error = "Elasticsearch failure: " + failure.getMessage() +
                                lineFeed +
                                "On index" + failure.getIndex() +
                                lineFeed +
                                "Reason: " + failure.getCause();
                        LOGGER.error(error);
                    }
                }
            }
            else {
                LOGGER.info(String.format("Indexing of %s complete.", file.getName()));
                String backup = config.getString(ConstantGlobal.LOGS_BACKUP);
                LOGGER.info("Backing up to " + backup);
                if(StringUtils.isNotEmpty(backup)) {
                    File backupDir = new File(backup);
                    if(!backupDir.exists()) {
                        boolean dirsCreated = backupDir.mkdirs();
                        if (!dirsCreated) {
                            LOGGER.error("Unable to create backup directory. Check permissions or if path is valid");
                            return;
                        }
                    }
                    boolean renamed = file.renameTo(new File(backupDir, "backup_" + file.getName()));
                    if (renamed) {
                        LOGGER.info(String.format("File %s moved to %s", file.getName(), backup));
                    }
                    else {
                        LOGGER.error(String.format("File %s move failed", file.getName()));
                    }

                }
                else {
                    LOGGER.info("Failed to move to backup");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error reading zip file", e);
            LOGGER.error("ZipFile " + file.getName() + " deleted?: " + file.delete());
        }
        finally {
            CommonUtils.close(zipFile);
            CommonUtils.close(reader);
        }
    }

    public void onReceive(Object msg) throws Exception {
        LOGGER.debug("Received message");
        if (msg instanceof File) {
            File file = (File) msg;
            LOGGER.debug("Received file: " + file.getName());
            unzipFile(file);
            sender().tell("Started Indexing " + file.getName(), self());
        }
        else if(msg instanceof String) {
            sender().tell("GOOD", self());
        }
    }
}
