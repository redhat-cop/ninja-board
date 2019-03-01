package com.redhat.sso.ninja.user;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.redhat.sso.ninja.Config;

public class UserService {

  private String getLDAPProvider(){
  	String providerString=Config.get().getOptions().get("users.ldap.provider");
  	if (null==providerString){
  		providerString=System.getenv("USERS_LDAP_PROVIDER");
  	}
  	return providerString;
  }
	
  public List<User> search(String field, String value) throws NamingException {
    Hashtable<String,String> env=new Hashtable<String,String>(3);
    
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, getLDAPProvider());
    env.put(Context.SECURITY_AUTHENTICATION, "none");

//    try {
      LdapContext ctx=new InitialLdapContext(env, null);
      ctx.setRequestControls(null);
//      String baseDN="ou=users,dc=redhat,dc=com";
//      String searchDN=String.format("(&(objectclass=Person)(%s=%s))",field, value);
      String baseDN=Config.get().getOptions().get("users.ldap.baseDN");
      String searchDN=String.format(Config.get().getOptions().get("users.ldap.searchDN"), field, value);
      NamingEnumeration<?> namingEnum=ctx.search(baseDN, searchDN, getSimpleSearchControls());
      List<User> result2=new ArrayList<UserService.User>();
      
      while (namingEnum.hasMore()) {
        SearchResult result=(SearchResult) namingEnum.next();
        Attributes attrs=result.getAttributes();
        
        // print all attribute names and values
//        Enumeration<? extends Attribute> e=attrs.getAll();
//        while (e.hasMoreElements()){
//          Attribute a=e.nextElement();
//          System.out.println(a.getID() +"="+a.get());
//        }
        
        User u=new User();
        u.uid           =safeToString(attrs.get("uid"));
        u.name          =safeToString(attrs.get("cn"));
        u.country       =safeToString(attrs.get("co"));
        u.employeeNumber=safeToString(attrs.get("employeeNumber"));
        u.mail          =safeToString(attrs.get("mail"));
        u.mobile        =safeToString(attrs.get("mobile"));
        u.location      =safeToString(attrs.get("rhatLocation"));
        u.jobTitle1     =safeToString(attrs.get("rhatJobTitle"));
        u.jobTitle2     =safeToString(attrs.get("title"));
        u.costCenter    =safeToString(attrs.get("rhatCostCenter"));
        u.costCenterDesc=safeToString(attrs.get("rhatCostCenterDesc"));
        u.geo           =safeToString(attrs.get("rhatGeo"));
        u.hireDate      =safeToString(attrs.get("rhatHireDate"));
        u.jobCode       =safeToString(attrs.get("rhatJobCode"));
        result2.add(u);
      }
      return result2;
      
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return new ArrayList<UserService.User>();
  }

  private String safeToString(Attribute a) throws NamingException{
    if (null!=a){
      Object o=a.get();
      return o.toString();
    }
    return "";
  }
  
  private SearchControls getSimpleSearchControls() {
    SearchControls sc=new SearchControls();
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    sc.setTimeLimit(30000);
    return sc;
  }

  public class User{
    private String uid;
    private String name;
    private String country;
    private String employeeNumber;
    private String mail;
    private String mobile;
    private String geo;
    private String costCenter;
    private String costCenterDesc;
    private String location;
    private String jobTitle1;
    private String jobTitle2;
    private String hireDate;
    private String jobCode;
    
    public String getUid()               {return uid;}
    public String getName()              {return name;}
    public String getCountry()           {return country;}
    public String getEmployeeNumber()    {return employeeNumber;}
    public String getMail()              {return mail;}
    public String getMobile()            {return mobile;}
    public String getRhatGeo()           {return geo;}
    public String getRhatCostCenter()    {return costCenter;}
    public String getRhatCostCenterDesc(){return costCenterDesc;}
    public String getRhatJobTitle()      {return jobTitle1;}
    public String getTitle()             {return jobTitle2;}
    public String getRhatLocation()      {return location;}
    public String getHireDate()          {return hireDate;}
    public String getJobCode()           {return jobCode;}
  }

}
