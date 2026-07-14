package com.vedantu.cmds.question.parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

public class SymbolMap {
	private static ALogger				LOGGER		= Logger.of(SymbolMap.class);
    public Map<String, String> symbol;

    public SymbolMap() throws FileNotFoundException {
        symbol=new HashMap<String, String>();
        setSymbols();
    }

    private void setSymbols() throws FileNotFoundException {
        FileInputStream fileInput = new FileInputStream(new File(Play.application().path().getAbsolutePath() + "/"
                + Play.application().configuration().getString("symbol.file")));
        DataInputStream din = new DataInputStream(fileInput);
        BufferedReader br = new BufferedReader(new InputStreamReader(din));
        String line;
       LOGGER.info("file path: "+Play.application().path().getAbsolutePath() + "/"
                + Play.application().configuration().getString("symbol.file"));
        try {
            while((line=br.readLine())!=null){
                String[] map = StringUtils.split(line);
               LOGGER.info("###############");
//                for(String s:map){
//                   LOGGER.info("map: "+s);
//                }
               LOGGER.info("map: "+map[0]+" "+map[1]+" "+map.length);
               LOGGER.info("###############");
                if(map.length>=2){
                    String key = map[0];
                    String value = map[1];
                    key=StringEscapeUtils.unescapeJava("\\uF0"+map[1].trim());
                    value=StringEscapeUtils.unescapeJava("\\u"+map[0].trim());
                    symbol.put(key, value);
                }
            }
            Iterator it = symbol.keySet().iterator();
            while(it.hasNext()){
                String key=it.next().toString();
                String value=symbol.get(key);
               LOGGER.info("key: "+key+" value: "+value);
            }
        } catch (IOException e) {
           LOGGER.error("error if reading symbol map file: "+e.getMessage());
        }
        finally{
            try {
                fileInput.close();
                din.close();
                br.close();
            } catch (IOException e) {
               LOGGER.error("error if closing symbol map file: "+e.getMessage());
            }
            
        }
    }

}
