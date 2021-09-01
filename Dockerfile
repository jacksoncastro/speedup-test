FROM loadimpact/k6:0.33.0 AS k6

FROM openjdk:8-jdk-alpine3.9

LABEL maintainer="jack.vasc@yahoo.com.br"

ARG JAR_FILE
ARG USER=speedup

ENV HOME /home/$USER

RUN apk add --update sudo gettext bash tzdata curl && \
        cp /usr/share/zoneinfo/America/Fortaleza /etc/localtime

RUN adduser -D $USER && \
        echo "$USER ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/$USER && \
        chmod 0440 /etc/sudoers.d/$USER

RUN apk del sudo tzdata

RUN wget -qO /usr/local/bin/kubectl "https://storage.googleapis.com/kubernetes-release/release/$(wget -qO- https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl" && \
wget -qO /tmp/kustomize.tar.gz "https://github.com/kubernetes-sigs/kustomize/releases/download/kustomize%2Fv3.8.0/kustomize_v3.8.0_linux_amd64.tar.gz" && \
tar -xvf /tmp/kustomize.tar.gz -C /usr/local/bin && \
chmod +x /usr/local/bin/kubectl /usr/local/bin && \
rm /tmp/kustomize.tar.gz

COPY --from=k6 /usr/bin/k6 /usr/bin/k6

USER $USER

RUN mkdir $HOME/app
WORKDIR $HOME/app
COPY /target/dependency-jars /run/dependency-jars
ADD /target/${JAR_FILE} /run/app.jar
COPY --chown=$USER /orquestration /orquestration

ENTRYPOINT java -jar /run/app.jar
