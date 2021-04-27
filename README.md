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

## Environment configuration

```bash
kubectl taint nodes kube-worker-01 group=app:NoSchedule
kubectl label node kube-worker-01 group=app
```

Create namespaces

```bash
kubectl create ns k6
kubectl create ns speedup-test
```
