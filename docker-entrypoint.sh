#!/bin/sh

# Write JWT keys from env vars to files for SmallRye JWT
if [ -n "$JWT_PUBLIC_KEY" ]; then
  printf '%s\n' "$JWT_PUBLIC_KEY" > /app/publicKey.pem
fi

if [ -n "$JWT_PRIVATE_KEY" ]; then
  printf '%s\n' "$JWT_PRIVATE_KEY" > /app/privateKey.pem
fi

exec java -jar /app/quarkus-app/quarkus-run.jar
