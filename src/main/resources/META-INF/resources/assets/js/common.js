function getTemplate(){return getPageOrTemplate(window.location.href,-1);}
function getPage(){return getPageOrTemplate(window.location.href,0);}
function getPage2(){return [getPageOrTemplate(window.location.href,-1), getPageOrTemplate(window.location.href,0)].join("/")}
function getPageOrTemplate(href,idxShift){
	if (undefined==href) href=window.location.href;
	href=href.includes("?")?href.substring(0,href.indexOf("?")):href; // strip after ? (params)
	href=href.includes("#")?href.substring(0,href.indexOf("#")):href; // strip after # (fragments)
	var a=href.split("/");a.shift();a.shift();a.shift(); // remove the "http://domain"
	return a[a.length-1+idxShift];
}

//function openNav(el,width)  { document.getElementById(el).style.width = "250px"; document.getElementById("main").style.marginLeft = "250px"; }
//function closeNav(el,width) { document.getElementById(el).style.width = "0";     document.getElementById("main").style.marginLeft= "0"; }
function openNav(el,width)  { document.getElementById(el).style.width = width+"px"; document.getElementById("main").style.marginLeft = width+"px"; }
function closeNav(el,width) { document.getElementById(el).style.width = width+"px"; document.getElementById("main").style.marginLeft = width+"px"; }
function hamburgerOnClick() {
	document.getElementById("hamburger").classList.toggle("change");
	if (document.getElementById("hamburger").classList.contains("change")){
		openNav("sidenav", 250);
		gaEntityClick("hamburger_open");
	}else{
		closeNav("sidenav", 0);
	}
}

function slideDown(el){
	document.getElementById(el).classList.toggle("change");
	if (document.getElementById(el).classList.contains("change")){
		document.getElementById("btn"+el).innerHTML=' <span><i class="fa-solid fa-sm fa-angles-up"></i></span>'; // close
	}else{
		document.getElementById("btn"+el).innerHTML=` <span><i class="fa-solid fa-sm fa-angles-down"></i></span>`; // close
	}
}
function filterOnClick() {
	document.getElementById("filterpanel").classList.toggle("change");
	if (document.getElementById("filterpanel").classList.contains("change")){ openFilter(); }else{ closeFilter(); }
}
function openFilter(){
	document.getElementById("filterpanel").classList.add("change");
	document.getElementById("filterpanel").style.height = document.getElementById("filterpanel").dataset["maxHeight"]; $("#filter-bar").css("display","initial");
	gaEntityClick("filter_open");
} 
function closeFilter() { document.getElementById("filterpanel").style.height = "38px";  $("#filter-bar").css("display","none"); } // "none" is to hide/show in case someone uses tab in the search box

function solutionTypeToName(type){
    var result=""; var type=Utils.toSafeString(type);
    result=""==result && ["journey"]           .includes(type)?"Services Map":result;
    result=""==result && [/*"journey",*/"solution"].includes(type)?"Consulting Solution":result;
    result=""==result && ["support"]           .includes(type)?"Services Support":result;
    result=""==result && ["training"]          .includes(type)?"Services Training":result;
    result=""==result && ["labs"]              .includes(type)?"Innovation Labs":result;
    result=""==result && ["assessment","healthcheck"]  .includes(type)?"Consulting Assessment":result;
    result=""==result && ["workshop","design"]         .includes(type)?"Consulting Workshop":result;
    result=""==result?"Unknown":result; // unknown type
    return result;
}
function solutionTypeToIcon(type){
    var icon=""; var type=Utils.toSafeString(type);
    icon=""==icon && ["journey"]                   .includes(type)?"fa-map":icon;
    icon=""==icon && [/*"journey",*/"solution"]    .includes(type)?"fa-suitcase":icon;
    //icon=""==icon && ["support"]                   .includes(type)?"fa-phone":icon;
    icon=""==icon && ["support"]                   .includes(type)?"fa-handshake":icon;
    icon=""==icon && ["training"]                  .includes(type)?"fa-graduation-cap":icon;
    icon=""==icon && ["labs"]                      .includes(type)?"fa-flask":icon;
    icon=""==icon && ["assessment","healthcheck"]  .includes(type)?"fa-list-check":icon;
    icon=""==icon && ["workshop","design"]         .includes(type)?"fa-person-chalkboard":icon; // fa-person-chalkboard
    icon=""==icon?"fa-circle-question":icon; // unknown type
    return icon;
}

