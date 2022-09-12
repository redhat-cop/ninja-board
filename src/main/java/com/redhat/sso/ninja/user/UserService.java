package com.redhat.sso.ninja.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import com.redhat.sso.ninja.utils.RegExHelper;

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
        u.userInfo.put("uid",            safeToString(attrs.get("uid")));
        u.userInfo.put("name",           safeToString(attrs.get("cn")));
        u.userInfo.put("country",        safeToString(attrs.get("co")));
        u.userInfo.put("employeeNumber", safeToString(attrs.get("employeeNumber")));
        u.userInfo.put("mail",           safeToString(attrs.get("mail")));
        u.userInfo.put("mobile",         safeToString(attrs.get("mobile")));
        u.userInfo.put("location",       safeToString(attrs.get("rhatLocation")));
        u.userInfo.put("jobTitle1",      safeToString(attrs.get("rhatJobTitle")));
        u.userInfo.put("jobTitle2",      safeToString(attrs.get("title")));
        u.userInfo.put("costCenter",     safeToString(attrs.get("rhatCostCenter")));
        u.userInfo.put("costCenterDesc", safeToString(attrs.get("rhatCostCenterDesc")));
        u.userInfo.put("geo",            safeToString(attrs.get("rhatGeo")));
        u.userInfo.put("hireDate",       safeToString(attrs.get("rhatHireDate")));
        u.userInfo.put("jobCode",        safeToString(attrs.get("rhatJobCode")));
        u.userInfo.put("manager",        safeToString(attrs.get("manager")));
        u.userInfo.put("user.manager",    RegExHelper.extract(attrs.get("manager").toString(), "uid=(.+),ou", 1));
        
        
        
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
  	Map<String,String> userInfo=new HashMap<String,String>();
  	public Map<String,String> asMap(){return userInfo;};
    public String getUid()               {return userInfo.get("uid");}
    public String getName()              {return userInfo.get("name");}
    public String getCountry()           {return userInfo.get("country");}
    public String getEmployeeNumber()    {return userInfo.get("employeeNumber");}
    public String getMail()              {return userInfo.get("mail");}
    public String getMobile()            {return userInfo.get("mobile");}
    public String getRhatGeo()           {return userInfo.get("geo");}
    public String getRhatCostCenter()    {return userInfo.get("costCenter");}
    public String getRhatCostCenterDesc(){return userInfo.get("costCenterDesc");}
    public String getRhatJobTitle()      {return userInfo.get("jobTitle1");}
    public String getTitle()             {return userInfo.get("jobTitle2");}
    public String getRhatLocation()      {return userInfo.get("location");}
    public String getHireDate()          {return userInfo.get("hireDate");}
    public String getJobCode()           {return userInfo.get("jobCode");}
  }

}
