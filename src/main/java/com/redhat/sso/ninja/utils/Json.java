package com.redhat.sso.ninja.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class Json {

  public static ObjectMapper newObjectMapper(boolean pretty){
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,pretty);
    return mapper;
  }
}
