DOCKER_COMPOSE=APPDYNAMICS_AGENT_ACCOUNT_NAME=$(APPDYNAMICS_AGENT_ACCOUNT_NAME) APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=$(APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY) APPDYNAMICS_CONTROLLER_HOST_NAME=$(APPDYNAMICS_CONTROLLER_HOST_NAME) APPDYNAMICS_CONTROLLER_SSL_ENABLED=$(APPDYNAMICS_CONTROLLER_SSL_ENABLED) APPDYNAMICS_CONTROLLER_PORT=$(APPDYNAMICS_CONTROLLER_PORT) AWS_ACCESS_KEY_ID=$(AWS_ACCESS_KEY_ID) AWS_SECRET_ACCESS_KEY=$(AWS_SECRET_ACCESS_KEY) docker-compose
DOCKER_RUN=$(DOCKER_COMPOSE) up -d --build
DOCKER_STOP=$(DOCKER_COMPOSE) down

dockerRun: ## Run MA in docker
	@echo starting container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
	@eco AWS Access key is: $(AWS_ACCESS_KEY_ID)
	${DOCKER_RUN}
	@echo started container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&

dockerStop:
	${DOCKER_STOP}

sleep:
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished