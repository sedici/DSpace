# NAME:     arieljlira/dspace
FROM tomcat:8.5-jdk11

LABEL maintainer "alira@sedici.unlp.edu.ar"

# DEBIAN_FRONTEND can be set during build but not in ENV (https://github.com/moby/moby/issues/4032)
ARG DEBIAN_FRONTEND=noninteractive

# TODO revisar si estos ENVs de postgres tienen que estar en el dockerfile o no
ENV POSTGRES_DB_HOST "dspace_db"
ENV POSTGRES_DB_PORT "5432"
ENV POSTGRES_DB_NAME "dspace"
ENV POSTGRES_DB_USER "dspace"
ENV POSTGRES_DB_PASS "dspace"

ENV DSPACE_GIT_URL=https://github.com/DSpace/DSpace
ENV DSPACE_GIT_REVISION=master
ENV DSPACE_WEBAPPS="api oai rest rdf server-webapp services sword swordv2"
ENV DSPACE_ROOT_WEBAPP=""

#quizás DSPACE_BASE deba ir a bashrc para no ser customizable 
ENV DSPACE_BASE=/dspace
ENV PATH=${CATALINA_HOME}/bin:${DSPACE_BASE}/install/bin:$PATH \
	CATALINA_OPTS="-Xmx512M -Dfile.encoding=UTF-8"

ENV BOOTSTRAP_DUMP "${DSPACE_BASE}/bootstrap-dump.sql"

#workaround for slim issue https://github.com/debuerreotype/debuerreotype/issues/10
#workaround for https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=866729
RUN mkdir -p /usr/share/man/man1 /usr/share/man/man7

RUN apt-get update && apt-get -y upgrade
RUN apt-get install -y --no-install-recommends  git ant maven postgresql-client  
RUN apt-get install -y --no-install-recommends	procps curl sudo gpg
	#imagemagick ghostscript \
	#net-tools bash-completion mlocate nano less procps apt-utils \
	#apache2 

RUN apt-get autoremove -y && apt-get clean

WORKDIR ${DSPACE_BASE}

#set up tomcat
RUN rm -rf $CATALINA_HOME/webapps/* 

# Install root filesystem
COPY rootfs /

RUN echo "Debian GNU/Linux `cat /etc/debian_version` image. (`uname -rsv`)" >> /root/.built \
    && echo "- with `java -version 2>&1 | awk 'NR == 2'`" >> /root/.built \
    && echo "\nNote: if you need to run commands interacting with DSpace enter the" >> /root/.built \
    && echo "container with: docker exec -it dspace /bin/bash" >> /root/.built

VOLUME $DSPACE_BASE/.m2/
VOLUME ${DSPACE_BASE}

EXPOSE 8080
CMD ["start"]
ENTRYPOINT ["dspace-manager.sh"]

# FIXME agregar user dspace
# ARG DSPACE_USER
# DSPACE_USER=${DSPACE_USER:-dspace} \
# RUN useradd --home-dir $DSPACE_BASE --create-home --shell /bin/bash $DSPACE_USER   
# RUN chown -R $DSPACE_USER.$DSPACE_USER $CATALINA_HOME
# RUN chown -R $DSPACE_USER.$DSPACE_USER $DSPACE_BASE
# USER $DSPACE_USER
