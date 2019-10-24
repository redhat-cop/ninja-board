<%@page import="
java.util.Date,
java.util.Calendar
"%>

<script src="js/http.js"></script>

<%@include file="header.jsp"%>
<%@include file="datatables-dependencies.jsp"%>
<%if (!"true".equalsIgnoreCase(request.getParameter("embedded"))){%>
<%}%>



<script>
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:\${}()|\[\]\/\\])/g, "\\$1");
}

$(document).ready(function() {
  document.getElementById("startDate").value="03/01/"+new Date().getFullYear();
});


</script>
<%if (!"true".equalsIgnoreCase(request.getParameter("embedded"))){%>
    <%@include file="nav.jsp"%>
<%}%>

    <div class="navbar-connector"></div>
    <div class="navbar-title">
    	<h2><span class="navbar-title-text">Giveback Ninja - Support<span id="title-user"></span></span></h2>
    </div>
    
    <style>
    table tr td:nth-child(1), table tr td:nth-child(6), table tr td:nth-child(7){
    	white-space: nowrap;
    }
    table{
    	/*
    	width: 90%;
    	*/
    }
    table tr td, table tr th{
    	padding: 3px;
    }
    
    .navbar-title{
    	position: relative;
    	text-align: center;
    	top:0px;
    }
    
    .boxed{
    	text-align:center;
    	border: 1px solid black;
    	border-collapse: unset;
    }
    .navbar-title{
    	width: 100%;
    	left: 0px;
    	margin-bottom: 40px;
    }
    .navbar-connector{
    	display: none;
    }
    .title{
    	font-weight: bold;
    	vertical-align: top;
    }
    </style>
    
    
    <script>
    var userData=undefined;
    var cardData=undefined;
    var username;
    var errors=[];
    
    
    $( document ).ready(function() {
	    <%if (request.getParameter("cardShortId")!=null){%>
	    getById("cardShortId").value="<%=request.getParameter("cardShortId")%>";
	    <%}%>
	    <%if (request.getParameter("username")!=null){%>
	    getById("supportUsername").value="<%=request.getParameter("username")%>";
	    <%}%>
	    <%if (request.getParameter("cardShortId")!=null && request.getParameter("username")!=null){%>
	    go('supportUsername');
	    <%}%>
    });
    
    function go(objId){
    	username=document.getElementById(objId).value;
    	Http.httpGetObject("/community-ninja-board/api/support/user/"+username, function(data){
    		var displayName=data['displayName'];
    		for (var key in data){
    			if (data.hasOwnProperty(key)){
    				var field=document.getElementById(key);
    				if (undefined!=field){
    					
    					if ("levelChanged"==key){
    						field.innerText=" - graded up on "+data[key];
    					}else{
	    					field.innerText=data[key];
    					}
    				}
    			}
    		}
    		
    		if (undefined!=document.getElementById("dashboardLink"))
    			document.getElementById("dashboardLink").innerHTML="<a target='_new' href='/community-ninja-board/mojo-dashboard-card.jsp?username="+username+"'>Dashboard</a>";
    		if (undefined!=document.getElementById("userEventsLink"))
    			document.getElementById("userEventsLink").innerHTML="<a target='_new' href='/community-ninja-board/events.jsp?id="+username+"'>Events for "+displayName+"</a>";
    		
    		
   			if (undefined!=document.getElementById("pointsBreakdown")){
   				var points="<table>";
   				for (var k in data['points'])
   					points+="<tr><td>"+k+"</td><td style='white-space: nowrap;'>"+data['points'][k]+" points</td></tr>";
   				document.getElementById("pointsBreakdown").innerHTML=points+"</table>";
   				
   				
	    		//document.getElementById("pointsBreakdown").innerHTML="<table>";
	    		//for (var k in data['points']){
	    		//	var style="";
	   			//	document.getElementById("pointsBreakdown").innerHTML+="<span style='"+style+"'>"+k +"="+ data['points'][k]+"</span><br/>";
	    		//}
	    		document.getElementById("pointsBreakdown").innerHTML+="</table>";
   			}
    		userData=data;
    		analyse();
    		
    	},function(statusCode, errorText){
    		console.log("Error -> "+statusCode +" -> "+errorText);
    	});
    	
    	
    	var now=new Date();
    	var from=new Date(getById("startDate").value);
    	var daysOld=Math.round((now.getTime() - from.getTime()) / (1000 * 3600 * 24));
    	
    	
    	//document.getElementById("cardData").innerHTML="<tr><td colspan='6'><center>Loading...</center></td></tr>";
    	
    	Http.httpGetObject("/community-ninja-board/api/support/trello/"+username+"/card/"+getById("cardShortId").value+"", function(data){
    		//document.getElementById("cardData").innerHTML="";
    		for (var key in data){
    			var boardShortId=data[key]['boardShortId'];
    			var boardName=data[key]['boardName'];;
    			var cardShortId=data[key]['shortId'];
    			var pointsFromCardTitle=data[key]['expectedPoints'];
    			var dateMovedToDone=data[key]['completedDate'];
    			var eventFound="<a href='/community-ninja-board/events.jsp?id="+username+"&filter="+cardShortId+"'>"+(data[key]['foundMatchingEvent']?"Yes":"No")+"</a>";
    			var trelloUsername=data[key]['trelloId'];
    			
    			if (new Date(dateMovedToDone)<new Date(getById("startDate").value)){
    				errors.push("Card is too old. This FY started "+getById("startDate").value +" and the card was moved to done on "+dateMovedToDone);
    			}
    			
    			// checks
    			
    			dupeWarn=("false"==data[key]['hasDupeRecord'] && undefined==dateMovedToDone?"warn":"");
    			dupeTitle="No, this card has not been counted for trello user: "+trelloUsername;
    			
    			
    			//var hasDupeRecord=data[key]['hasDupeRecord'];
    			//var hasBeenDone=dateMovedToDone!=undefined;
    			//
    			//var message="", rectification="";
    			//// "You already have points for this card"
    			//if (hasDupeRecord){
    			//	message="You already have points for this card";
    			//}
    			//// "Card isn't completed yet"
    			//if (!hasDupeRecord && !hasBeenDone){
    			//	message="Card has not been moved to Done yet";
    			//	rectification="";
    			//}
    			//// "Card has been completed, but you are not a member"
    			//if (!hasDupeRecord && hasBeenDone && !data[key]['members'].includes(trelloUsername)){
    			//	message=username+" (trello:@"+trelloUsername+") is not a member of this card and it's been Done already";
    			//	rectification="Move the card back out, add (trello:@"+trelloUsername+") as a member, and then move the card back into the Done column";
    			//}
    			//// "You were assigned a member of this card after this card was moved to Done"
    			//// CANT DO THIS UNTIL WE PARSE THE ACTIVITIES BETTER
    			//
    			//// Card was completed before the user registered
    			//// TODO: this wont work unless the user info is loaded first - race condition danger - needs re-engineering
    			////if (new Date(dateMovedToDone)<new Date(userData['date_registered'])){
    			////	message=username+" registered after the card was moved to done";
    			////	rectification="None, if the user wasn't registered prior to the card being completed, they're not eligible for points";
    			////}
    			//
    			//// Card was completed before the start of the program
    			//if (from>new Date(dateMovedToDone)){
    			//	message="Card was moved to done ("+dateMovedToDone+") before the start of the program ("+getById("startDate").value+")";
    			//	rectification="Only cards after the program start date accrue points";
    			//}
    			
    			var members="";
    			for(m in data[key]['members']){
    				members+=data[key]['members'][m]['name']+",";
    			}
    			
    			document.getElementById("trelloCard").innerHTML=`<a href="https://trello.com/b/`+boardShortId+`?menu=filter&filter=member:`+trelloUsername+`">`+boardName+`</a>/`+data[key]['listName']+`/<a href="https://trello.com/c/`+cardShortId+`">`+data[key]['name']+`</a>`;
    			document.getElementById("trelloCardPointsExpected").innerHTML=pointsFromCardTitle;
    			document.getElementById("trelloCardMembers").innerHTML=members;
    			document.getElementById("trelloCardDateMovedToDone").innerHTML=(undefined!=dateMovedToDone?dateMovedToDone:"Never");
    			document.getElementById("trelloCardCounted").innerHTML=`<span title="`+dupeTitle+`">`+data[key]['hasDupeRecord']+`</span>`;
    			//document.getElementById("trelloCardMessage").innerHTML=message;
    			//document.getElementById("trelloCardResolution").innerHTML=rectification;
    			
    			
    			//document.getElementById("cardData").innerHTML+=`
    			//	<tr>
					//		<td><a href="https://trello.com/b/`+boardShortId+`?menu=filter&filter=member:`+trelloUsername+`">`+boardName+`</a>/`+data[key]['listName']+`/<a href="https://trello.com/c/`+cardShortId+`">`+data[key]['name']+`</a></td>
					//		<td>`+pointsFromCardTitle+`</td>
					//		<td>`+members+`</td>
					//		<td>`+(undefined!=dateMovedToDone?dateMovedToDone:"Never")+`</td>
					//		<td class='`+dupeWarn+`'><span title="`+dupeTitle+`">`+data[key]['hasDupeRecord']+`</span></td>
					//		<td>`+message+`</td>
					//		<td>`+rectification+`</td>
    			//	</tr>`;
   				
    		}
    		//document.getElementById("cardData").innerHTML+=`<tr><td></td><td></td><td id='totals' style='font-weight:bold;'></td><td colspan='3'></td></tr>`;
//   			calc();
				cardData=data[0];
				analyse();
				
    	},function(statusCode, errorText){
    		console.log("Error -> "+statusCode +" -> "+errorText);
    	});
    	
    }
    
    function getById(id){ return document.getElementById(id); }
    
    function analyse(){
    	if (undefined!=userData && undefined!=cardData){
    		
    		var from=new Date(getById("startDate").value);
    		var trelloUsername=cardData['trelloId'];
    		var dateMovedToDone=cardData['completedDate'];
				var hasDupeRecord=cardData['hasDupeRecord'];
				var hasBeenDone=dateMovedToDone!=undefined;
				var message="", rectification="";
				
				// internal data error checking
				if (dateMovedToDone!=undefined && hasDupeRecord==false){
					message="Card is in (or has been through) 'Done', however Ninja has no record for this card in the database";
					rectification="Contact Ninja support with the username & trello short ID - this needs more investigation"
					setMessage(message, rectification); return;
				}
				
				
				// "You already have points for this card"
				if (hasDupeRecord){
					message=userData["displayName"]+" already has points for this card";
					setMessage(message, rectification); return;
				}
				// "Card isn't completed yet"
				if (!hasDupeRecord && !hasBeenDone){
					message="Card has not been moved to Done yet";
					rectification="The card must be completed and moved to 'Done' column in Trello. Also, ensure the members have been assigned";
					setMessage(message, rectification); return;
				}
				// "Card has been completed, but you are not a member"
				if (!hasDupeRecord && hasBeenDone && !cardData['members'].includes(trelloUsername)){
					message=username+" (trello:@"+trelloUsername+") is not a member of this card and it's been Done already";
					rectification="Move the card back out, add (trello:@"+trelloUsername+") as a member, and then move the card back into the Done column";
					setMessage(message, rectification); return;
				}
				// "You were assigned a member of this card after this card was moved to Done"
				// CANT DO THIS UNTIL WE PARSE THE ACTIVITIES BETTER
				
				// Card was completed before the user registered
				if (new Date(dateMovedToDone)<new Date(userData['date_registered'])){
					message=username+" registered after the card was moved to done";
					rectification="None, if the user wasn't registered prior to the card being completed, they're not eligible for points";
					setMessage(message, rectification); return;
				}
				
				// Card was completed before the start of the program
				if (from>new Date(dateMovedToDone)){
					message="Card was moved to done ("+dateMovedToDone+") before the start of the program ("+getById("startDate").value+")";
					rectification="Only cards after the program start date accrue points";
					setMessage(message, rectification); return;
				}
				
    	}
    }
    
    function setMessage(message, rectification){
 			document.getElementById("trelloCardMessage").innerHTML=message;
 			document.getElementById("trelloCardResolution").innerHTML=rectification;
    }
