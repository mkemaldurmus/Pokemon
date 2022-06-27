FROM hseeberger/scala-sbt:11.0.2
# For a Alpine Linux version, comment above and uncomment below:
# FROM 1science/sbt

RUN mkdir -p /exampleapp
RUN mkdir -p /exampleapp/out

WORKDIR /exampleapp

COPY src/test/scala /exampleapp