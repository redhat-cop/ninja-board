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
        script {
          openshift.withCluster() {
            openshift.withProject() {
              def dcObj = openshift.selector('dc', APPLICATION_NAME).object()
              def podSelector = openshift.selector('pod', [deployment: "${APPLICATION_NAME}-${dcObj.status.latestVersion}"])
              podSelector.untilEach {
                  echo "pod: ${it.name()}"
                  return it.object().status.containerStatuses[0].ready
              }
            }
          }
        }
      }
    }
  } 
}