#!/bin/sh
set -e

./gradlew clean

./gradlew shadow

rm -rf "/Applications/Raspi Monitor.app"

mkdir -p mac_bundle/wip

mkdir -p "mac_bundle/wip/Raspi Monitor.app"

mkdir -p "mac_bundle/wip/Raspi Monitor.app/Contents"

cp mac_bundle/Info.plist "mac_bundle/wip/Raspi Monitor.app/Contents/"

mkdir -p "mac_bundle/wip/Raspi Monitor.app/Contents/MacOS"

cp build/libs/*-all.jar "mac_bundle/wip/Raspi Monitor.app/Contents/MacOS/raspimonitor.jar"

cp mac_bundle/raspimonitor "mac_bundle/wip/Raspi Monitor.app/Contents/MacOS/"

chmod 755 "mac_bundle/wip/Raspi Monitor.app/Contents/MacOS/raspimonitor"

mkdir -p "mac_bundle/wip/Raspi Monitor.app/Contents/Resources"

cp mac_bundle/raspimonitor.icns "mac_bundle/wip/Raspi Monitor.app/Contents/Resources/"

chmod 755 "mac_bundle/wip/Raspi Monitor.app"

cp -R "mac_bundle/wip/Raspi Monitor.app" /Applications/