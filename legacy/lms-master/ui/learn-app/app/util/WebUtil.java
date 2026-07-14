/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import pojos.WebInfo;

/**
 *
 * @author ajithreddy
 */
public class WebUtil {

    public static WebInfo fetchDataFromLink(String url, String domain) {
        String title = null;
        String description = null;
        String keyWords = null;

        Set<String> imgLinks = new HashSet<String>();
        if (!URLUtil.isValidURL(url)) {
            Logger.log4j.info(url + " is not a valid url");
            return null;
        }

        Source source = null;
        try {
            URL soruceUrl = new URL(url);
            if (StringUtils.isEmpty(domain)) {
                domain = soruceUrl.getProtocol() + "://" + soruceUrl.getHost();
            }
            source = new Source(soruceUrl);
        } catch (MalformedURLException e) {
            Logger.log4j.error("can not connect ot " + url, e);
        } catch (IOException e) {
            Logger.log4j.error(e.getMessage(), e);
        }

        if (source == null) {
            return null;
        }

        Element element = source.getFirstElement("title");
        if (element != null) {
            title = element.getContent().getTextExtractor().toString();
        }

        List<Element> metaTags = source.getAllElements("meta");
        for (Element e : metaTags) {

            if (StringUtils.equalsIgnoreCase(e.getAttributeValue("name"),
                    "keywords")) {
                keyWords = e.getAttributeValue("content");
            } else if (StringUtils.equalsIgnoreCase(
                    e.getAttributeValue("name"), "description")) {
                description = e.getAttributeValue("content");
            } else if (StringUtils.equalsIgnoreCase(
                    e.getAttributeValue("property"), "og:description")) {
                description = e.getAttributeValue("content");
            } else if (StringUtils.equalsIgnoreCase(
                    e.getAttributeValue("property"), "og:image")) {
                imgLinks.add(e.getAttributeValue("content"));
            }
        }
        List<Element> imgRels = source.getAllElements("link");
        for (Element el : imgRels) {
            if (StringUtils.equalsIgnoreCase(
                    el.getAttributeValue("rel"), "image_src")) {
                imgLinks.add(el.getAttributeValue("href"));
            }
        }
        Logger.log4j.info("title: " + title + " \n keywords : " + keyWords
                + " and  \n description: " + description);
        if (imgLinks.isEmpty()) {
            List<Element> images = source.getAllElements("img");
            Logger.log4j.info("images : " + images);
            for (Element ie : images) {
                if (isValidImgAttribute(ie.getAttributes())) {
                    String imgSrc = ie.getAttributeValue("src");
                    if (!imgSrc.startsWith("http")) {
                        if (imgSrc.startsWith("//")) {
                            imgLinks.add("http:" + imgSrc);
                        } else if (imgSrc.startsWith("/")) {
                            imgLinks.add(domain + imgSrc);
                        } else {
                            imgLinks.add(domain + "/" + imgSrc);
                        }
//                        String protocol = StringUtils.substringBefore(url, "/");
//                        String domain = StringUtils.substringBetween(url, "//", "/");
//                        if (StringUtils.isEmpty(domain)) {
//                            domain = StringUtils.substringAfter(url, "//");
//                        }
//                        imgLinks.add(protocol + " " + protocol +"//" +domain + imgSrc);
                    } else {
                        imgLinks.add(imgSrc);
                    }
                    Logger.log4j.info("img url : " + ie.getAttributeValue("src")
                            + " : " + ie.getAttributeValue("alt"));
                }
            }
        }
        Logger.log4j.info("all imagesLinks are : " + imgLinks);
        WebInfo webInfo = new WebInfo();
        webInfo.url = url;
        webInfo.title = title;
        webInfo.description = description;
        webInfo.keywords = keyWords;
        webInfo.images = imgLinks;
        return webInfo;

    }

    public static boolean isValidImgAttribute(Attributes attributes) {
        boolean isValid = false;
        if (attributes == null) {
            return isValid;
        }
        if (StringUtils.isNotEmpty(attributes.getValue("src"))) {
            isValid = true;
        }
        return isValid;
    }

    public static void main(String[] args) {
        System.out.print(StringUtils.substringBetween("http://vedantu.com/", "//", "/"));
    }
}
