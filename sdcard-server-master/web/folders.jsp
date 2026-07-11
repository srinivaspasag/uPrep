<%@page import="com.vedantu.ext.cmds.enums.EntityType"%>
<%@page import="com.vedantu.ext.cmds.db.models.Resource"%>
<%@page
	import="com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager"%>
<%@page import="com.vedantu.ext.cmds.utils.config.Config"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Folders</title>
</head>
<body>
	<%
	    ResourceDataManager fDataManager = ResourceDataManager.INSTANCE;

	    Resource rootFolder = fDataManager.getResource(Config.DESKTOP_FOLDER_NAME,
	            EntityType.FOLDER.name());

	    List<Resource> folders = fDataManager.getResources(rootFolder.id, rootFolder.type,
	            EntityType.FOLDER.name());
	    if (folders.isEmpty()) {
	        out.println("<div>No Folders Added</div>");
	    } else {
	        for (Resource folder : folders) {
	%>
	<div><%=folder.name%></div>
	<%
	    }
	    }
	%>
</body>
</html>