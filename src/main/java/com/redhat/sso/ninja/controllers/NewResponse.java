package com.redhat.sso.ninja.controllers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class NewResponse{
  
  // common response created because post v79'ish of Chrome they introduced a SIGNED_EXCHANGE error without the following headers on every response
  public static ResponseBuilder status(int status){
    return Response.status(status)
     .header("Access-Control-Allow-Origin",  "*")
     .header("Content-Type","application/json")
     .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
     .header("Pragma", "no-cache")
     .header("X-Content-Type-Options", "nosniff");
  }

}
