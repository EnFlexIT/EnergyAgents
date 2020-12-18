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
        echo 'Energy Agent: Start Snapshot Build and Deployment ...'
        sh 'mvn --version'
        sh 'mvn clean install -P p2Deploy -f eclipseProjects/org.agentgui'
        echo 'Energy Agent: Build & Deployment of Snapshot is done!'
      }
    }

  }
}