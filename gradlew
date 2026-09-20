#!/bin/sh

# Gradle start-up script for MusicPro.
# The official Gradle 9.3.1 wrapper JAR is bootstrapped on first use if it is absent.

APP_HOME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v9.3.1/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$(dirname "$WRAPPER_JAR")" || exit 1
  TMP_JAR="$WRAPPER_JAR.tmp"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL --retry 3 "$WRAPPER_URL" -o "$TMP_JAR" || {
      rm -f "$TMP_JAR"
      echo "Unable to download Gradle wrapper JAR." >&2
      exit 1
    }
  elif command -v wget >/dev/null 2>&1; then
    wget -q --tries=3 -O "$TMP_JAR" "$WRAPPER_URL" || {
      rm -f "$TMP_JAR"
      echo "Unable to download Gradle wrapper JAR." >&2
      exit 1
    }
  else
    echo "Gradle wrapper JAR is missing and neither curl nor wget is available." >&2
    exit 1
  fi
  mv "$TMP_JAR" "$WRAPPER_JAR" || exit 1
fi

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="$(command -v java || true)"
fi

if [ ! -x "$JAVACMD" ]; then
  echo "JAVA_HOME is not set and no usable java command was found." >&2
  exit 1
fi

exec "$JAVACMD" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