function addSelectAllHandler(selectAllSelector, itemSelector){
	selectAllSelector=undefined!=selectAllSelector?selectAllSelector:".selectAll"; //defaults
	itemSelector=undefined!=itemSelector?itemSelector:".item-id"; //defaults
	$(selectAllSelector).click(function(){
		var checked=this.checked;
		$(itemSelector).each(function(){
			this.checked=checked;
		});
	})
}

Lists={
	stripEmptyItems: function(list){ return list.filter(i=>{if (typeof i == 'string') return i.trim()!==''; return false}) },
	stripNonBlankItems: function(list){ return list.filter(i=>{return !StringUtils.isBlank(i)}) },
	listToCommaString: function(items, max, separator){
		if (!separator) separator=", ";
		if (!max) max=9999;
		if (!items) return undefined;
		return items.split(",").map(x=>x.trim()).slice(0,max).join(separator); // maximum of "max" items using slice
	},
}

StringUtils={
	isBlank:function(v){return !v || v.trim()=='';}
}

//Predicates={
//	// TODO: this probably needs to be moved to a StringUtils common helper with a bunch of other methods dotted around the Hub code to consolidate it
//		displayCommaLists: function(items, max){
//			if (undefined==max) max=9999;
//			if (undefined==items) return undefined;
//			return items.split(",").map(x=>x.trim()).slice(0,max).join(", "); // maximum of "max" items using slice
//		}
//}

CommonUtils={
		displayAttributesBar:function(item, cfg){
			var attrs=``,maxCol=0,maxRow=0,mandatoryRowCount=0/*3*/,rowCounter={"1":0,"2":0}, hasMoreAttributes=false;
			for (f in cfg){
				if (undefined!=item[cfg[f].name]){
					var row=rowCounter[maxCol,cfg[f].col]+=1;
					attrs+=`<div class="solution-attribute" style="text-align:`+(cfg[f].col==1?`left`:`right`)+`;grid-column:`+cfg[f].col+`;grid-row:`+(attrs.includes("attributes-more")?row-mandatoryRowCount:row)+`;"><span class="solution-attribute-header">`+cfg[f].label+`:</span>&nbsp;<span id="solution-attribute-anchor" `+(cfg[f].title?` title="`+cfg[f].title(item[cfg[f].name])+`"`:``)+`>`+Utils.toAnchor(cfg[f].processor?cfg[f].processor(item[cfg[f].name]):item[cfg[f].name])+`</span></div>\n`;
					maxCol=Math.max(maxCol,cfg[f].col);
					maxRow=Math.max(maxRow,cfg[f].row);
				}
				if (!attrs.includes("attributes-more") && cfg[f].more){
					attrs+=`</div><div id="attributes-more" style="max-height: 0; overflow: hidden; transition: max-height 0.65s ease; grid-template-columns: 1fr 1fr; display:grid">`;
					hasMoreAttributes=true;
					rowCounter={"1":0,"2":0}
				}
			}
			attrs+=`</div><!--/attributes-more-->`;
			attrs=`<div style="padding-top:10px; display:grid; grid-template-columns:`+(new Array(maxCol+1).join(" 1fr"))+`;grid-template-rows:`+(new Array(Math.min(3,maxRow+1)).join(" 1fr"))+`">`+attrs+``;
			attrs+=`<div class="solution-attribute" style="padding-bottom: 10px; text-align: center;">`;
			if (hasMoreAttributes) attrs+=`<a id="btnattributes-more" href="#" onclick="slideDown('attributes-more'); return false;"><span><i class="fa-solid fa-sm fa-angles-down" style="width:50px;"></i></span></a>`;
			attrs+=`</div>`;
			return attrs;
		}
}
