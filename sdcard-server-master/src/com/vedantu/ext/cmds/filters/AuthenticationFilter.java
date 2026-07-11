package com.vedantu.ext.cmds.filters;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.managers.DataManagerUtils;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;

@WebFilter("/AuthenticationFilter")
public class AuthenticationFilter implements Filter {

    private Logger       logger = Logger.getLogger(AuthenticationFilter.class);
    private Organization org;

    @Override
    public void init(FilterConfig fConfig) throws ServletException {

        org = DataManagerUtils.getOrganization();
        logger.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        logger.info("Requested Resource::" + uri);

        HttpSession session = req.getSession(false);
        String relativeUri = uri.substring(uri.lastIndexOf(File.separator));
        logger.debug("relative url " + relativeUri);
        if (!uri.contains("public/") && !authorizedUrl.contains(relativeUri)) {
            logger.error("Unauthorized access request");
            if ((session == null || session.getAttribute(ConstantGlobal.USER_ID) == null)) {

                StringBuilder sb = new StringBuilder();
                sb.append("login.jsp");
                sb.append("?next=");
                String next = req.getRequestURL().toString();
                if (StringUtils.isNotEmpty(req.getQueryString())) {
                    next += "?" + req.getQueryString();
                }
                sb.append(URLEncoder.encode(next, "UTF-8"));
                res.sendRedirect(sb.toString());
            } else if (StringUtils.isEmpty(org.authToken) || StringUtils.isEmpty(org.secretKey)) {
                res.sendRedirect("landing.jsp");
            }

        } else {
            // pass the request along the filter chain
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

        // close any resources here
    }

    private static Set<String> authorizedUrl = new HashSet<String>(
                                                     Arrays.asList(new String[] { "/",
            "/login.jsp", "/logins", "/addOrg", "/header.jsp", "/headTag.jsp", "/welcome.jsp" }));

}