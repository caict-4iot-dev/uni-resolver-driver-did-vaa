# Dockerfile for universalresolver/driver-did-vaa

FROM universalresolver/base-ubuntu

# build driver-did-vaa

ADD . /opt/driver-did-vaa

RUN cd /opt/driver-did-vaa && mvn clean install package -N -DskipTests

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-vaa/docker/run-driver-did-vaa.sh
CMD "/opt/driver-did-vaa/docker/run-driver-did-vaa.sh"