
oc new-project community-ninja-board

# CREATE DEPLOYMENT APP
oc process --param-file=applier/params/community-ninja-board-deployment -f applier/templates/community-ninja-board-deployment.yml | oc apply -f-
#deploymentconfig "community-ninja-board" created
#route "community-ninja-board" created
#service "community-ninja-board" created

# CREATE BUILD APP
oc process --param-file=applier/params/community-ninja-board-build -f applier/templates/community-ninja-board-build.yml | oc apply -f-
#imagestream "community-ninja-board" created
#imagestream "community-ninja-board-base" created
#buildconfig "community-ninja-board-base" created
#buildconfig "community-ninja-board" created
