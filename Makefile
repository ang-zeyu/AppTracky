buildProdImage:
	cd backend &&\
	gradlew build &&\
	docker build . -f Dockerfile -t apptracky-backend
