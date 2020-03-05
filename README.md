# Hackernews - Clojure

The same project as: https://github.com/ronbarsi/HackerNews , but implemented in cloujure language (and not python)

Two ways to run the service:
```
1. with docker containter: 
    - 'docker-compose up' on root folder, will expose port 5000 on your localhost to REST requests
2. with k8s node: (minikube) 
    (cd /src)
    - minikube start 
    - kubectl apply -f mysql -f redis -f posts_system 
    (wait until all pods are running (kubectl get pods))
    - minikube service posts-system 
    (the url of the pooped up window is the url of the service)
```





