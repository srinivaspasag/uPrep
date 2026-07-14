<%@page import="com.vedantu.ext.cmds.utils.commons.StringUtils"%>
<%
    Cookie[] cookies = request.getCookies();
    String errorMessage = "";
    if (cookies != null) {
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals("errorMessage")) {
                errorMessage = cookie.getValue();
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    if (StringUtils.isNotEmpty(errorMessage)) {
%>

<div class="errorMessageDiv margTop10"><%=errorMessage%></div>
<%
    }
%>