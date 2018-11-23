<%@page import="com.redhat.sso.ninja.ManagementController"%>
<html>
<head>
	<meta http-equiv="cache-control" content="no-cache"/>
	<meta charset="utf-8"/>
	<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
	<meta name="viewport" content="width=device-width, initial-scale=1"/>
	
	<title>Communities of Practice</title>
	
	<link href="css/jquery.dataTables.min.css" rel="stylesheet">
	<link href="css/bootstrap.min.css" rel="stylesheet">
	<link rel="stylesheet" href="css/style2.css" type="text/css">
	
	<script src="js/jquery-1.11.3.min.js"></script>
	<script src="js/jquery.dataTables.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
	<script src="js/ChartNew.js"></script>
	<%
		if (ManagementController.isLoginEnabled()){
			if (null==session.getAttribute("x-access-token") || "".equals(session.getAttribute("x-access-token"))){
		response.sendRedirect("login.jsp");
			}
		}
	%>
	<script>
	var xxx="<%=session.getAttribute("x-access-token")%>";
		var jwtToken = "<%=session.getAttribute("x-access-token")!=null?session.getAttribute("x-access-token"):""%>";
	</script>
</head>