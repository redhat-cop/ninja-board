<%@page import="com.redhat.sso.roxy.Controller"%>
	<title>Communities of Practice</title>
	
	<link href="css/jquery.dataTables.min.css" rel="stylesheet">
	<link href="css/bootstrap.min.css" rel="stylesheet">
	<link rel="stylesheet" href="css/style2.css" type="text/css">
	
	<script src="js/jquery-1.11.3.min.js"></script>
	<script src="js/jquery.dataTables.min.js"></script>
	<script src="js/bootstrap.min.js"></script>

    <%@include file="nav.jsp"%>
    
    <div id="login">
			<div class="row">
				<div class="w-100" style="width: 30%; margin: 0 auto;">
					<div class="modal-content" style="margin: auto;">
						<div class="modal-body">
							<form id="loginForm" action="api/login" method="post">
								<div class="form-group">
									<label for="username" class="control-label">Username:</label>
									<input id="username" name="username" type="text" class="form-control">
								</div>
								<div class="form-group">
									<label for="password" class="control-label">Password:</label>
									<input id="password" name="password" type="password" class="form-control">
								</div>
								<input id="submit" class="btn btn-primary" type="submit" value="Submit">
							</form>
						</div>
					</div>
				</div>
			</div>
    </div>

