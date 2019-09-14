pipeline
{
	agent any

    environment
    {
        URL = {env.DOCKER_REPO}
        NAMESPACE = {env.DOCKER_BUILD_NAMESPACE}
    }

	stages
	{
		stage( "Create Maven Cache" )
			{
				agent{ label "master" }
				steps
				{
					sh " docker volume create maven-repo "
				}
			}

		stage( "Test" )
            {
                agent { label "master" }
                steps
                {
                    sh '''
                       ./jenkins/copy/copy.sh
                       ./jenkins/test/maven.sh mvn test
                       '''
                }
            }

		stage( "Build" )
			{
				agent { label "master" }
				steps
				{
					sh  '''
					    ./jenkins/copy/copy.sh
					    ./jenkins/build/maven.sh mvn -B -DskipTests clean package
					    ./jenkins/build/build.sh
					    '''

				}
			}
		stage( "Push" )
        	{
        		agent { label "master" }
        	    steps
        		{
        		    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId:'portus-push', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                      sh './jenkins/push/push.sh $USERNAME $PASSWORD'
                    }
        		}
        	}
        stage( "Deploy" )
            {
            	agent { label "master" }
                steps
           		{
                    sh 'echo Deploy'
               	}
           	}
	}
}