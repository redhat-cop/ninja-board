package com.redhat.sso.ninja;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Lists;
import com.redhat.sso.ninja.SupportController.Card;
import com.redhat.sso.ninja.SupportController.TrelloAPI;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.MapBuilder;
import com.redhat.sso.ninja.utils.RegExHelper;

@Path("/support")
public class SupportController {
  private static final Logger log=Logger.getLogger(SupportController.class);
  
  public static void main(String[] asd) throws JsonGenerationException, JsonMappingException, IOException, ParseException, URISyntaxException{
  	
  	
//  	System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).parse("2019-09-17T20:11:17.091Z"));
//  	if (true) System.exit(0);
  	
  	SupportController test=new SupportController(){
  		public String getTrelloApiKey(){return "39cca1583d34154be24aa99ee024106d";}
  		public String getTrelloApiToken(){return "85e332445c4d221e09ecd0d1309a795c4f6aa0b99249852d896d3cee1f235610";}
  	};
  	
  	
//	test.trelloCard("mallen", "6ORZzITs", null, null);
  	System.out.println(test.trelloCards("mallen", "redhatcop", "Done", 281));
  	
  }
  
  
  class TrelloAPI{
  	private String key,token;
  	private String _organizationId;
  	public TrelloAPI(String key, String token){
  		this.key=key;
  		this.token=token;
  	}
  	
  	public void initialize(String orgName){
  		String url=String.format("https://api.trello.com/1/organizations/%s?key=%s&token=%s", orgName, key, token);
  		com.redhat.sso.ninja.utils.Http.Response r=Http.get(url);
  		if (r.getResponseCode()==200){
  			mjson.Json j=mjson.Json.read(r.getString());
  			_organizationId=j.at("id").asString();
  		}else{
  			throw new RuntimeException("HTTP Error "+r.getResponseCode()+" for url: "+url);
  		}
  	}
  	private void checkPreconditions(){
  		if (this._organizationId==null) throw new RuntimeException("run Initialize(orgName) first");
  	}
  	public List<Board> getBoards(){
  		checkPreconditions();
  		com.redhat.sso.ninja.utils.Http.Response r=Http.get(String.format("https://api.trello.com/1/organizations/%s/boards?key=%s&token=%s", _organizationId, key, token), new MapBuilder<String,String>().put("fields", "name,shortUrl").put("filter", "open").build());
  		if (r.getResponseCode()==200){
  			List<Board> result=new ArrayList<SupportController.Board>();
  			mjson.Json j=mjson.Json.read(r.getString());
  			for(mjson.Json i:j.asJsonList()){
  				Board b=new Board();
  				b.id=i.at("id").asString();
  				b.name=i.at("name").asString();
  				b.shortUrl=i.at("shortUrl").asString();
  				result.add(b);
  			}
  			return result;
  		}else
  			throw new RuntimeException("trello api to get boards failed");
  	}
  	
  	public Card getCard(String cardShortId){
  		String url="https://api.trello.com/1/cards/%s?key=%s&token=%s"+
  				"&board=true&board_fields=name,idOrganization,url,shortLink"+
  				"&members=true&member_fields=id,fullName,username"+
  				"&actions=updateCard&action_fields=type,date,data"+
  				"&list=true&list_fields=id,name"+
  				"";
  		com.redhat.sso.ninja.utils.Http.Response r=Http.get(String.format(url, cardShortId, key, token), null);
  		if (r.getResponseCode()==200){
  			mjson.Json j=mjson.Json.read(r.getString());
  			Card c=new Card();
  			c.id=j.at("id").asString();
  			c.name=j.at("name").asString();
  			c.shortId=j.at("shortLink").asString();
//  			c.boardId=j.at("board").at("id").asString();
//  			c.boardShortId=j.at("board").at("shortLink").asString();
//				c.boardName=j.at("board").at("name").asString();
////				c.author=author;
//				c.listId=j.at("list").at("id").asString();
//				c.listName=j.at("list").at("name").asString();
				
  			c=populateExpectedPoints(c);
				c=populateBoard(url,c,j);
				c=populateMembers(url,c,j);
				c=populateList(url,c,j);
				return c;
  		}else
  			throw new RuntimeException("trello api to get card failed");
  	}
  	
