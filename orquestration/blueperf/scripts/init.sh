#!/bin/bash

# HOST='http://192.168.1.202:31952'
HOST='http://istio-ingressgateway.istio-system.svc.cluster.local'

curl $HOST/booking/loader/load
echo
curl $HOST/flight/loader/load
echo
curl $HOST/customer/loader/load?numCustomers=10000
echo
echo 'Finished!'