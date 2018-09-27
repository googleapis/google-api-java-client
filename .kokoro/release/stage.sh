#!/bin/bash
# Copyright 2018 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

source $(dirname "$0")/common.sh

pushd $(dirname "$0")/../../

setup_environment_secrets
create_settings_xml_file "settings.xml"

# Install play services
wget https://dl.google.com/dl/android/maven2/com/google/android/gms/play-services-basement/8.3.0/play-services-basement-8.3.0.aar
unzip play-services-basement-8.3.0.aar
mvn install:install-file \
  -Dfile=classes.jar \
  -DgroupId=com.google.android.google-play-services \
  -DartifactId=google-play-services \
  -Dversion=1 \
  -Dpackaging=jar

# Install the android SDK
mvn dependency:get -Dartifact=com.google.android:android:4.1.1.4

# Install the appengine SDK
mvn dependency:get -Dartifact=com.google.appengine:appengine-api-1.0-sdk:1.9.65

mvn clean install deploy \
  --settings settings.xml \
  -DperformRelease=true \
  -Dgpg.executable=gpg \
  -Dgpg.passphrase=${GPG_PASSPHRASE} \
  -Dgpg.homedir=${GPG_HOMEDIR}


