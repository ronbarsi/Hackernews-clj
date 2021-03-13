clean-running-containers:
	docker-compose kill
	docker-compose rm -f -v

start-dev-dependencies: clean-running-containers
	docker-compose up -d db

dev: clean-running-containers start-dev-dependencies
	source ./envfile.dev && export $(shell cut -d= -f1 envfile.dev) && cd posts-system && lein migrations:migrate && lein repl
	docker-compose down