#!/bin/sh
set -e

if [ -z "$KEYCLOAK_CLIENT_SECRET" ] || [ -z "$KEYCLOAK_ADMIN_CLIENT_SECRET" ]; then
  echo "❌ Keycloak secrets missing"
  exit 1
fi

sed \
  -e "s|__BACKEND_CLIENT_SECRET__|${KEYCLOAK_CLIENT_SECRET}|g" \
  -e "s|__ADMIN_CLIENT_SECRET__|${KEYCLOAK_ADMIN_CLIENT_SECRET}|g" \
  interim-realm.template.json > interim-realm.json

echo "✅ Realm generated with secrets"
