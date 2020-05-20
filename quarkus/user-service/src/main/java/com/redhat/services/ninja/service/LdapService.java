package com.redhat.services.ninja.service;

import com.redhat.services.ninja.user.RedHatUser;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class LdapService {
    private static final Logger LOGGER = Logger.getLogger(LdapService.class.getSimpleName());

    @ConfigProperty(name = "users.ldap.provider")
    String ldapProvider;
    @ConfigProperty(name = "users.ldap.baseDN")
    String baseDomainName;
    @ConfigProperty(name = "users.ldap.search.formatter", defaultValue = "(&(objectclass=Person)(%s=%s))")
    String searchDomainName;

    private LdapContext context;

    private final SearchControls searchControls = new SearchControls();

    @PostConstruct
    protected void init() throws NamingException {
        var env = new Hashtable<String, String>(3);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapProvider);
        env.put(Context.SECURITY_AUTHENTICATION, "none");
        context = new InitialLdapContext(env, null);

        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setTimeLimit(30000);
    }

    public List<RedHatUser> search(String key, String value) throws NamingException {
        var searchDN = String.format(searchDomainName, key, value);
        var namingEnum = context.search(baseDomainName, searchDN, searchControls);

        return Collections.list(namingEnum).stream()
                .map(SearchResult::getAttributes)
                .map(ATTRIBUTES_TO_MAP)
                .map(RedHatUser.newMapper())
                .collect(Collectors.toList());
    }

    private static final Function<Attributes, Map<String, String>> ATTRIBUTES_TO_MAP = attributes ->
            Collections.list(attributes.getAll()).stream()
                    .map(Attribute.class::cast)
                    .collect(Collectors.toMap(Attribute::getID, attribute -> {
                        try {
                            return attribute.get().toString();
                        } catch (NamingException e) {
                            LOGGER.log(Level.WARNING, e.getMessage());
                            return "";
                        }
                    }));
}
