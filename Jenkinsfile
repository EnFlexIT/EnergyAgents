pipeline {
  agent any
  stages {
    stage('Snapshot Build & Deploy') {
      tools {
        jdk 'jdk8'
      }
      environment {
        JAVA_HOME = '/usr/lib/jvm/java-1.8.0-openjdk-amd64'
      }
      steps {
        echo 'Energy Agents: Start Snapshot Build and Deployment ...'
        sh 'mvn --version'
        sh 'mvn clean install -P p2Deploy -f eclipseProjects/de.enflexit.ea'
        echo 'Energy Agents: Build & Deployment of Snapshot is done!'
      }
    }

  }
}