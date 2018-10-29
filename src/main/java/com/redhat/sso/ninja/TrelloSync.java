package com.redhat.sso.ninja;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jayway.restassured.specification.RequestSpecification;
import com.redhat.sso.ninja.utils.MapBuilder;

import groovy.lang.GroovyClassLoader;
import mjson.Json;


/**
 * 
 * THIS IS NOT USED, IT'S AN EXAMPLE OF HOW JAVA IMPLEMENTATION INTEGRATIONS COULD BE DONE
 *
 */

public class TrelloSync extends ScriptBase{
  private static final Logger log=Logger.getLogger(TrelloSync.class);
  
  /*
   * 
   * Developed using these urls:
   *    https://developers.trello.com/reference/#search
   *    https://developers.trello.com/page/authorization
   *    https://github.com/redhat-cop/ninja-points/blob/master/trello-stats.py
   *    https://github.com/redhat-cop/ninja-points/blob/master/github-stats.py
   *    https://mojo.redhat.com/groups/paas-community-of-practice/projects/communities-of-practice-ninja-program
   *    
   * Ninja Submission Form & sheet
   *    https://docs.google.com/spreadsheets/d/1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ/edit#gid=2111805875
   *    
   *    
   *  also useful if we initiate with google sheet submissions
   *    http://thisdavej.com/consuming-json-web-data-using-google-sheets/
   * 
   */
  static String GET_ORGANIZATIONS="https://api.trello.com/1/organizations/%s"; // orgname
  static String GET_ORGANIZATION_MEMBERS="https://api.trello.com/1/organizations/%s/members";
  static String GET_MEMBER="https://api.trello.com/1/members/%s";
  static String SEARCH_CARDS="https://api.trello.com/1/search";
  
  
  static String orgId="redhatcop";
  static String key="39cca1583d34154be24aa99ee024106d";
  static String token;
//  static String POOL_NAME="Trello";
  
  public static void main(String[] asd){
    new TrelloSync().execute("test", new MapBuilder<String,String>().put("organizationName", "redhatcop").build(), 30, new PointsAdder(){
      public void addPoints(String username, String pool, Integer increment, String sourceEntityId){
        System.out.println("AddPoints called ["+pool+"/"+username+"] Incrementing "+increment+" points");
      }
    });
  }
  
  @Override
  public void execute(String name, Map<String,String> options, Integer daysFromLastRun, PointsAdder adder){
    
    if (StringUtils.isEmpty(System.getenv("TRELLO_TOKEN"))){
      log.error("No \"TRELLO_TOKEN\" Environment property set");
      return;
    }
    token=System.getenv("TRELLO_TOKEN");
    
//    TrelloSync ts=new TrelloSync();
    parse(options.get("organizationName"), daysFromLastRun, null);
    
//    System.out.println("CARD COUNT:");
    log.debug("Found cards for "+cardCount.size()+" individual people");
    for(Entry<String, Integer> e:cardCount.entrySet()){
//      System.out.println("["+e.getKey()+"] = "+e.getValue());
    }
//    System.out.println("");
//    System.out.println("POINTS:");
    
//    Map<String, String> trelloIdToUser=getUsersBy("trelloId");
    
    log.debug("Found points statistics for "+pointsStats.size()+" individual people");
    for(Entry<String, Integer> e:pointsStats.entrySet()){
      String trelloUserId=e.getKey();
//      String userId=trelloIdToUser.get(trelloUserId);
//      log.debug("trelloId is "+trelloUserId+", dereferenced to "+userId);
//      if (userId!=null){ // a null userid means they're most likely not registered
        Integer increment=e.getValue();
//        log.debug("Before calling adder.addPoints('"+trelloUserId+"','"+name+"','"+increment+"')");
        adder.addPoints(trelloUserId, name, increment, "not used");
  //      System.out.println("["+e.getKey()+"] = "+e.getValue());
//      }
    }
  }
  
  private Map<String, Integer> cardCount=new HashMap<String, Integer>();
  private Map<String, Integer> pointsStats=new HashMap<String, Integer>();
  private Map<String, String> memberIdMapping=new HashMap<String, String>();

