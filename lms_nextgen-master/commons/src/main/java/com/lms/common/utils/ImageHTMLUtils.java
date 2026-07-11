package com.lms.common.utils;

import com.lms.common.vedantu.enums.EntityType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ImageHTMLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHTMLUtils.class);
    private static final String IMG_IDENTIFIER = "v-uid";
    private static final String IMG_SRC = "src";
    private static final String IMG_SRC_PERMANENT = "v-perm";
    private static final String IMG_CLASS_NAME = "vImageUrl";

    public static String addImageSrcUrl(EntityType entityType, String html) {

        if (StringUtils.isEmpty(html)) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            String url = getImgUrl(entityType, es.attr(IMG_IDENTIFIER));
            if (!StringUtils.isEmpty(url)) {
                es.attr(IMG_SRC, url);
                es.attr("class", IMG_CLASS_NAME);
                es.attr(IMG_SRC_PERMANENT, Boolean.toString(true));
            }
            LOGGER.info("html element url : " + url + " and element: " + es);
        }
        String htmlBody = doc.body().html();
        return htmlBody;
    }

    public static Set<String> getImageUUids(String html) {

        Set<String> uuids = new HashSet<String>();
        if (StringUtils.isEmpty(html)) {
            return new HashSet<String>();
        }
       /* Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            if (es.attr(IMG_IDENTIFIER) != null) {
                uuids.add(es.attr(IMG_IDENTIFIER));
            }
        }

        return uuids;*/
        return null;
    }

    public static String removeImageSrcUrl(String html, Set<String> uuids) {

        if (StringUtils.isEmpty(html)) {
            LOGGER.info(" HTML content is empty");
            return "";
        }

      /*  Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();

        while (it.hasNext()) {
            Element es = it.next();
            String uuid = es.attr(IMG_IDENTIFIER);// v-uuid=type:uuid
            String isUrlPerm = es.attr(IMG_SRC_PERMANENT);
            if (StringUtils.isEmpty(isUrlPerm)) {
                uuids.add(uuid);
                es.attr(IMG_SRC_PERMANENT,"true");
                LOGGER.info(" uuid " + uuid);
            }
           
            es.attr(IMG_SRC, StringUtils.EMPTY);
        }
        return doc.body().html();*/
        return null;
    }

    private static String getImgUrl(EntityType entityType, String dataUid) {

        LOGGER.info(" dataUid: " + dataUid);
        String url = ImageDisplayURLUtil.getEntityImageURL(entityType, dataUid);
        return url;
    }

    public static void main(String[] args) {

        String html = "sadfhsalf <img data-id='s:13213' src='http://afasdf' alt='ssk' /> there is something in between <img data-id='132134' src='http://afasdf' alt='ssk' /> sdfsaf";
       /* Document d = Jsoup.parseBodyFragment(html);
        System.out.println("document d : " + d);
        Elements e = d.getElementsByAttribute("data-id");
        Iterator<Element> it = e.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            String data = es.attr("data-id");
            System.out.println(StringUtils.substringAfter(data, ":"));
            System.out.println(StringUtils.substringBefore(data, ":"));
            es.attr("data-id", "new value");
        }
        System.out.println(d.getElementsByAttribute("data-id"));
        System.out.println("new document d : " + d.body().html());*/
    }
}
