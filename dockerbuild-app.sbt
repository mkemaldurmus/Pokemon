enablePlugins(DockerPlugin)
docker / dockerfile := {
  val artifact: File     = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("openjdk:11.0.6-slim")
    add(artifact, artifactTargetPath)
    env("HEAP_SIZE" -> "256m", "CLASS_NAME" -> "com.kemal.Boot", "ROOT_LOG_LEVEL" -> "INFO")
    cmdRaw(
      s"java -Xms$$HEAP_SIZE -Xmx$$HEAP_SIZE -cp $artifactTargetPath $$CLASS_NAME"
    )
  }
}