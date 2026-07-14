package com.vedantu.ext.cmds.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.models.SDCard;

@WebServlet("/sdCardGroupInfo")
public class GetSDCardGroupInfoServlet extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doGet(req, resp);

        List<SDCard> cards = SDCardDataManger.INSTANCE.getSDCards((String) httpParams
                .get(SDCard.FIELD_GROUP_ID));
       
        JSONArray wRes = new JSONArray(cards);
        LOGGER.debug("wRes " + wRes.toString());
        JSONObject jsonRes = new JSONObject();
        jsonRes.put("cards", wRes);
        printJSONResponse(resp, jsonRes);
    }
}
