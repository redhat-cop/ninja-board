import mjson.Json

import java.util.regex.Matcher
import java.util.regex.Pattern;

def calculate(Json card){
  def cardName=card.at("name").asString();
  Matcher m=Pattern.compile("\\(([0-9]+)\\)").matcher(cardName);
  if (m.find()){
    return Integer.parseInt(m.group(1));
  }else
    return 1;
}