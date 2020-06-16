#Getting Started

##Requirements
* OpenShift Client (`oc`)
* The deployment location must be within Red Hat Network.

## Create or set your current project.
```shell script
oc new-project ninja-board-golum-experiment # or
oc project ninja-board-golum-experiment
```

##Deploying on OpenShift

```shell script
oc process -f .openshift/templates/ninja-board-back-end.yaml -o yaml | oc apply -f -
oc process -f .openshift/templates/ninja-board-front-end.yaml -p API_URL=http://$(oc get route ninja-board-back-end -o jsonpath='{.spec.host}') -o yaml | oc apply -f -
```

## Open Front End URL within your browser of choice.

To obtain this url:
```shell script
oc get route ninja-board-front-end -o jsonpath='{.spec.host}'
```