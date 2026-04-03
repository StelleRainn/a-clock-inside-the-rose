# 云主机Terminal错误日志

#### 4 月 3 日 23:42
root@iZ0jldq0yd4sa1m00vxvqmZ:/opt/acir-deploy# docker-compose up -d --build
[+] Running 13/13
 ✔ acir-mysql 12 layers [⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿]      0B/0B      Pulled                                   15.9s 
   ✔ 72a69066d2fe Pull complete                                                                  3.8s 
   ✔ e0431212d27d Pull complete                                                                  0.3s 
   ✔ 93619dbc5b36 Pull complete                                                                  0.5s 
   ✔ 99da31dd6142 Pull complete                                                                  0.8s 
   ✔ 626033c43d70 Pull complete                                                                  0.9s 
   ✔ 37d5d7efb64e Pull complete                                                                  0.5s 
   ✔ ac563158d721 Pull complete                                                                  3.3s 
   ✔ d2ba16033dad Pull complete                                                                  0.6s 
   ✔ 688ba7d5c01a Pull complete                                                                  0.5s 
   ✔ 00e060b6d11d Pull complete                                                                 11.8s 
   ✔ 1c04857f594f Pull complete                                                                  0.5s 
   ✔ 4d7cfa90e6ea Pull complete                                                                  0.4s 
[+] Building 2309.2s (13/15)                                                           docker:default
 => [acir-backend internal] load build definition from Dockerfile                                0.0s
 => => transferring dockerfile: 995B                                                             0.0s
 => [acir-backend internal] load metadata for docker.io/library/eclipse-temurin:17-jre-alpine    4.1s
 => [acir-backend internal] load metadata for docker.io/library/maven:3.9.6-eclipse-temurin-17   4.1s
 => [acir-backend internal] load .dockerignore                                                   0.0s
 => => transferring context: 2B                                                                  0.0s
 => [acir-backend builder 1/6] FROM docker.io/library/maven:3.9.6-eclipse-temurin-17@sha256:  1206.5s
 => => resolve docker.io/library/maven:3.9.6-eclipse-temurin-17@sha256:29a1658b1f3078e07c2b17f7  0.0s
 => => sha256:4712dfa85971124b8c3507fbba4c02441e5754f1517162926e600afd3fc5404b 358B / 358B       0.9s
 => => sha256:fc06d68d71ba375a9ceed165c0c04b09b1a8cc193146888663b005660fc25013 156B / 156B       0.9s
 => => sha256:58c3491a14ebc973960cbda9f2eea22537785a3cb07e81487bd1115bad4a8278 852B / 852B       1.5s
 => => sha256:45f480637770dbb740623c0c7f827d44bbacc9204faf3d07b4135d54a2bee043 9.48MB / 9.48MB  49.2s
 => => sha256:67f99c2668af1d0eced6d61dad057d7beffe543a8e8370d5c0d3fe8682e05 19.00MB / 19.00MB  330.2s
 => => sha256:a3ba11f7aaaedad962216812fe84aee9061aeabac6932f0274a1d204eb96e8e8 734B / 734B       1.4s
 => => sha256:c97285723537ab7921fca4d081c256e501adfdaa8992d04637992075f4cea392 173B / 173B       0.8s
 => => sha256:d59fd278c1b4ffe1727be6ccba42125e8f4db57e660e795cce19889d2c 145.10MB / 145.10MB  1180.6s
 => => sha256:5e5d1bccc5440d3a24f4a620704b9e687b4163c6c872fcc8e812e200c9bba 17.46MB / 17.46MB  208.8s
 => => sha256:4a023cab5400feb5c1ab725beb8345ddb0e3200314004b56677a5eee2e8c8 30.44MB / 30.44MB  617.2s
 => => extracting sha256:4a023cab5400feb5c1ab725beb8345ddb0e3200314004b56677a5eee2e8c86cf        0.9s
 => => extracting sha256:5e5d1bccc5440d3a24f4a620704b9e687b4163c6c872fcc8e812e200c9bbac58        0.7s
 => => extracting sha256:d59fd278c1b4ffe1727be6ccba42125e8f4db57e660e795cce19889d2c776457        2.2s
 => => extracting sha256:c97285723537ab7921fca4d081c256e501adfdaa8992d04637992075f4cea392        0.0s
 => => extracting sha256:a3ba11f7aaaedad962216812fe84aee9061aeabac6932f0274a1d204eb96e8e8        0.0s
 => => extracting sha256:67f99c2668af1d0eced6d61dad057d7beffe543a8e8370d5c0d3fe8682e059a3        0.7s
 => => extracting sha256:45f480637770dbb740623c0c7f827d44bbacc9204faf3d07b4135d54a2bee043        0.1s
 => => extracting sha256:58c3491a14ebc973960cbda9f2eea22537785a3cb07e81487bd1115bad4a8278        0.0s
 => => extracting sha256:4712dfa85971124b8c3507fbba4c02441e5754f1517162926e600afd3fc5404b        0.0s
 => => extracting sha256:fc06d68d71ba375a9ceed165c0c04b09b1a8cc193146888663b005660fc25013        0.0s
 => [acir-backend stage-1 1/4] FROM docker.io/library/eclipse-temurin:17-jre-alpine@sha256:7a  645.9s
 => => resolve docker.io/library/eclipse-temurin:17-jre-alpine@sha256:7aa804a1824d18d06c68598fe  0.0s
 => => sha256:4adb00321f3d94aaeaa42c1ea668bbeaff8b812f2fad313e0dd4dbe61230e060 2.28kB / 2.28kB   0.8s
 => => sha256:8a88a697ab448612edd46009d67c7ff9a550d5ee7b21f3a5918c8db270a94e30 128B / 128B       1.1s
 => => sha256:b50a56d2ab3866fc53135239e9f9e16aab5173989633f1e4177a216c3dfba0e 16.84MB / 16.84MB  8.9s
 => => sha256:2b28c8d7648832152177a8901dd89be1a11ae271432c89a598c1344f87755 47.10MB / 47.10MB  644.4s
 => => sha256:589002ba0eaed121a1dbf42f6648f29e5be55d5c8a6ee0f8eaa0285cc21ac153 3.86MB / 3.86MB  19.4s
 => => extracting sha256:589002ba0eaed121a1dbf42f6648f29e5be55d5c8a6ee0f8eaa0285cc21ac153        0.1s
 => => extracting sha256:b50a56d2ab3866fc53135239e9f9e16aab5173989633f1e4177a216c3dfba0e0        0.5s
 => => extracting sha256:2b28c8d7648832152177a8901dd89be1a11ae271432c89a598c1344f8775512e        1.0s
 => => extracting sha256:8a88a697ab448612edd46009d67c7ff9a550d5ee7b21f3a5918c8db270a94e30        0.0s
 => => extracting sha256:4adb00321f3d94aaeaa42c1ea668bbeaff8b812f2fad313e0dd4dbe61230e060        0.0s
 => [acir-backend internal] load build context                                                   0.1s
 => => transferring context: 58.95kB                                                             0.0s
 => [acir-backend stage-1 2/4] WORKDIR /app                                                      0.4s
 => [acir-backend builder 2/6] WORKDIR /build                                                    0.4s
 => [acir-backend builder 3/6] COPY pom.xml .                                                    0.1s
 => [acir-backend builder 4/6] RUN mvn dependency:go-offline -B                               1087.6s
 => [acir-backend builder 5/6] COPY src ./src                                                    0.1s
 => ERROR [acir-backend builder 6/6] RUN mvn clean package -DskipTests                          10.1s
------
 > [acir-backend builder 6/6] RUN mvn clean package -DskipTests:
1.744 [INFO] Scanning for projects...
2.214 [INFO] 
2.215 [INFO] ----------------------< com.stellerainn:back-end >----------------------
2.216 [INFO] Building back-end 0.0.1-SNAPSHOT
2.219 [INFO]   from pom.xml
2.219 [INFO] --------------------------------[ jar ]---------------------------------
3.011 Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.14.12/byte-buddy-1.14.12.pom
Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.14.12/byte-buddy-1.14.12.pom (16 kB at 9.8 kB/s)
4.637 Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-parent/1.14.12/byte-buddy-parent-1.14.12.pom
Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-parent/1.14.12/byte-buddy-parent-1.14.12.pom (63 kB at 273 kB/s)
4.887 Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.14.12/byte-buddy-agent-1.14.12.pom
Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.14.12/byte-buddy-agent-1.14.12.pom (10 kB at 23 kB/s)
5.575 Downloading from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar (68 kB at 150 kB/s)
6.031 Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.14.12/byte-buddy-1.14.12.jar
6.032 Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.14.12/byte-buddy-agent-1.14.12.jar
Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.14.12/byte-buddy-agent-1.14.12.jar (257 kB at 90 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.14.12/byte-buddy-1.14.12.jar (4.2 MB at 1.3 MB/s)
9.384 [INFO] 
9.385 [INFO] --- clean:3.3.2:clean (default-clean) @ back-end ---
9.465 [INFO] 
9.466 [INFO] --- resources:3.3.1:resources (default-resources) @ back-end ---
9.607 [INFO] Copying 1 resource from src/main/resources to target/classes
9.627 [INFO] Copying 0 resource from src/main/resources to target/classes
9.628 [INFO] 
9.628 [INFO] --- compiler:3.11.0:compile (default-compile) @ back-end ---
9.862 [INFO] Changes detected - recompiling the module! :source
9.862 [INFO] Compiling 42 source files with javac [debug release 21] to target/classes
10.05 [INFO] ------------------------------------------------------------------------
10.05 [INFO] BUILD FAILURE
10.05 [INFO] ------------------------------------------------------------------------
10.05 [INFO] Total time:  8.332 s
10.05 [INFO] Finished at: 2026-04-03T15:09:46Z
10.05 [INFO] ------------------------------------------------------------------------
10.05 [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project back-end: Fatal error compiling: error: release version 21 not supported -> [Help 1]
10.05 [ERROR] 
10.05 [ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
10.05 [ERROR] Re-run Maven using the -X switch to enable full debug logging.
10.05 [ERROR] 
10.05 [ERROR] For more information about the errors and possible solutions, please read the following articles:
10.05 [ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
------
failed to solve: process "/bin/sh -c mvn clean package -DskipTests" did not complete successfully: exit code: 1
root@iZ0jldq0yd4sa1m00vxvqmZ:/opt/acir-deploy# 
