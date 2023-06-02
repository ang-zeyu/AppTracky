./gradlew bootRun --args='--spring.profiles.active=local' &
while true; do
  inotifywait -e modify,create,delete,move -r ./src/ && ./gradlew build
done