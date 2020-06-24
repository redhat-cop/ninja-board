# OpenLDAP
An OpenLDAP image based on [osixia/openldap](https://hub.docker.com/r/osixia/openldap/).

Documentation of the base image can be found at https://github.com/osixia/docker-openldap.

### Deploying on OpenShift

Inside the *ldap* subdirectory, you can process the template

```
oc process -f .openshift/templates/ldap.yaml -o yaml | oc create -f -
```

This will create all resources needed to provision the OpenLDAP image. 
**However**, the deployment will fail since it needs to run as root. The template includes a `serviceaccount` named `ldap` that the deployment uses, but doesn't yet have root 
capabalities. To grant root capabilties you can perform 

```
oc adm policy add-scc-to-user anyuid -z ldap
```

If you don't have access to perform the above, ask a cluster administrator to grant `anyuid SCC` to `serviceaccount/ldap`.

### Extending the schema/adding entries
To add new attributes, you can edit the `bootstrap/schema/rhperson.schema`. You will need to rebuild/redeploy for changes to take place. 

To add new entries, you can edit the `ConfigMap` named `ldif-config`. You will need to redeploy the ldap image for changes to take place. 
