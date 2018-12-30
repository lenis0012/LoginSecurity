pipeline {
  agent any
  stages {
    stage('Prepare') {
      steps {
        withMaven() {
          sh 'mvn clean'
        }

      }
    }
    stage('Build') {
      steps {
        withMaven() {
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