  public void parse(String orgName, Integer days, String author){
//    TrelloSync s=new TrelloSync();
    String orgId=getOrganization(orgName).at("id").asString();
    log.debug("Found Trello organisation id: "+orgId);
    for(Json member:getOrganisationMembers(orgId).asJsonList())
      memberIdMapping.put(member.at("id").asString(), member.at("username").asString());
    Json cards=searchCards(orgId, days, author);
//    System.out.println("CARDS:\n"+cards.toString());
    
    List<Json> cardsList=cards.at("cards").asJsonList();
    log.debug("Found "+cardsList.size()+" trello cards to process into points");
    for (Json card:cardsList){
      
      String cardId=card.at("id").asString();
      String cardName=card.at("name").asString();
      
      String boardName=card.at("board").at("name").asString();
      String boardOrganizationId=null;
      if (!card.at("board").at("idOrganization").isNull())
        boardOrganizationId=card.at("board").at("idOrganization").asString();
      String boardId=card.at("board").at("id").asString();
      
      for(Json member:card.at("idMembers").asJsonList()){
        String memberId=member.asString();
        
        if (!memberIdMapping.containsKey(memberId)) // an on-demand grab for the user details for an userId that isn't in the orgs members list (for some reason users not in the private org can be on cards!?)
          memberIdMapping.put(memberId, getMember(memberId).at("username").asString());
        
        String memberUsername=memberIdMapping.get(memberId);
        String userKey=memberUsername;
        
        if (!cardCount.containsKey(userKey)) cardCount.put(userKey, 0);
        if (!pointsStats.containsKey(userKey)) pointsStats.put(userKey, 0);
        
        cardCount.put(userKey, cardCount.get(userKey)+1);
//        pointsStats.put(userKey, pointsStats.get(userKey)+calculatePoints(cardName));
        
        Integer points=calculatePoints(card);
        if (points>0){
          log.debug("["+userKey+"] Awarded "+points+" points");
          pointsStats.put(userKey, pointsStats.get(userKey)+points);
        }
        
      }
    }
  }
  
  @SuppressWarnings({ "unchecked", "resource", "rawtypes" })
  private Integer calculatePoints2(Json card){
    try{
//      Class scriptClass = new GroovyScriptEngine(".").loadScriptByName("calculatePoints.groovy") ;
      
      Class scriptClass = new GroovyClassLoader().parseClass( new File("src/main/resources/calculatePoints.groovy"));
      Object scriptInstance = scriptClass.newInstance() ;
      return (Integer)scriptClass.getDeclaredMethod("calculate", new Class[]{Json.class}).invoke( scriptInstance, new Json[]{card});
    }catch(Exception e){
      e.printStackTrace();
    }
    return 0;
  }
  
  private Integer calculatePoints(Json card){
    String cardName=card.at("name").asString();
    Matcher m=Pattern.compile("\\(([0-9]+)\\)").matcher(cardName);
    if (m.find()){
      return Integer.parseInt(m.group(1));
    }else
      return 1;
    
  }
  
  private RequestSpecification givenWithCredentials(){
    return given()
    .param("key", key)
    .param("token", token);
  }
  
  private Json getMember(String userId){
    return 
        mjson.Json.read(
          givenWithCredentials()
          .get(String.format(GET_MEMBER, userId)).thenReturn().asString()
          )
        ;
  }
  private Json getOrganisationMembers(String orgId){
    return 
        mjson.Json.read(
          givenWithCredentials()
          .get(String.format(GET_ORGANIZATION_MEMBERS, orgId)).thenReturn().asString()
          )
        ;
  }
  
  private Json getOrganization(String orgName){
    return 
        mjson.Json.read(
          givenWithCredentials()
          .get(String.format(GET_ORGANIZATIONS, orgName)).thenReturn().asString()
          )
        ;
  }
  
  private Json searchCards(String orgId, Integer days, String author){
    String query=String.format("list:Done edited:%s%s", days, author!=null && !"".equals(author)?" @"+author:"");
    return
        mjson.Json.read(
          givenWithCredentials()
          .param("query", query)
          
          .param("idOrganizations", orgId)
          .param("modelTypes", "cards")
          .param("board_fields", "name,idOrganization")
          .param("card_fields", "name,idMembers")
          .param("cards_limit", 1000)
          .param("card_board", "true")
          .param("organization_fields", "id,name,displayName")
          
//          .param("idOrganizations", orgId)
//          .param("card_fields", "name,idMembers")
////          .param("board_fields", "name,idOrganization")
//          .param("card_board", "true")
//          .param("cards_limit", 1000)
////          .param("boards_limit", 1000)
//          .param("cards_page", 1000)
////          .param("card_members", "true")
          
          .get(String.format(SEARCH_CARDS)).thenReturn().asString()
          )
        ;
  }

  
//  public void run(){
//    Response response =
//        givenWithCredentials()
//    .when().auth().preemptive()
//    .basic(username, password)
////    .basic("sa_solution_tracker", "17s*df8c7Q8cnD(d")
////    .header("sa_solution_tracker", "c2Ffc29sdXRpb25fdHJhY2tlcjoxN3MqZGY4YzdROGNuRChk")
//    .get(url)
//    .thenReturn()
//    ;
//    
//    String strResponse=response.asString();
//    
//  }
}
