<%@page import="java.net.InetAddress"%>
<%@ page language="java" contentType="text/comma-separated-values; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
%><%@ page import="jadex.base.relay.*"
%><%@ page import="java.util.*"
%><%
	response.setHeader("Content-Disposition", "attachment; filename=relay_statistics.csv");
%>Relay Statistics of <%= InetAddress.getLocalHost().getHostName() %> (<%= PlatformInfo.TIME_FORMAT_LONG.format(new Date()) %>)
ID;Platform;Host;IP;Scheme;Connected;Disconnected;Messages;Bytes;Transfer_Time 
<%
	PlatformInfo[]	infos	= (PlatformInfo[])request.getAttribute("platforms");
	for(int i=0; i<infos.length; i++)
	{
		%><%= infos[i].getDBId()
		%>;<%= infos[i].getId()
		%>;<%= infos[i].getHostName()
		%>;<%= infos[i].getHostIP()
		%>;<%= infos[i].getScheme()
		%>;<%= infos[i].getConnectDate().getTime()
		%>;<%= infos[i].getDisconnectDate().getTime()
		%>;<%= infos[i].getMessageCount()
		%>;<%= infos[i].getBytes()
		%>;<%= infos[i].getTransferTime() %>
<%	}
%>