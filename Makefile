version = 0.1.2

build:
	mvn clean package  -Drevision=$(version)

docker_build: build
	docker build -t cloudmediaforge/imgflux-upload-api:$(version) ./imgFlux-upload-api
	docker build -t cloudmediaforge/imgflux-download-api:$(version) ./imgFlux-download-api
	docker build -t cloudmediaforge/imgflux-mcp-sever:$(version) ./imgFlux-mcp-server

build_push: docker_build
	docker push cloudmediaforge/imgflux-upload-api:$(version)
	docker push cloudmediaforge/imgflux-download-api:$(version)
	docker push cloudmediaforge/imgflux-mcp-sever:$(version)