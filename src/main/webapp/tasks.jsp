<%@page import="
java.util.Date,
java.util.Calendar
"%>


<html lang="en">
<head>

<%@include file="header.jsp"%>

<%@include file="nav.jsp"%>

		<div class="navbar-connector"></div>
    <div class="navbar-title">
    	<h2><span class="navbar-title-text">Tasks</span></h2>
    </div>
    
    <link rel="stylesheet" href="https://raw.githack.com/riktar/jkanban/master/dist/jkanban.min.css">
    <link rel="stylesheet" href="css/tasks.css">
    
		<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/solid.css" integrity="sha384-+0VIRx+yz1WBcCTXBkVQYIBVNEFH1eP6Zknm16roZCyeNg2maWEpk/l/KsyFKs7G" crossorigin="anonymous">
		<!--link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/regular.css" integrity="sha384-aubIA90W7NxJ+Ly4QHAqo1JBSwQ0jejV75iHhj59KRwVjLVHjuhS3LkDAoa/ltO4" crossorigin="anonymous"-->
		<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/fontawesome.css" integrity="sha384-jLuaxTTBR42U2qJ/pm4JRouHkEDHkVqH0T1nyQXn1mZ7Snycpf6Rl25VBNthU4z0" crossorigin="anonymous">

    <link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet">

    <style>
        body {
            font-family: "Lato";
            margin: 0;
            padding: 0;
            width: 100%;
        }
        #myKanban {
            overflow-x: auto;
            padding: 20px 0;
            width: 95%;
        }
        .success {
            background: #00B961;
        }
        .info {
            background: #2A92BF;
        }
        .warning {
            background: #F4CE46;
        }
        .error {
            background: #FB7D44;
        }
    </style>
</head>
<body>
<div id="myKanban"></div>


<!--
<input type="text" id="newItem" /><button disabled id="addToDo">Add</button>
<script src="https://raw.githack.com/riktar/jkanban/master/dist/jkanban.min.js"></script>
-->
<script src="js/jkanban.js"></script>


<script>

