Http = {
	send: function(action, uri, data, callback){
	  var xhr = new XMLHttpRequest();
	  var ctx = "${pageContext.request.contextPath}";
	  var url=uri;
	  
	  xhr.open(action, url, true);
	  if (data != undefined){
	    xhr.setRequestHeader("Content-type", "application/json");
	    xhr.send(JSON.stringify(data));
	  }else{
	    xhr.send();
	  }
	  xhr.onloadend = function () {
		console.log("datatables-functions::send:: onloadend ... status = "+this.status);
		
	  	if (this.status == 200){
		  	console.log("datatables-functions::send:: returned 200");
	  	}else if(xhr.status>=400){
	  		
	  	}
	  	
	  	if (undefined!=callback){
	  		callback(xhr);
	  	}
	  };
	},
	httpPost: function(uri, data){
		return Http.send("POST", uri, data);
	},
	httpPost: function(uri, data, callback){
		return Http.send("POST", uri, data, callback);
	},
	httpDelete: function(uri, data){
		return Http.send("DELETE", uri, data);
	},
	httpDelete: function(uri, data, callback){
		return Http.send("DELETE", uri, data, callback);
	},
	httpGet: function(url, callback){
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		xhr.send();
		xhr.onloadend = function () {
		  callback(xhr.responseText);
		  //callback(JSON.parse(xhr.responseText));
		};
	}
}

