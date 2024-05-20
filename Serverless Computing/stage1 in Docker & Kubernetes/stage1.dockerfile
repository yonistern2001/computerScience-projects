FROM azul/zulu-openjdk:21.0.1-21.30-jre

EXPOSE 9090/tcp

WORKDIR /app
COPY /target/classes/ /app

CMD ["java", "edu.yu.cs.com3800.stage1.SimpleServerImpl", "9090"]