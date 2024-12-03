FROM summerwind/actions-runner:latest

# Instalar Java, Maven, kubectl, SonarQube Scanner y Liquibase
USER root

# Actualizar repositorios e instalar herramientas básicas
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven curl unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Instalar kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl

# Instalar SonarQube Scanner
RUN curl -o /tmp/sonar-scanner-cli.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-5.0.1.3006-linux.zip && \
    unzip /tmp/sonar-scanner-cli.zip -d /opt && \
    mv /opt/sonar-scanner-* /opt/sonar-scanner && \
    ln -s /opt/sonar-scanner/bin/sonar-scanner /usr/local/bin/sonar-scanner && \
    rm /tmp/sonar-scanner-cli.zip

# Instalar Liquibase
RUN curl -fsSL https://github.com/liquibase/liquibase/releases/download/v4.23.1/liquibase-4.23.1.tar.gz -o liquibase.tar.gz && \
    mkdir -p /usr/local/liquibase && \
    tar -xvzf liquibase.tar.gz -C /usr/local/liquibase && \
    ln -sf /usr/local/liquibase/liquibase /usr/local/bin/liquibase && \
    rm liquibase.tar.gz

USER runner

# Crear directorio para Maven cache y ajustar permisos
RUN mkdir -p /home/runner/.m2/repository && chown -R runner:runner /home/runner/.m2

COPY settings.xml /home/runner/.m2/settings.xml

# Crear un directorio de trabajo
WORKDIR /app

# Copiar únicamente el archivo pom.xml al contenedor
COPY ../pom.xml /app/pom.xml

# Descargar dependencias especificadas en el pom.xml
RUN mvn dependency:resolve dependency:resolve-plugins || mvn dependency:go-offline

# Configurar el directorio de Maven como cache
ENV MAVEN_CONFIG=/home/runner/.m2

# Configurar el path para SonarQube Scanner
ENV PATH="${PATH}:/opt/sonar-scanner/bin"

# Configurar el path para Liquibase
ENV PATH="${PATH}:/opt/liquibase"