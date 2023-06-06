# Web-scale Data Management Project Template

Basic project structure with Python's Flask and Redis.
**You are free to use any web framework in any language and any database you like for this project.**

### Project structure

- `env`
  Folder containing the Redis env variables for the docker-compose deployment
- `helm-config`
  Helm chart values for Redis and ingress-nginx
- `k8s`
  Folder containing the kubernetes deployments, apps and services for the ingress, order, payment and stock services.
- `order`
  Folder containing the order application logic and dockerfile.
- `payment`
  Folder containing the payment application logic and dockerfile.

- `stock`
  Folder containing the stock application logic and dockerfile.

- `test`
  Folder containing some basic correctness tests for the entire system. (Feel free to enhance them)

### Deployment types:

#### docker-compose (local development)

After coding the REST endpoint logic run `docker-compose up --build` in the base folder to test if your logic is correct
(you can use the provided tests in the `\test` folder and change them as you wish).

**_Requirements:_** You need to have docker and docker-compose installed on your machine.

#### minikube (local k8s cluster)

This setup is for local k8s testing to see if your k8s config works before deploying to the cloud. Start minikube and run `kubectl apply -f .` in the k8s folder.

**_Requirements:_** You need to have minikube (with ingress enabled) and helm installed on your machine.

#### kubernetes cluster (managed k8s cluster in the cloud)

Change hostname in ingress-service.yaml to the hostname of the cloud service. Then run `kubectl apply -f .` in the k8s folder. Services should be accessible at:
hostname/orders
hostname/stock
hostname/payment

**_Requirements:_** You need to have access to kubectl of a k8s cluster.
