set dotenv-load := true

DOCKERHUB_USERNAME := env_var("DOCKERHUB_USERNAME")
DOCKERHUB_TOKEN := env_var("DOCKERHUB_TOKEN")

# Login Docker Hub
docker-login:
    echo "{{DOCKERHUB_TOKEN}}" | docker login -u "{{DOCKERHUB_USERNAME}}" --password-stdin

# Build e push di una singola immagine Docker
docker-build-push service:
    #!/usr/bin/env bash
    set -euo pipefail

    VERSION="${VERSION:?VERSION environment variable is required}"

    SERVICE="{{service}}"
    IMAGE="{{DOCKERHUB_USERNAME}}/eurotransit-${SERVICE}"

    MAJOR="$(echo "${VERSION}" | cut -d. -f1)"
    MINOR="$(echo "${VERSION}" | cut -d. -f1,2)"

    docker build \
      -t "${IMAGE}:${VERSION}" \
      -t "${IMAGE}:${MINOR}" \
      -t "${IMAGE}:${MAJOR}" \
      "./${SERVICE}"

    docker push "${IMAGE}:${VERSION}"
    docker push "${IMAGE}:${MINOR}"
    docker push "${IMAGE}:${MAJOR}"

# Build e push di tutte le immagini
docker-build-push-all:
    just docker-build-push frontend
    just docker-build-push catalog
    just docker-build-push inventory
    just docker-build-push order
