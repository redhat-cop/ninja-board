# community-ninja-board

Front end user interface for the Giveback Ninja Program

## Deployment locally (laptop) / or for DEV purposes

### Setting up the environment variables
```
export TRELLO_API_TOKEN=<your token>
export TRELLO_API_KEY=<your token>
export GITHUB_API_TOKEN=<your token>
export GITLAB_API_TOKEN=<your token>
export GD_CREDENTIALS=<contents of you google drive credentials.json file (escaped & in quotes)>
export GRAPHS_PROXY=<optional: url to an external proxy storage app>
export USERS_LDAP_PROVIDER=<optional: ldap url for user lookup>

```
note: see *Getting your google drive credentials.json file* below for the GD_CREDENTIALS value


### Starting up the application

Included in the pom is a jetty plugin, so as long as you have maven installed you just run:

```
mvn clean package -DskipTests jetty:run -Djetty.port=8082
```


## Deployment on Openshift

### Setting up the deployment config files

**Optional:** If you want to deploy a different github repository (ie, a forked copy of redhat-cop/ninja-board), then update the ansible variable `source_repo` in *.applier/inventory/group_vars/all.yml* or specify it as an extra variable.

Configure the mandatory deployment parameters in *applier/params/community-ninja-board-deployment*
```
github_api_token=<your token>
trello_api_token=<your token>
...
```

Alternatively, you can target each of the environments for deployment by prepending `dev_` or `prod_` before each variable.s


### Getting your google drive credentials.json file
```
mkdir gdrive_temp
cd gdrive_temp
wget https://github.com/odeke-em/drive/releases/download/v0.3.9/drive_linux
chmod +x drive_linux
./drive_linux init

```
This will output a url you need to visit and sign in, which will return an auth code to enter in the console.
After you've entered the auto code, the credentials are stored in *gdrive_temp/.gd/credentials.json*.


### Deployment Using the OpenShift Applier

The project can be deployed using the [openshift-applier](https://github.com/redhat-cop/openshift-applier).

#### Prerequisites

The following prerequisites must be satisfied:

1. [Ansible](https://www.ansible.com/)
2. OpenShift Command Line Interface (CLI)
3. OpenShift environment

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

4. Update ansible variables in *.applier/inventory/group_vars/all.yml* to configure desired end result.

   a. Update variable `ninja_board_dev_namespace` to some unique value such as `"{{ namespace }}-dev-<your-keberos-id>"`. This will be the name of the openshift project.
   b. **Optional:** If you want to deploy a different github repository (ie, a forked copy of redhat-cop/ninja-board), then update the ansible variable `source_repo`.

5. Add the namespace defined in 4a above to the Openshift Jenkins Sync plugin

   1. Login into the central Jenkins instance https://jenkins-ninja-board-cicd-dev.int.open.paas.redhat.com
   2. Click on `Manage Jenkins`
   3. Click on `Configure System`
   4. Scroll down to the `Openshift Jenkins Sync` section and add the namespace created in 4a to the `Namespace` subsection. Add values to the list using a space separator.
   5. Click `Save`

6. Execute the _openshift-applier_

    Replace `<ninja-board-dev-namespace>` in the `include_tags` argument with the namespace created in 4a.

    ```
    ansible-playbook -i .applier/inventory galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml -e="@.openshift/params/ninja-board-deployment" -e include_tags=projects-<ninja-board-dev-namespace>,rbac,front-end,back-end
    ```

Once complete, all of the resources should be available in OpenShift
