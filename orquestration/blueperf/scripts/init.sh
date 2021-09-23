#!/bin/bash

# Use environment APP_HOST like:
# APP_HOST='http://192.168.1.202:31952'
# APP_HOST='http://istio-ingressgateway.istio-system.svc.cluster.local'

curl "$APP_HOST"/booking/loader/load
echo
curl "$APP_HOST"/flight/loader/load
echo
curl "$APP_HOST"/customer/loader/load?numCustomers=10000
echo
echo 'Finished!'