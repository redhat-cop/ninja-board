package com.redhat.sso.ninja.utils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Json
 */
public class Json {

    public static ObjectMapper newObjectMapper(boolean pretty){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT,pretty);
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }
}