  	public Card populateList(String url, Card card, mjson.Json j){
  		if (url.contains("&list=true")){
  			List<String> fields=Lists.newArrayList(RegExHelper.extract(url, "&list_fields=(.+?)($|&)", 1).split(","));
				if (fields.contains("id"))  card.listId=j.at("list").at("id").asString();
				if (fields.contains("name"))  card.listName=j.at("list").at("name").asString();
  		}
  		return card;
  	}
  	public Card populateBoard(String url, Card card, mjson.Json j){
  		if (url.contains("&board=true")){
  			List<String> fields=Lists.newArrayList(RegExHelper.extract(url, "&board_fields=(.+?)($|&)", 1).split(","));
  			if (fields.contains("id")) card.boardId=j.at("board").at("id").asString();
  			if (fields.contains("shortLink")) card.boardShortId=j.at("board").at("shortLink").asString();
  			if (fields.contains("name")) card.boardName=j.at("board").at("name").asString();
  		}
			return card;
  	}
  	public Card populateMembers(String url, Card card, mjson.Json j){
  		if (url.contains("&members=true")){
  			List<String> fields=Lists.newArrayList(RegExHelper.extract(url, "&member_fields=(.+?)($|&)", 1).split(","));
  			card.members=new ArrayList<Member>();
				for(mjson.Json m:j.at("members").asJsonList()){
					Member member=new Member();
					if (fields.contains("id")) member.id=m.at("id").asString();
					if (fields.contains("fullName")) member.name=m.at("fullName").asString();
					if (fields.contains("username")) member.username=m.at("username").asString();
					card.members.add(member);
				}
  		}
			return card;
  	}
  	
  	
  	public Activity getCardActivity(String cardShortId) throws ParseException{
//  		checkPreconditions();
  		Activity result=new Activity();
  		com.redhat.sso.ninja.utils.Http.Response r=Http.get(String.format("https://api.trello.com/1/cards/%s/actions?key=%s&token=%s", cardShortId, key, token), null);
//  		System.out.println("getCardActivity() responseCode = "+r.getResponseCode());
  		if (r.getResponseCode()==200){
  			SimpleDateFormat sdfInput=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  			SimpleDateFormat sdfOutput=new SimpleDateFormat("yyyy-MM-dd");
  		
  			mjson.Json j=mjson.Json.read(r.getString());
  			for(mjson.Json i:j.asJsonList()){
  				String type=i.at("type").asString();
  				if ("updateCard".equals(type)){
  					mjson.Json data=i.at("data");
  					if (data.has("listAfter") && "done".equals(data.at("listAfter").at("name").asString().toLowerCase())){
  						result.isDone=true;
  						result.whenDone=sdfOutput.format(sdfInput.parse(i.at("date").asString()));
  					}
  				}
  			}
  		}
  		return result;
  	}
  	
  	public List<Card> getCards(String listNullForAll, int daysOld){
  		return getCards(listNullForAll, daysOld, null);
  	}
  	public List<Card> getCards(String listNullForAll, int daysOld, String author){
  		checkPreconditions();
  		String query=String.format("%s edited:%s %s", (listNullForAll!=null?"list:"+listNullForAll:""), daysOld, author!=null?"@"+author:"");
  		System.out.println("getCards query = "+query);
//  		Map<String,String> headers=new MapBuilder<String,String>()
//  				.put("query", query)
//  				.put("idOrganizations", _organizationId)
//  				.put("card_fields", "name,idBoard,idMembers,idLabels,shortLink")
////  				.put("board_fields", "name,idOrganization")
////  				.put("card_board", "true")
//  				.put("cards_limit","1000")
//  				
//  				.put("key", key)
//  				.put("token", token)
//  				
//  				.build();
  		com.redhat.sso.ninja.utils.Http.Response r=Http.get(String.format(
  				"https://api.trello.com/1/search?key=%s&token=%s&query=%s"+
  				"&card_board=true&board_fields=name,shortLink"+
  				"&card_members=true&member_fields=id,username,fullName"+
  				"&card_list=true"+
  				"&cards_limit=1000&idOrganizations=%s",
  				key, token, URLEncoder.encode(query), _organizationId));
  		System.out.println("getCards() responseCode = "+r.getResponseCode());
  		if (r.getResponseCode()==200){
  			List<Card> result=new ArrayList<SupportController.Card>();
  			mjson.Json j=mjson.Json.read(r.getString());
  			
  			for(mjson.Json i:j.at("cards").asJsonList()){
//  				Card c=new Card(i.at("id").asString(), i.at("name").asString(), i.at("shortLink").asString());
  				Card c=new Card();
  				c.id=i.at("id").asString();
  				c.name=i.at("name").asString();
  				c.shortId=i.at("shortLink").asString();
  				c.boardId=i.at("board").at("id").asString();
  				c.boardShortId=i.at("board").at("shortLink").asString();
  				c.boardName=i.at("board").at("name").asString();
  				c.trelloId=author;
  				c.listId=i.at("list").at("id").asString();
  				c.listName=i.at("list").at("name").asString();
  				
  				try{
  					Activity activity=getCardActivity(c.shortId);
  					c.completed=activity.isDone;
  					c.completedDate=activity.whenDone;
  				}catch(Exception e){
  					e.printStackTrace();
  				}
  				
  				c.members=new ArrayList<Member>();
  				for(mjson.Json m:i.at("members").asJsonList()){
  					Member member=new Member();
  					member.id=m.at("id").asString();
						member.name=m.at("fullName").asString();
						member.username=m.at("username").asString();
  					c.members.add(member);
  				}
  				result.add(c);
  			}
  			return result;
  		}else
  			throw new RuntimeException("trello api to get cards failed");
  	}
  }
  
