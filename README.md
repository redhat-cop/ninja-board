# community-ninja-board

Front end user interface for the Giveback Ninja Program(v1) and a parallel system to replace the former called v2.

## Deployment locally (laptop) / or for DEV purposes

### Local v1 Deployment

#### Setting up the environment variables
```
export TRELLO_API_TOKEN=<your token>
export TRELLO_API_KEY=<your token>
export GITHUB_API_TOKEN=<your token>
export GITLAB_API_TOKEN=<your token>
export GD_CREDENTIALS=<contents of you google drive credentials.json file (escaped & in quotes)>
export GRAPHS_PROXY=<optional: url to an external proxy storage app>
export USERS_LDAP_PROVIDER=<ldap url for user lookup>
```
note: see *Getting your Google drive credentials.json file* below for the GD_CREDENTIALS value


#### Starting up the application

Included in the pom is a jetty plugin, so as long as you have maven installed you just run:

```
mvn clean package -DskipTests jetty:run -Djetty.port=8082
```
### Local v2 Deployment

#### Deploy Back-End Services

1. Follow steps in https://github.com/redhat-cop/ninja-board/blob/v2/quarkus/README.MD

#### Deploy Front-End

1. Follow steps in https://github.com/redhat-cop/ninja-board/blob/v2/react/ninja-react/README.md

## Deployment on Openshift

### Deployment Using the OpenShift Applier

The project can be deployed using the [openshift-applier](https://github.com/redhat-cop/openshift-applier).

### Prerequisites

The following prerequisites must be satisfied:

1. [Ansible](https://www.ansible.com/)
2. OpenShift Command Line Interface (CLI)
3. OpenShift environment

### v1 OpenShift Deployment

#### Setting up the deployment config files

**Optional:** If you want to deploy a different github repository (ie, a forked copy of redhat-cop/ninja-board), then
update the ansible variable `source_repo` in *.applier/inventory/group_vars/all.yml* or specify it as an extra variable.

Configure the mandatory deployment parameters in *.openshift/applier/params/ninja-board-deployment*
```
github_api_token=<your token>
trello_api_token=<your token>
...
```

Alternatively, you can target each of the environments for deployment by prepending `dev_` or `prod_` before each 
variable.

#### Getting your google drive credentials.json file
```
mkdir gdrive_temp
cd gdrive_temp
wget https://github.com/odeke-em/drive/releases/download/v0.3.9/drive_linux
chmod +x drive_linux
./drive_linux init

```
This will output an url you need to visit and sign in, which will return an auth code to enter the console.
After you've entered the auto code, the credentials are stored in *gdrive_temp/.gd/credentials.json*.

#### Login to OpenShift

Login to OpenShift

```
oc login <openshift url>
```

#### Deployment

Utilize the following steps to deploy the project

1. Clone the repository

    ```
    git clone https://github.com/redhat-cop/ninja-board
    ```

2. Change into the project directory and utilize Ansible Galaxy to retrieve required dependencies

    ```
    ansible-galaxy install -r requirements.yml --roles-path=galaxy
    ```

3. Ensure the variables have been updated in the _ninja-board-deployment_ params file

4. Execute the _openshift-applier_

    ```
    ansible-playbook -i .applier/inventory galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml \
    -e="@.openshift/params/ninja-board-deployment" -e exclude_tags=ldap-rbac,ldap,v2
    ```

Once complete, all the v1 resources should be available in OpenShift

### v2 OpenShift Deployment

#### Login to OpenShift

Login to OpenShift

```
oc login <openshift url>
```

#### Deployment

Utilize the following steps to deploy the project

1. Clone the repository and checkout v2 branch

    ```
    git clone https://github.com/redhat-cop/ninja-board
    git checkout v2
    ```

2. Change into the project directory and utilize Ansible Galaxy to retrieve required dependencies

    ```
    ansible-galaxy install -r requirements.yml --roles-path=galaxy
    ```

3. Ensure the variables have been updated in the _.applier/inventory/group_vars/all.yml_ ansible params file to the
desired values

4. Execute the _openshift-applier_
    ```
    ansible-playbook -i .applier/inventory galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml \
    -e="@.openshift/params/ninja-board-deployment" -e include_tags=v2
    ```
   
    ***NOTES***
    `include_tags` is a comma separated variable. For creating v2 artifacts, v2 should always be included. Add
    additional tags depending on your need:
    * If you have not yet created the openshift dev project, you will need to add `project-req-dev`
    * If you have not yet created a Jenkins instance in the openshift dev project, you will need to add `cicd`
    * If you are not behind RedHat Network and would like access to services that use ldap, you will need to add 
    `ldap-rbac,ldap`. This will ***FAIL*** if you do not have sufficient privileges. If you don't have access to 
    perform this, ask a cluster administrator.

Once complete, all the v2 resources should be available in OpenShift
