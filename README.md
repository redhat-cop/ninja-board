# community-ninja-board

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
