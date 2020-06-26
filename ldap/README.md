# OpenLDAP
An OpenLDAP image based on [osixia/openldap](https://hub.docker.com/r/osixia/openldap/).

Documentation of the base image can be found at https://github.com/osixia/docker-openldap.

### Deploying on OpenShift
Inside the *ldap* subdirectory:

The OpenLDAP image needs to run as root to deploy successfully. You can grant a `serviceaccount` access to `scc/anyuid` by processing the *rbac.yaml* template.

```
oc process -f .openshift/templates/rbac.yaml -o yaml | oc create -f -
```

The above will **FAIL** if you do not have sufficient privileges. If you don't have access to perform this, ask a cluster administrator.

Once the service account has been granted access to `scc/anyuid`, you can process the *ldap.yaml* template.

```
oc process -f .openshift/templates/ldap.yaml -o yaml | oc create -f -
```

Once complete, all ldap resources should be available in OpenShift.

### Extending the schema/adding entries
To add new attributes, you can edit the `bootstrap/schema/rhperson.schema`. You will need to rebuild/redeploy for changes to take place.

To add new entries, you can edit the `ConfigMap` named `ldif-config`. You will need to redeploy the ldap image for changes to take place.
