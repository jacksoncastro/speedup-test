# SPEEDUP TEST

```bash
mvn -DskipTests clean package dockerfile:build
docker run -it --rm jackvasc/speedup-test
docker push jackvasc/speedup-test
kustomize build kustomize/ | kubectl delete --ignore-not-found=true -f -
kustomize build kustomize/ | kubectl apply -f -
```

```bash
mvn -DskipTests clean package dockerfile:build && \
    docker push jackvasc/speedup-test
```

