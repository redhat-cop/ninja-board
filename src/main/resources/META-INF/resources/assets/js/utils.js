Utils = {
	
	getParameterByName: function(name, url) {
		if (!url) url = window.location.href;
		name = name.replace(/[\[\]]/g, "\\$&");
		var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
		    results = regex.exec(url);
		if (!results) return undefined;
		if (!results[2]) return '';
		return decodeURIComponent(results[2].replace(/\+/g, " "));
	},
	
	getFragment: function (url){
		if (!url) url = window.location.href;
		var match=new RegExp("#(\\w*)", "ig").exec(url);
		if (undefined==match) return undefined;
		//console.log(url+" - fragment is "+match[1]);
		return match[1];
	},

	setFragment: function(replacement, url){
		var q="";
		if (!url) url = window.location.href;
		if (!url.includes("#") && (!url.includes("?"))) regex="$";
		//if (!url.includes("#") && (url.includes("?"))){ regex="(\\?)"; q="?"} // add it before the ? (this is technically incorrect URL syntax though where frags were before ?)
		if (!url.includes("#") && (url.includes("?"))){ regex="$";}
		if (url.includes("#") && (!url.includes("?"))){ regex="#(.*)";}
		//if (url.includes("#") && (url.includes("?"))){ regex="#(.*\\?)"; q="?"} // this went with the 'technicall incorrect URL syntax where frags were before ?
		//if (url.includes("#") && (url.includes("?"))){ regex="#(.*)"; q="?"}
		if (url.includes("#") && (url.includes("?"))){ regex="#(.*)";}
		return url.replace(new RegExp(regex,'g'),"#"+replacement+q);
	},
	
	removeFragment: function(url){
		if (!url) url = window.location.href;
		if (!url.includes("#")) return url;
		return url.replace(new RegExp("#.[^?]+",'ig'),""); // remove anything after a # and upto (but not including) a ? character
	},
	
//	replaceFragment: function(url, replacement){ // just a nicer name for the overloaded getFragment method
//		return getFragment(url, replacement);
//	}
	
	findAncestor: function findAncestor (el, cls) {
		while ((el = el.parentElement) && !el.classList.contains(cls));
		return el;
	},
	
	isNotBlank: function(value){
		return value!=undefined && value.length>0;
	},
	
	/* array object sort by field name 
	 * 
	 * var people=[{Name:"Fred",Surname:"Bloggs"}, {Name:"Adam",Surname:"Carter"}]
	 * people.sort(Utils.sortBy("Name", "-Surname"));
	 * 
	 * */
	sortBy: function() {
	    var props = arguments;
	    return function (obj1, obj2) {
	        var i = 0, result = 0, numberOfProperties = props.length;
	        while(result === 0 && i < numberOfProperties) {
	            result = Utils.sortBySingle(props[i])(obj1, obj2);
	            i++;
	        }
	        return result;
	    }
	},
	sortBySingle: function(property) {
	    var sortOrder = 1;
	    if(property[0] === "-") {
	        sortOrder = -1;
	        property = property.substr(1);
	    }
	    return function (a,b) {
	        return  sortOrder * ((a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0);
	    }
	},
	
	/*
	 * Remove duplicates from an array
	 * */
	removeDuplicates: function(array){
	    var set = array.filter((c, index) => {
	        return array.indexOf(c) === index;
	    });
	    return set;
	},
	
	/* returns a string that has no spaces and is lowercase, good for matching things such as complex sales play titles to solutions' sales play properties */
	toSafeString: function(value){
		if (undefined==value) return "";
		var v=value.includes("|")?value.split("|")[0]:value;
		return v.replace(/ /g, '_').replace(/\W/g, '').toLowerCase();
	},
	
	/* return an html encoded string, */
	toHtmlString: function(value){
		if (undefined==value) return "";
		//return value;
		var regexAsciiWhitelist = /[\x01-\x7F]/g;
		var escapeMap = {
				'"':  'quot',
				'&':  'amp',
				'\'': '#x27',
				'<':  'lt',
				'>':  'gt',
				'-':  '#8209', // dash
				//'`': '&#x60;'
			};
		value = value.replace(regexAsciiWhitelist, function($0) {
			if (undefined!=escapeMap[$0]){ // ie. the map contains the symbol extracted
				return '&' + escapeMap[$0] + ';';
			}
			return $0;
		});		
		return value;
	},
	
	replaceAll: function(str, find, replace){
		return str.replace(new RegExp(find, 'g'), replace);
	},
	
	defaultTo: function(str, d3fault){
		return Utils.isNotEmpty(str)?str:d3fault;
	},
	
	isEmpty: function(value){
		return value==undefined || value.trim()=="";
	},
	isNotEmpty: function(value){
		return value!=undefined && value.trim()!="";
	},
	
	// value is the field (formats include 'undefined', 'a plain string' or a 'name|url' pair)
	// target is the window target (ie. _blank) (optional)
	// style is the css style class (optional)
	toAnchor: function(value, target, cssStyle, showNewWindowIcon){
		if (undefined==value) return value;
		if (value.includes("|")){
			var nameUrl=Utils.toNameUrl(value);
			return `<a `+(undefined!=target?`target='`+target+`' `:``)+(undefined!=cssStyle?`style='`+cssStyle+`' `:``)+`href='`+nameUrl.url+`'>`+nameUrl.name.replace(/ /g, "&nbsp;")+(showNewWindowIcon?` <i class="fa-solid fa-up-right-from-square fa-2xs"></i>`:``)+`</a>`;
		}
		return value.replace(/ /g, "&nbsp;");
//		return value.replaceAll(" ","&nbsp;");
	},
	
	toNameUrl: function(value){ // extracts a compound field name/link. ie. Accelerate new app development|https://redhat.highspot.com/items/61df11cd66147fc6b86afded
		if (value==undefined) return {id:"",name:"",url:""};
		var result=value.includes("|")?{name:value.split("|")[0],url:value.split("|")[1]}:{name:value};
		if (result["url"]){
			try {
			  result["id"]=result["url"].match(/(?!.+\/)(\w+)/)[0]; // if there's a url, then we can extract the ID (a solution id)
			}catch(sink){
				result["id"]="";
			}
			//result["id"]=solutionresult["url"]?result["url"].substring(result["url"].lastIndexOf("/")+1):name.replace(/ /g, '_').replace(/\W/g, '').toLowerCase();
		}
		return result;
	},
	
	getParametersAsMap: function(url){
		if (!url) url = window.location.href;
        var map = [], hash;
        var hashes = url.slice(url.indexOf('?') + 1).split('&');
        for(var i = 0; i < hashes.length; i++){
            hash = hashes[i].split('=');
            map[hash[0]] = hash[1];
        }
        return map;
	},
	
	buildAttr: function(attrName, attrValue, d3fault){
		return (undefined!=attrValue && !Utils.isEmpty(attrValue)?` `+attrName+`="`+attrValue+`"`: (d3fault?` `+attrName+`="`+d3fault+`"`:``))
	},
	
}
Browser = {
		updateAddress: function(regexPattern, newValue, newTitle){
			// regex flags - https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#advanced_searching_with_flags
			if (undefined==newTitle) newTitle="";
			var newUrl=window.location.href.replace(new RegExp(regexPattern,"mi"), newValue);
			document.title=newTitle;
			window.history.pushState("", newTitle, newUrl);	
		},
		
		inject: function(elementType /*link or script*/, href,type/*text/css or javascript*/,rel){
			var head=document.getElementsByTagName("head")[0]
			var e=document.createElement(elementType);
			if (["script"].includes(elementType.toLowerCase())){
				e.src=href;
			}else{
				e.href=href;
			}
			if (type) e.type=type;
			if (rel)  e.rel=rel;
			head.appendChild(e);
		},
		injectCss: function(cssHref){
			Browser.inject("link",cssHref,"text/css","stylesheet");
		},
		injectScript: function(src){
			Browser.inject("script",src);
		},
		
		/* NO LONGER USED, SUPERCEEDED BY v2 below it that supports removal and hashes */
		updateQueryStringParameter: function(uri, key, value){
			var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
			var separator = uri.indexOf('?') !== -1 ? "&" : "?";
			if (uri.match(re)) {
			  return uri.replace(re, '$1' + key + "=" + value + '$2');
			}
			else {
			  return uri + separator + key + "=" + value;
			}
		},
		
		removeQueryStringParameter(url, id){
			return Browser.updateQueryStringParameter2(url, id, undefined);
		},
		
		updateQueryStringParameter2: function (url, key, value) {
		    if (!url) url = window.location.href;
		    var re = new RegExp("([?&])" + key + "=.*?(&|#|$)(.*)", "gi"),
		        hash;

		    if (re.test(url)) {
		        if (typeof value !== 'undefined' && value !== null)
		            return url.replace(re, '$1' + key + "=" + value + '$2$3');
		        else {
		            hash = url.split('#');
		            url = hash[0].replace(re, '$1$3').replace(/(&|\?)$/, '');
		            if (typeof hash[1] !== 'undefined' && hash[1] !== null) 
		                url += '#' + hash[1];
		            return url;
		        }
		    }
		    else {
		        if (typeof value !== 'undefined' && value !== null) {
		            var separator = url.indexOf('?') !== -1 ? '&' : '?';
		            hash = url.split('#');
		            url = hash[0] + separator + key + '=' + value;
		            if (typeof hash[1] !== 'undefined' && hash[1] !== null) 
		                url += '#' + hash[1];
		            return url;
		        }
		        else
		            return url;
		    }
		}
		
/*
		// split params & fragments, update uri (not server) and re-attach params
		updateAddress: function(newUrl, replaceHistory){
			// https://stackoverflow.com/questions/17507091/replacestate-vs-pushstate
			if (undefined!=replaceHistory && true==replaceHistory){
				//window.history.replaceState(data, title, url);
				window.history.replaceState("", "", newUrl);
			}else{
				window.history.pushState("", "", newUrl);
			}
		}

		// extract all the parts of the address so we can replace just parts of it
		getUri: function(url){
			var scheme=url.split("://")[0];
			var server=url.split("://")[1];
			var firstSlash=server.indexOf("/");
			server=server.substring(0, firstSlash);
			var uri=server.substring(firstSlash);
		}
*/
}

LocalStorage = {
		storageName:"RHServicesPortfolio",
		put: function(key, value) {
			console.log("LocalStorage::Put: "+key+"="+value+"");
			window.localStorage.setItem(LocalStorage.storageName+"_"+key, value);
		},
		get: function(key) {
			var result=window.localStorage.getItem(LocalStorage.storageName+"_"+key);
			if (result=="undefined") result=undefined; // localstorage cannot store anything but a string, including undefined values
			console.log("LocalStorage::Get: "+key+"="+result);
			return result;
		},
		remove: function(key) {
			console.log("LocalStorage::Remove: "+key);
			window.localStorage.removeItem(LocalStorage.storageName+"_"+key);
		},
}

function CopyEmbedUrlToClipboard(){
	// build embed link
	var url=window.location.href;
	if (url.indexOf("?")>0){ // if ? in url, inject /embed in the correct place
	  var baseUrl = url.substring(0, url.indexOf("?"));
	  baseUrl+="/embed";
	  url=baseUrl+url.substring(url.indexOf("?")) + "&controls=hide";
	}else // no ? in url, then just add the /embed
		url=url+"/embed?controls=hide";
	CopyToClipboard(url);
}
const CopyToClipboard = toCopy => {
    const el=document.createElement(`textarea`)
    el.value = toCopy
    el.setAttribute(`readonly`, ``)
    el.style.position = `absolute`
    el.style.left = `-9999px`
    document.body.appendChild(el)
    el.select()
    document.execCommand(`copy`)
    document.body.removeChild(el)
}

if (!String.prototype.format) {
  String.prototype.format = function() {
    var args = arguments;
    return this.replace(/{(\d+)}/g, function(match, number) { 
      return typeof args[number] != 'undefined'
        ? args[number]
        : match
      ;
    });
  };
}

if (!String.prototype.startsWith) {
	  String.prototype.startsWith = function(str, word) {
		  return str.lastIndexOf(word, 0) === 0;
	  }
}
