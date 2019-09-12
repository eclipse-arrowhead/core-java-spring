pipeline
{
	agent any

	stages
	{
		stage("Create Maven Cache")
			{
				agent{ label "master" }
				steps
				{
					sh " docker volume create maven-repo "
				}
			}

			stage( "Build" )
			{
				agent { label "master" }
				steps
				{
					sh  '''
					    ./jenkins/build/maven.sh mvn -B -DskipTests clean package
					    ./jenkins/build/build.sh
					    '''

				}
			}

			stage('Test')
			{
			    agent { label "master" }
                steps
                {
                    sh './jenkins/test/maven.sh mvn test'
                }

                post
                {
                    always
                    {
                        junit 'java-app/target/surefire-reports/*.xml'
                    }
                }
            }
	}
}