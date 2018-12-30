pipeline {
  agent any
  stages {
    stage('Prepare') {
      steps {
        withMaven(maven: 'Maven 3', jdk: 'JDK 8', publisherStrategy: 'EXPLICIT') {
          sh 'mvn clean'
        }

      }
    }
    stage('Build') {
      steps {
        withMaven(jdk: 'JDK 8', maven: 'Maven 3', publisherStrategy: 'EXPLICIT') {
          sh 'mvn install'
        }

      }
    }
    stage('Archive') {
      steps {
        archiveArtifacts 'target/*.jar'
      }
    }
  }
}