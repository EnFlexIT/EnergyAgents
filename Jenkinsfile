pipeline {
  agent any
  stages {
    stage('Snapshot Build & Deploy for Java 21') {
      steps {
        echo 'Energy Agents: Start Snapshot Build and Deployment ...'
        sh 'mvn --version'
        sh 'mvn clean install -P p2Deploy -f eclipseProjects/de.enflexit.ea -Dtycho.localArtifacts=ignore -Dtycho.localArtifacts=ignore -Dtycho.p2.transport.min-cache-minutes=0'
        echo 'Energy Agents: Build & Deployment of Snapshot is done!'
      }
    }

  }
}