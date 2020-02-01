@Library('cop-library') _

openshift.withCluster() {
  env.localToken = readFile('/var/run/secrets/kubernetes.io/serviceaccount/token').trim()
  env.NAMESPACE = openshift.project()
}

pipeline {
  agent {
    label 'maven'
  }
  
  stages {
    stage ('Fetch Source Code') {
      steps {
          git url: "${SOURCE_REPOSITORY_URL}", branch: "${SOURCE_REPOSITORY_REF}"
      }
    }
    
    stage('Build Application'){
      steps {
        sh "mvn clean install -DskipTests=true"   
      }
    }

    stage('Build Image'){
      steps {
        sh """
        rm -rf oc-build && mkdir -p oc-build/deployments
        cp -v target/*.war oc-build/deployments/
        """   
        script {
          openshift.withCluster() {
            openshift.withProject() {
              openshift.selector("bc", "${APPLICATION_NAME}").startBuild("--from-dir=oc-build").logs("-f")
            }
          }
        }

      }
    }
    
    stage ('Verify Deployment to Dev') {
      steps {
        rollout([projectName: "${DEV_NAMESPACE}", resourceKindAndName: "dc/${APPLICATION_NAME}", latest: false])
      }
    }

    stage ('Promote to Prod') {
      agent none
      steps {
        script {
          input message: "Promote Ninja Board to Prod?"
        }
      }
    }

    stage ('Tag Image to Prod'){
      steps {
        script {
          openshift.withCluster() {
            openshift.withProject() {
              echo "Promoting via tag from ${DEV_NAMESPACE} to ${PROD_NAMESPACE}/${APPLICATION_NAME}"
              tagImage(sourceImagePath: "${DEV_NAMESPACE}", sourceImageName: "${APPLICATION_NAME}", toImagePath: "${PROD_NAMESPACE}", toImageName: "${APPLICATION_NAME}", toImageTag: "latest")
            }
          }
        }
      }
    }

    stage ('Verify Deployment to Prod') {
      steps {
        rollout([projectName: "${PROD_NAMESPACE}", resourceKindAndName: "dc/${APPLICATION_NAME}", latest: false])
      }
    }
  } 
}