$(document).ready(function() {
	Http.httpGet("${pageContext.request.contextPath}/api/tasks", function(response){
		
		var j=JSON.parse(response);
		var todo=j.todo;
		var working=j.working;
		var done=j.done;
		
		
		//console.log("response="+j);
		//console.log("response.todo="+j.todo);
		//$('#alertsEnabled').prop("checked", "true"==response.toLowerCase());
		
		var id=0;
		
    var KanbanTest = new jKanban({
        element: '#myKanban',
        gutter: '10px',
        widthBoard: '450px',
        dragBoards: false,
        contextMenu: true,
        deleteCards: true,
        comments: false,
        userAssignment: false,
        labels: true,
        onLabelNew: function(el, nodeItem, addLabel){
          console.log("onLabelNew():: el.value="+el.value+", el.dataset="+JSON.stringify(el.dataset) +", nodeItem="+JSON.stringify(nodeItem.dataset));
          if (undefined==el.value || el.value=="") return;
          Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.id+"/labels/"+el.value, null, function(response, status){
            if (status==200)
            	addLabel(el, el.dataset.id, el.value);
          });
        },
        
        onLabelDelete: function(el, nodeItem, removeLabel){
          console.log("onLabelDelete():: el="+JSON.stringify(el.dataset) +", nodeItem="+JSON.stringify(nodeItem.dataset));
          Http.httpDelete("${pageContext.request.contextPath}/api/tasks/"+el.dataset.id+"/labels/"+el.dataset.label, null, function(response, status){
            if (status==200)
            	removeLabel(el, el.dataset.id, el.value);
          });
        },
        onUpdate: function(el, nodeItem, updateTitle){
        	console.log("onUpdate():: el="+el.value +", nodeItem="+JSON.stringify(nodeItem.dataset));
        	
        	var data={"title":el.value};
        	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+nodeItem.dataset.eid, data, function(response, status){
		   				// TODO: get the status and change only if it's a 200 - for now, just hope it went ok
		   				if (status==200){
		   					updateTitle(el, el.dataset.id, el.value);
		   				}
		    	});
        	
        },
        onDelete: function(el){
        	console.log("onDelete():: el="+JSON.stringify(el.dataset));
        	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.eid+"/delete", null, function(response){
       				KanbanTest.removeElement(el.dataset.eid);
        	});
        },
        customDisplay: function(boardId, el, data){
        	console.log("customDisplay():: el="+JSON.stringify(el.dataset));
        	
        	return "<textarea id='title_"+data.id+"' style='height:10px' class='title'>"+data.title+"</textarea><br/><span style='border: 3px solid transparent'>"+data.timestamp.substring(0,10)+"</span>";
        	
        	
        	//return "<div class='card'>"+
        	//			 "<div class='header'><a class='id' href=''>"+id+"</a><span class='right'><button onclick=''><i class='unassigned fa fa-user'></i></button></span></div>"+
        	//			 "<div class='body'><textarea class='title' onblur='card_title_update(\""+data.id+"\",this);'>"+data.title+"</textarea></div>"+
        	//			 "<div class='footer clearfix'><div class='footer-labels'></div>"+
        	//			 "<div class='footer-actions right'>"+
        	//			   "<button><i class='fas fas-comment-alt'></i></button>"+
        	//			   "<button><i class='fa fa-dots'></i></button>"+
        	//			 "</div></div>"+
        	//			 "</card>";
        	//
        	//return "<table><tr><td>"+data.title+"</td></tr><tr><td>"+data.user+"</td></tr><tr><td>"+data.timestamp.substring(0,10)+"</td></tr></table>";
        	//
        	//return "<table><tr><td>"+data.title+"</td></tr><tr><td>"+data.timestamp.substring(0,10)+"</td></tr></table>";
        },
        //click: function (el) {
        //	console.log("onClick: "+el.dataset.eid);
        //	//
        //	//Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.eid+"/delete", null, function(response){
        //	//	// update board to show removal of task?
        //	//});
        //	
        //    //console.log("Trigger on all items click!");
        //},
        dropEl:function (el, target, source, sibling) {
        	
        	var taskGuid=el.dataset.eid;
        	var taskTitle=el.innerText;
        	var sourceBoardName=source.offsetParent.dataset.id;
        	var targetBoardName=target.offsetParent.dataset.id;
        	
        	console.log("dragged from "+sourceBoardName+" to "+targetBoardName+" - title="+taskTitle+ " - guid="+taskGuid);
        	
        	var data={'list':targetBoardName};
        	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+taskGuid, data, function(response){
        		//need to figure out how to update the task to say its been dragged
        	});
        	
        },
//        buttonClick: function (el, boardId) {
//            console.log("el="+el);
//            console.log("boardId="+boardId);
//            // create a form to enter element 
//            var formItem = document.createElement('form');
//            formItem.setAttribute("class", "itemform");
//            formItem.innerHTML = '<div class="form-group"><textarea id="new" class="form-control" rows="2" autofocus></textarea></div><div class="form-group"><button type="submit" class="btn btn-primary btn-xs pull-right">Submit</button><button type="button" id="CancelBtn" class="btn btn-default btn-xs pull-right">Cancel</button></div>'
//            KanbanTest.addForm(boardId, formItem);
//            formItem.addEventListener("submit", function (e) {
//                console.log("submit pressed");
//                var title=$("#new").innerText();
//                console.log("title="+title);
//                var data='{"title":'+title+', "labels":labels}';
//                
//                //Http.httpPost("${pageContext.request.contextPath}/api/tasks", JSON.stringify(data), function(response){
//                //  
//                //});
//                e.preventDefault();
//                var text = e.target[0].value
//                KanbanTest.addElement(boardId, {
//                    "title": text,
//                })
//                formItem.parentNode.removeChild(formItem);
//            });
//            document.getElementById('CancelBtn').onclick = function () {
//                formItem.parentNode.removeChild(formItem)
//            }
//        },
        addItemButton: false,
        boards: [
            {
                "id": "_todo",
                "title": "To Do",
                "class": "info,good",
                "item": todo
            },
            {
                "id": "_working",
                "title": "Working",
                "class": "info,good",
                "item": working
            },
            {
                "id": "_done",
                "title": "Done",
                "class": "success",
                "item": done
            }
        ]
    });
    
    //$(document).on('click', "#newItem", function() {
		//	document.getElementById("addToDo").disabled=document.getElementById("newItem").value.length>0;
		//});
    
    //var toDoButton = document.getElementById('addToDo');
    //toDoButton.addEventListener('click', function () {
    //	  var newItem={"title":document.getElementById("newItem").value};
    //	  console.log("item text = "+newItem);
    //    
    //    Http.httpPost("${pageContext.request.contextPath}/api/tasks", newItem, function(response){
    //	  document.getElementById("newItem").value="";
	  //      // if server created the task, then show it on the kanban board
    //    	KanbanTest.addElement(
	  //          "_todo",
	  //          {
	  //              "id": response,
	  //              "title": newItem.title,
	  //          }
	  //      );
    //    });
    //  
    //});
	});
	
	
	//$('.dropdown-togglex').dropdown()
	
});




</script>
</body>
</html>


<!--
<div style="display:none" class="dropdown-menu waffle-dropdown-menu assignees-dropdown-menu ng-scope ng-isolate-scope" card="card" ng-if="expanded">
  <div class="up-arrow"></div>
  <div class="up-arrow-inner"></div>

  <div class="text-center waffle-dropdown-menu-header">
    <p>Assign up to 10 people to this card</p>
    <input type="text" ng-model="assigneeSearch.login" placeholder="Filter people" class="form-control js-assignee-search ng-pristine ng-valid">
  </div>

  <ul class="text-left waffle-dropdown-menu-list" ng-click="$event.stopPropagation()">
    <li class="waffle-dropdown-menu-list-item" ng-click="setAssignees()">
      <i class="fa fa-times-circle-o clear-selection-icon" aria-hidden="true"></i>
      <span class="clear-selection-text">Clear assignees</span>
    </li>
    <li ng-hide="possibleAssignees" class="text-center ng-hide"><i class="fa fa-spinner fa-spin"></i></li>
    <li class="waffle-dropdown-menu-list-item ng-scope" bindonce="" ng-repeat="assignee in possibleAssignees | filter:assigneeSearch track by assignee.login" ng-class="{highlight: ($index === 0 &amp;&amp; assigneeSearch)}" ng-click="setAssignees(assignee)">
      <i ng-class="{'is-picked': isActiveAssignee(assignee)}" class="fa fa-check checkmark"></i>
      <img bo-src="assignee.avatarUrl" class="img-circle" src="https://avatars3.githubusercontent.com/u/3470466?v=4">
      <span class="user-login" bo-text="assignee.login">matallen</span>
    </li>
  </ul>
  <button class="close-btn btn" ng-click="close()">Close</button>
</div>
-->
