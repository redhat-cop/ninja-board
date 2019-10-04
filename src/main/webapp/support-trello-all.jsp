<%@page import="
java.util.Date,
java.util.Calendar
"%>

<%@include file="header.jsp"%>
<%@include file="datatables-dependencies.jsp"%>

<script src="js/http.js"></script>

<script>
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:\${}()|\[\]\/\\])/g, "\\$1");
}
function loadDataTable(){
  var userFilter=Utils.getParameterByName("id");
  
}

$(document).ready(function() {
  if (null!=Utils.getParameterByName("name")){
    document.getElementById("title-user").innerText=": "+Utils.getParameterByName("name");
  }
  loadDataTable();
  
  document.getElementById("startDate").value="03/01/"+new Date().getFullYear();
  
});


</script>
	
    <%@include file="nav.jsp"%>

    <div class="navbar-connector"></div>
    <div class="navbar-title">
    	<h2><span class="navbar-title-text">Support<span id="title-user"></span></span></h2>
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
    
    
    </style>
    
    
    <script>
    
   	
    function go(objId){
    	var username=document.getElementById(objId).value;
    	Http.httpGetObject("/community-ninja-board/api/support/user/"+username, function(data){
    		var displayName=data['displayName'];
    		for (var key in data){
    			if (data.hasOwnProperty(key)){
    				var field=document.getElementById(key);
    				if (undefined!=field){
    					field.innerText=data[key];
    				}
    			}
    		}
    		document.getElementById("dashboardLink").innerHTML="<a target='_new' href='/community-ninja-board/mojo-dashboard-card.jsp?username="+username+"'>Dashboard</a>";
    		document.getElementById("userEventsLink").innerHTML="<a target='_new' href='/community-ninja-board/events.jsp?id="+username+"'>Events for "+displayName+"</a>";
    		
    		
    		document.getElementById("pointsBreakdown").innerHTML="";
    		for (var k in data['points']){
    			var style="";
    			if ((document.getElementById("org").value.includes("servicesmarketing") && k.includes("ThoughtLeadership")) ||
    					(document.getElementById("org").value.includes("redhatcop") && !k.includes("ThoughtLeadership"))
    					){
    				style='font-weight: bold;';
    			}
   				document.getElementById("pointsBreakdown").innerHTML+="<span style='"+style+"'>"+k +"="+ data['points'][k]+"</span><br/>";
    		}
    		//document.getElementById("pointsBreakdown").innerHTML=data['points'];
    		
    	},function(statusCode, errorText){
    		console.log("Error -> "+statusCode +" -> "+errorText);
    	});
    	
    	
    	var now=new Date();
    	var from=new Date(document.getElementById("startDate").value);
    	var daysOld=Math.round((now.getTime() - from.getTime()) / (1000 * 3600 * 24));
    	var list=document.getElementById("list").value;
    	var org=document.getElementById("org").value;
    	
    	document.getElementById("cardData").innerHTML="<tr><td colspan='6'><center>Loading...</center></td></tr>";
    	
    	Http.httpGetObject("/community-ninja-board/api/support/trello/"+username+"/cards?org="+org+"&daysOld="+daysOld+(list.length>0?"&list="+list:""), function(data){
    		document.getElementById("cardData").innerHTML="";
    		for (var key in data){
    			var boardShortId=data[key]['boardShortId'];
    			var boardName=data[key]['boardName'];;
    			var cardShortId=data[key]['shortId'];
    			var pointsFromCardTitle=data[key]['expectedPoints'];
    			var dateMovedToDone=data[key]['completedDate'];
    			var eventFound="<a href='/community-ninja-board/events.jsp?id="+username+"&filter="+cardShortId+"'>"+(data[key]['foundMatchingEvent']?"Yes":"No")+"</a>";
    			var trelloUsername=data[key]['trelloId'];
    			
    			if (new Date(dateMovedToDone)<new Date(document.getElementById("startDate").value)){
    				console.log("skipping because date ("+dateMovedToDone+") is before start date ("+document.getElementById("startDate").value+")");
    				continue;
    			}
    			
    			// checks
    			
    			dupeWarn=("false"==data[key]['hasDupeRecord'] && undefined==dateMovedToDone?"warn":"");
    			
    			
    			document.getElementById("cardData").innerHTML+=`
    				<tr>
    					<td><input onchange="calc();" name="calcPoints" id="cbx-`+cardShortId+`" type="checkbox" data-value='`+pointsFromCardTitle+`' `+(undefined!=dateMovedToDone?"checked":"")+`/></td>
							<td><a href="https://trello.com/b/`+boardShortId+`?menu=filter&filter=member:`+trelloUsername+`">`+boardName+`</a>/`+data[key]['listName']+`/<a href="https://trello.com/c/`+cardShortId+`">`+data[key]['name']+`</a></td>
							<td>`+pointsFromCardTitle+`</td>
							<td>`+(undefined!=dateMovedToDone?dateMovedToDone:"Never")+`</td>
							<td>`+eventFound+`</td>
							<td class='`+dupeWarn+`'>`+data[key]['hasDupeRecord']+`</td>
    				</tr>`;
   				
    		}
    		document.getElementById("cardData").innerHTML+=`<tr><td></td><td></td><td id='totals' style='font-weight:bold;'></td><td colspan='3'></td></tr>`;
   			calc();
    	},function(statusCode, errorText){
    		console.log("Error -> "+statusCode +" -> "+errorText);
    	});
    	
    }
    
    function calc(){
    	var total=0;
    	for (var obj of document.getElementsByName("calcPoints"))
    		total+=(obj.checked?parseInt(obj.dataset.value):0);
    	document.getElementById("totals").innerHTML=total;
    }
    
    </script>
    
    <div>
    	<table>
    		<tr>
    			<td>Starting Date:</td>
    			<td><input type="text" id="startDate" value="01-03-2019"/></td>
    		</tr>
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
    		<tr>
    			<td>Please enter your Red Hat username:</td>
    			<td><input type="text" id="supportUsername"/></td>
    			<td><input type="button" name="go" value="Go!" onclick="go('supportUsername');"/></td>
    		</tr>
    	</table>
    </div>
    
    
    
    
    <!--
    <div>
    	<table>
    		<tr>
    			<td>Please enter your Red Hat username</td><td><input type="text" name="username"/></td>
    		</tr>
    		<tr>
    			<td>A Trello card</td><td><input type="text" name="trelloId"/></td>
    		</tr>
    		<tr>
    			<td>A Github card</td><td><input type="text" name="trelloId"/></td>
    		</tr>
    	</table>
    </div>
    -->
    
    <center>
	    <h3>User Information</h3>
	    <div>
	    	<table border=1 style="width: 50%">
	    		<tr><td>Date registered:</td><td><span id="date_registered"></span></td></tr>
	    		<tr><td>Username:</td><td><span id="username"></span></td></tr>
	    		<tr><td>Display Name:</td><td><span id="displayName"></span></td></tr>
	    		<tr><td>Geo:</td><td><span id="geo"></span></td></tr>
	    		<tr><td>Level:</td><td><span id="level"></span>(<span id="levelChanged"></span>)</td></tr>
	    		<tr><td>Email:</td><td><span id="email"></span></td></tr>
	    		<tr><td>Trello Username:</td><td><span id="trello_username"></span></td></tr>
	    		<tr><td>Github Username:</td><td><span id="github_username"></span></td></tr>
	    		<tr><td>Gitlab Username:</td><td><span id="gitlab_username"></span></td></tr>
	    		<tr><td>Link to Ninja Dashboard: </td><td><span id="dashboardLink"></span></td></tr>
	    		<tr><td>Link to All Ninja Events:</td><td><span id="userEventsLink"></span></td></tr>
	    		<tr><td>Points Breakdown:</td><td><span id="pointsBreakdown"></span></td></tr>
	    	</table>
	    </div>
    </center>
    
    
    <center>
	    <h3>Trello Reconcilliation</h3>
	    <table border=1 style="width: 95%">
	    	<thead>
		    	<tr>
		    		<th></th>
		    		<th>Trello Card (Board/List/Card Title)</th>
		    		<th>Point Expected</th>
		    		<th><span title="Using Trello Activity logs, this is the date this card moved to (or passed) Done. It doesnt mean it's in the Done column now, it means it has been in the past">Date moved to Done</span></th>
		    		<th>Ninja Event Found</th>
		    		<th><span title="Does a 'duplicate check' record exist for this card + user ID in the Ninja database? If so, then the card has been counted and they've accrued points for this card">Has been counted?</span></th>
		    	</tr>
	    	</thead>
	    	<tbody id="cardData">
	    	<!--
		    	<tr>
		    		<td><a href="https://trello.com/b/clsdmAnn">services-thought-leadership-program</a>/<a href="https://trello.com/c/fK2OKjD8">fK2OKjD8</a></td>
		    		<td>3</td>
		    		<td>03-14-2019</td>
		    		<td><a href="http://localhost:8082/community-ninja-board/events.jsp?id=bszeti&filter=fK2OKjD8">Yes</a></td>
		    		<td>Yes - already counted</td>
		    		<td><a href="https://trello.com/b/clsdmAnn/services-thought-leadership-program?menu=filter&filter=member:scottknauss">services-thought-leadership-program?filter=scottknauss</a></td>
		    		<td><a href="http://localhost:8082/community-ninja-board/events.jsp?id=bszeti">Ninja/events?user=bszeti</a></td>
		    		<td></td>
		    		<td></td>
		    	</tr>
	    	-->
	    	</tbody>
	    </table>
    </center>
<br/>
<br/>