  class Activity{
  	private boolean isDone;
  	private String whenDone;
  	public Boolean isDone(){ return isDone; }
  	public String getWhenDone(){ return whenDone; }
  	
  }
  class Board{
  	private String id;
  	private String name;
  	private String shortUrl;
  	
  }
  class Card{
//  	public Card(String id, String name, String shortLink){
//  		this.id=id; this.name=name; this.shortLink=shortLink;
//  	}
  	private String id,name,shortId,boardId,boardShortId,boardName,trelloId,expectedPoints,listId,listName;
  	private boolean completed,hasDupeRecord;
  	private String completedDate;
  	private List<Member> members;
  	public String getId(){ return id; }
  	public String getName(){ return name; }
  	public String getShortId(){ return shortId; }
  	public String getBoardId(){ return boardId; }
  	public String getBoardShortId(){ return boardShortId; }
  	public String getBoardName(){ return boardName; }
  	public String getTrelloId(){ return trelloId; }
  	public String getExpectedPoints(){ return expectedPoints; }
  	public String getListId(){ return listId; }
  	public String getListName(){ return listName; }
  	public boolean getCompleted(){ return completed; }
  	public boolean getHasDupeRecord(){ return hasDupeRecord; }
  	public String getCompletedDate(){ return completedDate; }
  	public List<Member> getMembers(){ return members; }
  	
  }
  class Member{
  	private String id,name,username;
  	public String getId(){ return id; }
  	public String getName(){ return name; }
  	public String getUsername(){ return username; }
  }

//  @GET
//	@Path("/trello/card/{cardId}")
//	public Response trelloCard(@PathParam("cardId") String cardId, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException, ParseException{
//  	log.info("/trello/card/"+cardId);
//  	
//  	TrelloAPI t=new TrelloAPI(System.getenv("TRELLO_API_KEY"), System.getenv("TRELLO_API_TOKEN"));
//  	t.initialize("redhatcop");
//  	Card card=t.getCard(cardId);
//  	Activity activity=t.getCardActivity(card.shortId);
//  	
//  	card.completed=activity.isDone;
//  	card.completedDate=activity.whenDone;
//  	
//  	return Response.status(200)
//        .header("Access-Control-Allow-Origin",  "*")
//        .header("Content-Type","application/json")
//        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
//        .header("Pragma", "no-cache")
////        .entity(Json.newObjectMapper(true).writeValueAsString(new MapBuilder<String,Object>().put("card", card).put("activity", activity).build())).build();
//        .entity(Json.newObjectMapper(true).writeValueAsString(new MapBuilder<String,Object>().put("card", card).build())).build();
//  }
  
  public String getTrelloApiKey(){
  	return System.getenv("TRELLO_API_KEY");
  }
  public String getTrelloApiToken(){
  	return System.getenv("TRELLO_API_TOKEN");
  }

