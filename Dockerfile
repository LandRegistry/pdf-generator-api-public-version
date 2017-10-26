# Set the base image to the java/gradle base image
FROM hmlandregistry/dev_base_java:3

ENV APP_NAME=pdf-generator-api \
    MAX_HEALTH_CASCADE=6 

# The CMD is inherited from the base image