//    function calc(){
//    	var total=0;
//    	for (var obj of document.getElementsByName("calcPoints"))
//    		total+=(obj.checked?parseInt(obj.dataset.value):0);
//    	document.getElementById("totals").innerHTML=total;
//    }
    
    </script>
    
    <center>
    <div>
    	<table class="boxed">
    		<%if (!"true".equalsIgnoreCase(request.getParameter("embedded"))){%>
    		<tr>
    			<td class="title">Search From:</td>
    			<td><input type="text" id="startDate" value="not set"/></td>
    		</tr>
    		<%}else{%>
    			<tr><td><input type="hidden" id="startDate" value="not set"/></td></tr>
    		<%}%>
    		<tr>
    			<td class="title">Trello ShortId:</td>
    			<!--
    			<td><input type="text" id="cardShortId" value="fK2OKjD8"/></td>
    			<td><input type="text" id="cardShortId" value="7N1IZgPA"/></td>
    			-->
    			<td><input type="text" id="cardShortId" value=""/></td>
    		</tr>
    		<!--
    		<tr>
    			<td>Trello Organization:</td>
    			<td>
    				<select id="org">
    					<option value="redhatcop">redhatcop</option>
    					<option value="servicesmarketing">servicesmarketing</option>
    				</select>
    			</td>
    		</tr>
    		<tr>
    			<td>Trello List (empty for cards on all lists):</td>
    			<td><input type="text" id="list" value="Done"/></td>
    		</tr>
    		-->
    		<tr>
    			<td class="title">Red Hat username:</td>
    			<td><input type="text" id="supportUsername" value=""/></td>
    			<td><input type="button" name="go" value="Go!" onclick="go('supportUsername');"/></td>
    		</tr>
    	</table>
    </div>
    </center>
    
    <center>
	    <div style="display: flex;">
		    
		    <div style="flex-grow: 1; padding: 30px;">
		    	<h3>User Information</h3>
		    	<table border=1 style="width: 100%">
		    		<tr><td class="title" style="width: 40%">Date registered:</td><td><span id="date_registered"></span></td></tr>
		    		<!--
		    		<tr><td>Username:</td><td><span id="username"></span></td></tr>
		    		-->
		    		<tr><td class="title">Display Name:</td><td><span id="displayName"></span></td></tr>
		    		<tr><td class="title">Geo:</td><td><span id="geo"></span></td></tr>
		    		<tr><td class="title">Level:</td><td><span id="level"></span><span id="levelChanged"></span></td></tr>
		    		<tr><td class="title">Email:</td><td><span id="email"></span></td></tr>
		    		<tr><td class="title">Trello Username:</td><td><span id="trello_username"></span></td></tr>
		    		<tr><td class="title">Github Username:</td><td><span id="github_username"></span></td></tr>
		    		<tr><td class="title">Gitlab Username:</td><td><span id="gitlab_username"></span></td></tr>
		    		
		    		<%if (!"true".equalsIgnoreCase(request.getParameter("embedded"))){%>
		    		<tr><td class="title">Link to Ninja Dashboard: </td><td><span id="dashboardLink"></span></td></tr>
		    		<tr><td class="title">Link to All Ninja Events:</td><td><span id="userEventsLink"></span></td></tr>
		    		<%}%>
		    		<tr><td class="title">Points Breakdown:</td><td><span id="pointsBreakdown"></span></td></tr>
		    	</table>
		    </div>
 		    <div style="flex-grow: 1; padding: 30px;">
		    	<h3>Trello Card Information</h3>
		    	<table border=1 style="width: 100%">
		    		<tr><td class="title" style="width: 20%">Trello Card (Board/List/Card Title)</td><td><span id="trelloCard"></span></td></tr>
		    		<tr><td class="title">Point Expected</td><td><span id="trelloCardPointsExpected"></span></td></tr>
		    		<tr><td class="title">Members</td><td><span id="trelloCardMembers"></span></td></tr>
		    		<tr><td class="title"><span title="Using Trello Activity logs, this is the date this card moved to (or passed) Done. It doesnt mean it's in the Done column now, it means it has been in the past">Date moved to Done</span></td><td><span id="trelloCardDateMovedToDone"></span></td></tr>
		    		<tr><td class="title"><span title="Does a 'duplicate check' record exist for this card + user ID in the Ninja database? If so, then the card has been counted and they've accrued points for this card">Has been counted?</span></td><td><span id="trelloCardCounted"></span></td></tr>
		    		<tr><td class="title">Message</td><td><span id="trelloCardMessage"></span></td></tr>
		    		<tr><td class="title">Resolution</td><td><span id="trelloCardResolution"></span></td></tr>
		    	</table>
		    </div>
		  </div>
    </center>
    
    <!--
    <center>
	    <h3>Trello Reconcilliation</h3>
	    <table border=1 style="width: 95%">
	    	<thead>
		    	<tr>
		    		<th>Trello Card (Board/List/Card Title)</th>
		    		<th>Point Expected</th>
		    		<th>Members</th>
		    		<th><span title="Using Trello Activity logs, this is the date this card moved to (or passed) Done. It doesnt mean it's in the Done column now, it means it has been in the past">Date moved to Done</span></th>
		    		<th><span title="Does a 'duplicate check' record exist for this card + user ID in the Ninja database? If so, then the card has been counted and they've accrued points for this card">Has been counted?</span></th>
		    		<th>Message</th>
		    		<th>Resolution</th>
		    	</tr>
	    	</thead>
	    	<tbody id="cardData">
	    	</tbody>
	    </table>
    </center>
    -->
<br/>
<br/>

