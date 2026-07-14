package com.vedantu.ext.cmds.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.vedantu.ext.cmds.db.datamanagers.JobInfoDataManager;
import com.vedantu.ext.cmds.db.models.Job;
import com.vedantu.ext.cmds.pojo.responses.local.GetJobResponse;

@WebServlet("/track")
public class JobIDTracker extends AbstractVedantuServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        super.doPost(req, resp);

        int jobId = (Integer.parseInt((String) httpParams.get("jobId")));

        Job info = JobInfoDataManager.INSTANCE.getJobInfo(jobId);
        if (info == null) {
            throw new ServletException("Job Id is not found");
        }
        GetJobResponse response = new GetJobResponse(jobId);
        response.setCompleted(info.completed);
        response.setJobId(info._id);
        response.setStatus(info.status);
        response.setTargetId(info.targetId);
        response.setTargetType(info.targetType);
        response.setSteps(info.steps);

        JSONObject json = new JSONObject(response);
        printJSONResponse(resp, json);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        this.doGet(req, resp);
    }
}
