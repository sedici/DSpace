include .env

.PHONY: up update install down stop prune ps bash logs dsbin

default: up

up:
	@echo "Starting up containers for $(COMPOSE_PROJECT_NAME)..."
	docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml up -d

update:
	@echo "Stopping containers for $(COMPOSE_PROJECT_NAME)..."
	@docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml stop
	docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml run dspace update
	sudo chown -R $(id -u):$(id -g) ../install/*
	@echo "Starting up containers for $(COMPOSE_PROJECT_NAME)..."
	docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml up -d

install:
	docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml run --rm dspace install
#	sudo chown -R $(id -u):$(id -g) data/*

install-angular:
	docker-compose -f docker-compose.yml run dspace-angular install

start-angular:
	docker-compose -f docker-compose.yml run dspace-angular start

reset-db:
	docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml run dspace reset-db

index-discovery:
	@echo "[HELP!] Define \"PARAMS\" variable if wants to pass specifics parameters to 'index-discovery' command... In example 'make PARAMS=\"-b\" index-discovery'"...
	@if [ -f "../install/bin/dspace" ]; then echo "Running \"index-discovery $(PARAMS)\"..."; docker-compose exec dspace /dspace/install/bin/dspace index-discovery "$(PARAMS)"; echo "Exiting..."; fi

dsbin:
	@echo "[HELP!] Define \"COMMAND\" variable if wants to pass specifics command to 'bin/dspace' DSpace's CLI... In example 'make COMMAND=\"dsprop -p dspace.dir\" dsbin'"...
	@if [ -f "../install/bin/dspace" ]; then echo "Running \"bin/dspace $(COMMAND)\"..."; docker-compose exec dspace /dspace/install/bin/dspace $(COMMAND); echo "Exiting..."; fi


down: stop


stop:
	@echo "Stopping containers for $(COMPOSE_PROJECT_NAME)..."
	@docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml stop

prune:
	@echo "Removing containers for $(COMPOSE_PROJECT_NAME)..."
	@docker-compose -f docker-compose.yml -f others/docker-compose-debug.yml down

ps:
	@docker ps --filter name='$(COMPOSE_PROJECT_NAME)*'

bash:
	docker-compose exec dspace /bin/bash

#logs:
#	@docker-compose logs -f $(filter-out $@,$(MAKECMDGOALS))

logs:
	docker-compose exec dspace  sh -c "tail -f /dspace/install/log/* /usr/local/tomcat/logs/*"

less:
	less -N ../install/log/$(filter-out $@,$(MAKECMDGOALS)).log

# https://stackoverflow.com/a/6273809/1826109
%:
	@:
