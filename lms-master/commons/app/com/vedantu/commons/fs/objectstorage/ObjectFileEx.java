package com.vedantu.commons.fs.objectstorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;

public class ObjectFileEx extends Client {

    private final static ALogger LOGGER      = Logger.of(LocalFileSystemHandler.class);

    private String               name;
    private String               containerName;
    private byte[]               bytes;
    private Map<String, Object>  headers;

    public InputStream           inputstream = null;

    public Map<String, Object> getHeaders() {

        return headers;
    }

    /**
     * This class represents a file object in a container on the objectstorage server
     * 
     * @param name
     *            the name of the server side objectstorage object
     * @param containerName
     *            the name of the container this object resides in
     * @param baseUrl
     *            the base sift rest api url
     * @param username
     *            the username for this session
     * @param password
     *            the password for this session
     * @param auth
     *            whether to auth this transaction with the REST api if not (useful if you have not
     *            yet connected this session or if you need/want to re-auth)
     * @return
     * @throws IOException
     */
    public ObjectFileEx(String name, String containerName, String baseUrl, String username,
            String password, boolean auth) throws IOException {

        super(baseUrl, username, password, auth);
        this.containerName = containerName;
        this.name = name;

    }

    /**
     * Get the name of the file on the objectstorage server
     * 
     * @return the name of the file on the objectstorage server
     */
    public String getName() {

        return name;
    }

    /**
     * Set the name of the file on the objectstorage server
     * 
     * @param name
     *            the name of the file on the objectstorage server
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * return the headers for this client transaction
     * 
     * @return Map of the name value pairs of the headers for this client transaction
     * @throws EncoderException
     * @throws IOException
     */
    public Map<String, Object> getMetaTags() throws EncoderException, IOException {

        if (headers == null)
            this.loadFileData();

        return headers;

    }

    /**
     * returns a byte[] representation of the file from the objectstorage server
     * 
     * @return a byte[] representation of the file from the objectstorage server
     * @throws EncoderException
     * @throws IOException
     */
    public byte[] getBytes() throws EncoderException, IOException {

        if (this.bytes == null)
            this.loadFileData();

        return bytes;
    }

    /**
     * returns a byte[] representation of the file from the objectstorage server
     * 
     * @return a byte[] representation of the file from the objectstorage server
     * @throws EncoderException
     * @throws IOException
     */
    public byte[] getBytes(Range range) throws EncoderException, IOException {

        if (this.bytes == null) {
            System.out.println("Fetch ranged data between " + range.getIndex() + " And "
                    + (range.getIndex() + range.getSize()));
            this.loadFileData(range);
        } else {

            System.out.println("Data already fetched");
        }
        return bytes;
    }

    /**
     * upload this file from a local file copy to the objectstorage server
     * 
     * @param localFileLocation
     *            string representation of the path of the local file
     * @param tags
     *            Map of tags to attach to this file
     * @return etag value of this upload
     * @throws EncoderException
     * @throws IOException
     */
    public String uploadFile(File file, Map<String, String> tags) throws EncoderException,
            IOException {

        if (super.isValidName(this.name)) {
            Hashtable<String, String> params = super.createAuthParams();
            Iterator<Map.Entry<String, String>> it = tags.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                params.put(Client.X_OBJECT_META + pairs.getKey(), pairs.getValue());
                it.remove();
            }

            String uName = super.saferUrlEncode(this.containerName);
            String fName = super.saferUrlEncode(this.name);
            Representation representation = new FileRepresentation(file, MediaType.ALL);
            ClientResource client = super.put(params, representation, super.storageurl + "/"
                    + uName + "/" + fName);
            this.headers = client.getResponseAttributes();
            Form head = (Form) this.headers.get("org.restlet.http.headers");
            return head.getFirstValue("Etag");
        } else {
            throw new EncoderException("invalid file name");
        }

    }

    /**
     * removes this file form the objectstorage server
     * 
     * @throws EncoderException
     * @throws IOException
     */
    public void remove() throws EncoderException, IOException {

        // super.auth(username, password);
        Hashtable<String, String> params = super.createAuthParams();
        String uName = super.saferUrlEncode(this.containerName);
        String fName = super.saferUrlEncode(this.name);
        super.delete(params, super.storageurl + "/" + uName + "/" + fName);

    }

    /**
     * purge this file from CDN
     * 
     * @throws EncoderException
     * @throws IOException
     */
    public void purgeCDN() throws EncoderException, IOException {

        // super.auth(username, password);
        Hashtable<String, String> params = super.createAuthParams();
        String uName = super.saferUrlEncode(this.containerName);
        String fName = super.saferUrlEncode(this.name);
        super.delete(params, super.cdnurl + "/" + uName + "/" + fName);

    }

    /**
     * make a file copy in this container from another file
     * 
     * @param container
     *            the source container to copy from
     * @param objectName
     *            the source file to copy from
     * @throws EncoderException
     * @throws IOException
     */
    public void copyFrom(String container, String objectName) throws EncoderException, IOException {

        String cuName = super.saferUrlEncode(container);
        String cfName = super.saferUrlEncode(objectName);
        String sourceUrl = cuName + "/" + cfName;
        Hashtable<String, String> params = super.createAuthParams();
        params.put(Client.X_COPY_FROM, sourceUrl);

        String uName = super.saferUrlEncode(this.containerName);
        String fName = super.saferUrlEncode(this.name);

        super.put(params, null, super.storageurl + "/" + uName + "/" + fName);
    }

    /**
     * Utility method for getting data from REST api to populate this object
     * @return 
     * 
     * @throws EncoderException
     * @throws IOException
     */
    public void loadFileData(Range range) throws EncoderException, IOException {

        Hashtable<String, String> params = super.createAuthParams();
        URLCodec ucode = new URLCodec();
        String uName = ucode.encode(this.containerName).replaceAll("\\+", "%20");
        String fName = ucode.encode(this.name).replaceAll("\\+", "%20");
        ClientResource client = super.get(params, super.storageurl + "/" + uName + "/" + fName);
        if (range != null) {

            LOGGER.debug("Range" + range.getIndex() + "  size " + range.getSize());
            List<Range> rangeList = new ArrayList<Range>();
            rangeList.add(range);
            client.setRanges(rangeList);
        }
        this.headers = client.getResponseAttributes();
        inputstream = client.get().getStream();
        for (String key : this.headers.keySet()) {
            LOGGER.debug("Object storage Header : " + key + " value:" + this.headers.get(key)
                    + " \n");
        }
        LOGGER.debug("Object storage response attribute : " + client.getResponseAttributes()
                + " \n");
        LOGGER.debug("Object storage ranges : " + client.getRanges() + " \n");
        LOGGER.debug("Object storage response : " + client.getResponse() + " \n");

    }

    /**
     * Utility method for getting data from REST api to populate this object
     * 
     * @throws EncoderException
     * @throws IOException
     */
    private void loadFileData() throws EncoderException, IOException {

        Hashtable<String, String> params = super.createAuthParams();
        URLCodec ucode = new URLCodec();
        String uName = ucode.encode(this.containerName).replaceAll("\\+", "%20");
        String fName = ucode.encode(this.name).replaceAll("\\+", "%20");
        ClientResource client = super.get(params, super.storageurl + "/" + uName + "/" + fName);


        // client.get().write(outputStream);
        inputstream = client.get().getStream();
        this.headers = client.getResponseAttributes();
    }

    public InputStream getStream() throws EncoderException, IOException {

        if (inputstream == null) {
            loadFileData();
        }
        return this.inputstream;
    }
}
