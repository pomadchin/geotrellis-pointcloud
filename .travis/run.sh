#!/usr/bin/env bash

docker run -it --net=host \
           -v $HOME/.ivy2:/root/.ivy2 \
           -v $HOME/.coursier:/root/.coursier \
           -v $HOME/.sbt:/root/.sbt \
           -v $TRAVIS_BUILD_DIR:/geotrellis-pointcloud \
           -e TRAVIS_SCALA_VERSION=$TRAVIS_SCALA_VERSION \
           -e TRAVIS_COMMIT=$TRAVIS_COMMIT \
           -e TRAVIS_JDK_VERSION=$TRAVIS_JDK_VERSION daunnc/pdal-ubuntu:2.2.0 /bin/bash -c "cd /geotrellis-pointcloud; .travis/build-and-test.sh"
