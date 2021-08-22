# SPEEDUP TEST

```bash
mvn -DskipTests clean package dockerfile:build
docker run -it --rm jackvasc/speedup-test
docker push jackvasc/speedup-test

reset && \
    kustomize build kustomize/ | kubectl delete --ignore-not-found=true -f - && \
    kustomize build kustomize/ | kubectl apply -f -
```

```bash

reset && kubectl -n speedup-test logs -f job/speedup-test

kubectl -n istio-system delete po -l app=jaeger

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

```bash
docker rmi $(docker images | grep -i jack | awk '{print $3}')
```
