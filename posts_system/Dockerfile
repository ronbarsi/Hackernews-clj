FROM clojure
#RUN apt update && apt install -y curl
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj project.clj
RUN lein deps
COPY . .
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
CMD ["java", "-jar", "/usr/src/app/app-standalone.jar"]
