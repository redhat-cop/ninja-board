# community-ninja-board





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

**Optional:** If you want to deploy a different github repository (ie, a forked copy of redhat-cop/ninja-board), then update *applier/params/community-ninja-board-build*
```
SOURCE_REPOSITORY_URL=https://github.com/<your repository>/ninja-board
```

Configure the mandatory deployment parameters in *applier/params/community-ninja-board-deployment*
```
GITHUB_API_TOKEN=<your token>
TRELLO_API_TOKEN=<your token>
TRELLO_API_KEY=<your token>
GITLAB_API_TOKEN=<your token>
GD_CREDENTIALS=<contents of you google drive credentials.json file (not escaped)>
```

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



### Deploying the application

```
oc login <openshift url>
oc new-project <project name>
deploy.sh <project name>
```


## Old Info - superceded by the above


## Deployment Using the OpenShift Applier

Alternatively to the manual deployment of the core projects and supporting infrastructure, the project can be deployed using the [openshift-applier](https://github.com/redhat-cop/openshift-applier).

### Prerequisites

The following prerequisites must be satisfied:

1. [Ansible](https://www.ansible.com/)
2. OpenShift Command Line Interface (CLI)
3. OpenShift environment

### Deployment

Utilize the following steps to deploy the project

1. Clone the repository

    ```
    git clone https://github.com/matallen/community-ninja-board
    ```

2. Change into the project directory and utilize Ansible Galaxy to retrieve required dependencies

    ```
    ansible-galaxy install -r requirements.yml --roles-path=galaxy
    ``` 
 
3. Update credentials in the _applier/params/community-ninja-board-deployment_ file

4. Execute the _openshift-applier_

    ```
    ansible-playbook -i applier/inventory galaxy/openshift-applier/playbooks/openshift-cluster-seed.yml
    ```

Once complete, all of the resources should be available in OpenShift
