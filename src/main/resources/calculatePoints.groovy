import java.util.regex.*;
import mjson.Json;

def calculate(Json card){
  def cardName=card.at("name").asString();
  Matcher m=Pattern.compile("\\(([0-9]+)\\)").matcher(cardName);
  if (m.find()){
    return Integer.parseInt(m.group(1));
  }else
    return 1;
}