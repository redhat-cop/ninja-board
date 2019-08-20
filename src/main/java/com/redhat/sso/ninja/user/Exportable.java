package com.redhat.sso.ninja.user;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

public interface Exportable {

  @SuppressWarnings("unchecked")
  default Map<String, String> asHash() {
    return new ObjectMapper().convertValue(this, Map.class);
  }

}