  public Card populateDupeCheck(Card card, String trelloId){
  	System.out.println("checking for dupe: "+"TR"+card.id+"."+trelloId);
		if (Database2.get().getPointsDuplicateChecker().contains("TR"+card.id+"."+trelloId))
			card.hasDupeRecord=true;
		return card;
  }
  public Card populateExpectedPoints(Card card){
		String expectedPoints=RegExHelper.extract(card.name, "\\(([0-9]+)\\)");
		card.expectedPoints=expectedPoints!=null?expectedPoints:"1";
		return card;
  }

  
  @GET
	@Path("/trello/{username}/card/{cardShortId}")
	public Response trelloCard(@PathParam("username") String redHatUserId, @PathParam("cardShortId") String cardShortId, @Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
  	redHatUserId=redHatUserId.toLowerCase();
  	log.info("/trello/"+redHatUserId+"/card/"+cardShortId);
  	
  	Database2 dbUsers=Database2.get();
  	Map<String, String> userInfo=dbUsers.getUsers().get(redHatUserId);
  	
  	if (null==userInfo) return Response.serverError().entity("User '"+redHatUserId+"' not found in database").build();
  	
  	String trelloId=userInfo.get("trelloId");
  	
  	TrelloAPI t=new TrelloAPI(getTrelloApiKey(), getTrelloApiToken());
  	Card card=t.getCard(cardShortId);
  	card.trelloId=trelloId;
  	
  	card=populateDupeCheck(card, trelloId);
  	
		try{
			Activity activity=t.getCardActivity(cardShortId);
			card.completed=activity.isDone;
			card.completedDate=activity.whenDone;
		}catch(Exception e){
			e.printStackTrace();
		}
		
  	return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(Lists.newArrayList(card))).build();
  }

  @GET
	@Path("/trello/{username}/cards")
	public Response trelloCards(@PathParam("username") String username, @Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
  	username=username.toLowerCase();
  	log.info("/trello/"+username+"/cards/");
  	
  	String daysOld=request.getParameter("daysOld");
  	String list=request.getParameter("list");
  	String org=request.getParameter("org");
  	
  	TrelloAPI t=new TrelloAPI(getTrelloApiKey(), getTrelloApiToken());
  	t.initialize(org);
  	List<Card> cards=trelloCards(username, org, list, Integer.parseInt(daysOld));
  			
  	return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(cards)).build();
  }
  
  private List<Card> trelloCards(String username, String org, String listName, int daysOld){
  	if (null==org) throw new RuntimeException("Mandatory 'org' parameter missing. Should be Trello Organization");
  	
  	Database2 dbUsers=Database2.get();
  	Map<String, String> userInfo=dbUsers.getUsers().get(username);
  	String trelloId=userInfo.get("trelloId");
  	
  	TrelloAPI t=new TrelloAPI(getTrelloApiKey(), getTrelloApiToken());
  	t.initialize(org);
  	List<Card> cards=t.getCards(listName, daysOld, trelloId);
  	
  	// attach event searches
		Database2 db=Database2.get();
		List<Map<String,String>> userEvents=new ArrayList<Map<String,String>>();
		for(Map<String, String> e:db.getEvents()){
			if ("points increment".equalsIgnoreCase(e.get("type")) && username.equals(e.get("user"))){
				userEvents.add(e);
			}
		}
		for(Card card:cards){
			card=populateExpectedPoints(card);
			card=populateDupeCheck(card, trelloId);
		}
		return cards;
  }
  
	@GET
	@Path("/user/{userId}")
	public Response userSupport(@PathParam("userId") String redHatUserId, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("/support/user/"+redHatUserId);
//		String uri=IOUtils.toString(request.getInputStream());
		
		Database2 db=Database2.get();
		Map<String, Map<String, String>> dbUsers=db.getUsers();
		
		Map<String,Object> result=new HashMap<String, Object>();
		// lookup user info + registration date
		
		
		Map<String, String> dbUserInfo=dbUsers.get(redHatUserId);
		if (null==dbUserInfo) return Response.serverError().entity("User '"+redHatUserId+"' not found in database").build();
  	
		try{
			Map<String, Map<String, String>> users=new Heartbeat2.HeartbeatRunnable(null).getUsersFromRegistrationSheet(Config.get());
			for (Entry<String, Map<String, String>> e:users.entrySet()){
				if (redHatUserId.equals(e.getKey())){
					
					Map<String, String> regInfo=e.getValue();
					Map<String, String> userInfo=dbUsers.get(e.getKey());
					Map<String, Integer> userPoints=db.getScoreCards().get(e.getKey());
					
					result.put("username", e.getKey());
					result.put("displayName", userInfo.get("displayName"));
					result.put("geo", userInfo.get("geo"));
					result.put("level", userInfo.get("level"));
					result.put("levelChanged", userInfo.get("levelChanged"));
					result.put("email", userInfo.get("email"));
					result.put("date_registered", regInfo.get("reg"));
					result.put("trello_username", regInfo.get("trelloId"));
					result.put("github_username", regInfo.get("githubId"));
					result.put("gitlab_username", e.getKey());
					result.put("points", userPoints);
//					result.put("user_exists_in_db", String.valueOf(dbUsers.containsKey(e.getKey())));
				}
				
			}
			
			
		}catch(IOException e){
		}catch (InterruptedException e){
		}
		
		if (result.size()>0){
			return Response.status(200)
					.header("Access-Control-Allow-Origin",  "*")
					.header("Content-Type","application/json")
					.header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
					.header("Pragma", "no-cache")
					.entity(Json.newObjectMapper(true).writeValueAsString(result)).build();
		}else{
			return Response.status(400)
					.header("Access-Control-Allow-Origin",  "*")
					.header("Content-Type","application/json")
					.header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
					.header("Pragma", "no-cache")
					.entity(Json.newObjectMapper(true).writeValueAsString("User '"+redHatUserId+"' NOT found")).build();
			
		}
		
	}
	@GET
	@Path("/logout")
	public Response logout(@Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("/logout");
		request.getSession().setAttribute("x-access-token", null);
		request.getSession().invalidate();
		return Response.status(302).location(new URI("../index.jsp")).build();
	}
	
}