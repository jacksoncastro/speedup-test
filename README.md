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
kubectl label node kube-worker-app group=app
kubectl taint nodes kube-worker-app group=app:NoSchedule

kubectl label node kube-worker-load group=test
kubectl taint nodes kube-worker-load group=test:NoSchedule

kubectl create ns istio-system
kubectl label node kube-worker-istio group=istio
kubectl annotate namespace istio-system scheduler.alpha.kubernetes.io/node-selector=istio

```

Create namespaces

```bash
kubectl create ns k6
kubectl create ns speedup-test
```

```bash
docker rmi $(docker images | grep -i jack | awk '{print $3}')
```
