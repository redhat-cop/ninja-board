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
    <link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet">

    <style>
        body {
            font-family: "Lato";
            margin: 0;
            padding: 0;
        }
        #myKanban {
            overflow-x: auto;
            padding: 20px 0;
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

<input type="text" id="newItem" /><button disabled id="addToDo">Add</button>

<!--
<script src="https://raw.githack.com/riktar/jkanban/master/dist/jkanban.min.js"></script>
-->
<script src="js/jkanban.js"></script>

<style>
.kanban-item{
  padding: 7px !important;
  box-sizing: border-box;
  overflow: auto;
}
.kanban-item-delete{
	float: right;
	overflow: auto;
}
.kanban-item-title{
	float: left;
	overflow: auto;
}
/*

.clearfix {
  overflow: auto;
}
.clearfix::after {
  content: "";
  clear: both;
  display: table;
}
*/
</style>

<script>

function deleteIt(el){
	console.log(el.dataset.eid);
	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.eid+"/delete", null, function(response){
		// update board to show removal of task?
				//KanbanTest.refresh();
				KanbanTest.removeElement(el.dataset.eid);
	});
}

$(document).ready(function() {
	Http.httpGet("${pageContext.request.contextPath}/api/tasks", function(response){
		
		var j=JSON.parse(response);
		var todo=j.todo;
		var working=j.working;
		var done=j.done;
		
		
		//console.log("response="+j);
		//console.log("response.todo="+j.todo);
		//$('#alertsEnabled').prop("checked", "true"==response.toLowerCase());
		
		
    var KanbanTest = new jKanban({
        element: '#myKanban',
        gutter: '10px',
        widthBoard: '450px',
        dragBoards: false,
        deleteCards: true,
        customDisplay: function(boardId, el, data){
        	console.log("customDisplay():: el="+JSON.stringify(el.dataset));
        	
        	
        	return "<table><tr><td>"+data.title+"</td></tr><tr><td>"+data.user+"</td></tr><tr><td>"+data.timestamp.substring(0,10)+"</td></tr></table>";
        	
        	return "<table><tr><td>"+data.title+"</td></tr><tr><td>"+data.timestamp.substring(0,10)+"</td></tr></table>";
        	//return data.title;
        },
        //	console.log("X="+JSON.stringify(element));
        //	//return "<table style='position:relative;top:-25px;' border=0 width=100%><tr><td>"+element.title+"</td><td rowspan='3' valign='top' align='center'></td></tr><tr><td>"+element.user+"</td></tr><tr><td>"+element.timestamp+"</td></tr></table>";
        //	//return "<table border=0 width=100%><tr><td>"+element.title+"</td><td rowspan='3' valign='top' align='center'><button onclick='return deleteIt("+element.id+");'>X</button></td></tr><tr><td>"+element.user+"</td></tr><tr><td>"+element.timestamp+"</td></tr></table>";
        //	
        //	return "<table border=0 width=100%><tr><td>"+element.title+"</td><td rowspan='3' valign='top' align='center'><button onclick='return deleteIt("+element.id+");'>X</button></td></tr><tr><td>"+element.timestamp+"</td></tr></table>";
        //	
        //	
        //	//return "<table><tr><td>"+element.title+"</td></tr><tr><td>"+element.user+"</td></tr><tr><td>"+element.timestamp+"</td></tr></table>";
        //	
        //	//return "<table><tr><td>"+element.title+"</td></tr><tr><td>"+element.timestamp+"</td></tr></table>";
        //	//return element.title;
        //},
        onDelete: function(el){
        	console.log("onDelete():: el="+JSON.stringify(el.dataset));
        	Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.eid+"/delete", null, function(response){
       				KanbanTest.removeElement(el.dataset.eid);
        	});
        },
        click: function (el) {
        	console.log("onClick: "+el.dataset.eid);
        	//
        	//Http.httpPost("${pageContext.request.contextPath}/api/tasks/"+el.dataset.eid+"/delete", null, function(response){
        	//	// update board to show removal of task?
        	//});
        	
            //console.log("Trigger on all items click!");
        },
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
    
    $(document).on('click', "#newItem", function() {
			document.getElementById("addToDo").disabled=document.getElementById("newItem").value.length>0;
		});
    
    var toDoButton = document.getElementById('addToDo');
    toDoButton.addEventListener('click', function () {
    	  var newItem={"title":document.getElementById("newItem").value};
    	  console.log("item text = "+newItem);
        
        Http.httpPost("${pageContext.request.contextPath}/api/tasks", newItem, function(response){
    	  document.getElementById("newItem").value="";
	        // if server created the task, then show it on the kanban board
        	KanbanTest.addElement(
	            "_todo",
	            {
	                "id": response,
	                "title": newItem.title,
	            }
	        );
        });
      
    });
	});
	
});




</script>
</body>
</html>