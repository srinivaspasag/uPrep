package com.lms.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lms.common.vedantu.entity.storage.MediaType;
@Service
public class MediaTypeMapper {
	private Map<String, MediaType> mediaTypeMap;
	@Value("${media.types.file.path}")
    private String   mediaTypeConfFileName;
    
    @PostConstruct
    private void initializeMediaTypeConfFile() throws Exception {
    	/*FileReader fr;
        fr = new FileReader(new File("path" + "/" + mediaTypeConfFileName));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
           // String[] keyValue = StringUtils.split(line, ' ');
        	String[] keyValue = line.split(" ");
            String key = keyValue[0];
            MediaType value = MediaType.valueOfKey(keyValue[1]);
            mediaTypeMap.put(key, value);
        }
        br.close();*/
    }
    public MediaType getMediaType(String fileExtension) {

        if (!this.mediaTypeMap.containsKey(fileExtension)) {
            return MediaType.UNKNOWN;
        }
        return this.mediaTypeMap.get(fileExtension);
    }
    
}
