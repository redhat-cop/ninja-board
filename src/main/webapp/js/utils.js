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
	
	findAncestor: function findAncestor (el, cls) {
		while ((el = el.parentElement) && !el.classList.contains(cls));
		return el;
	},
	
	isNotBlank: function(value){
		return value!=undefined && value.length>0;
	}
}