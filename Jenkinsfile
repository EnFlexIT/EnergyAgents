pipeline {
  agent any
  stages {
    stage('Snapshot Build & Deploy') {
      steps {
        echo 'Energy Agents: Start Snapshot Build and Deployment ...'
        sh 'mvn --version'
        sh 'mvn clean install -P p2Deploy -f eclipseProjects/de.enflexit.ea -Dtycho.localArtifacts=ignore'
        echo 'Energy Agents: Build & Deployment of Snapshot is done!'
      }
    }

  }
}