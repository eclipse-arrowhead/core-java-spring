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

        stage( "Copy Config" )
			{
			    agent { label "master" }
			    steps
			    {
			        sh './jenkins/copy/copy.sh'
			    }
			}

		stage('Test')
            {
                agent { label "master" }
                steps
                {
                    sh './jenkins/test/maven.sh mvn test'
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
	}
}