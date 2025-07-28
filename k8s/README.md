# Kubernetes Deployment

This directory contains production-ready Kubernetes manifests for deploying the Cloud Cost Dashboard.

## Quick Start

```bash
# Deploy everything
./deploy.sh

# Remove everything
./undeploy.sh
```

## Architecture

The deployment includes:

- **Namespace**: `cloud-cost` for resource isolation
- **MySQL Database**: Persistent storage with health checks
- **Backend API**: Spring Boot application with 2 replicas
- **Frontend**: React application with 2 replicas  
- **Ingress**: nginx-based routing
- **Secrets**: Secure credential management
- **ConfigMap**: Application configuration

## Prerequisites

1. **Kubernetes cluster** (v1.20+)
2. **kubectl** configured and connected
3. **nginx-ingress-controller** installed
4. **Storage class** `standard` available

## Manual Deployment

Deploy in this order:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f secret.yaml
kubectl apply -f mysql-pvc.yaml
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-service.yaml
kubectl apply -f configmap.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml
```

## Configuration

### Database Credentials

Stored in `secret.yaml` (base64 encoded):
- Username: `root`
- Password: `password123`
- Database: `cloud_costs`

**Production**: Replace with secure credentials before deployment.

### Resource Limits

| Component | CPU Request | Memory Request | CPU Limit | Memory Limit |
|-----------|-------------|----------------|-----------|--------------|
| MySQL     | 250m        | 512Mi          | 500m      | 1Gi          |
| Backend   | 250m        | 512Mi          | 1000m     | 1Gi          |
| Frontend  | 100m        | 128Mi          | 200m      | 256Mi        |

### Health Checks

All services include:
- **Liveness probes**: Restart unhealthy containers
- **Readiness probes**: Route traffic only to ready containers
- **Startup delays**: Allow applications time to initialize

## Access

1. **Get ingress IP**:
   ```bash
   kubectl get ingress -n cloud-cost
   ```

2. **Add to /etc/hosts**:
   ```
   <INGRESS_IP> cloud-cost.local
   ```

3. **Access application**:
   ```
   http://cloud-cost.local
   ```

## Monitoring

```bash
# View all resources
kubectl get all -n cloud-cost

# Check pod status
kubectl get pods -n cloud-cost

# View logs
kubectl logs -n cloud-cost deployment/cloud-cost-backend
kubectl logs -n cloud-cost deployment/cloud-cost-frontend

# Describe resources
kubectl describe deployment -n cloud-cost cloud-cost-backend
kubectl describe service -n cloud-cost cloud-cost-backend

# Check ingress
kubectl describe ingress -n cloud-cost cloud-cost-ingress
```

## Scaling

```bash
# Scale backend
kubectl scale deployment cloud-cost-backend --replicas=3 -n cloud-cost

# Scale frontend  
kubectl scale deployment cloud-cost-frontend --replicas=3 -n cloud-cost
```

## Troubleshooting

### Common Issues

1. **Pods not starting**:
   ```bash
   kubectl describe pod -n cloud-cost <pod-name>
   kubectl logs -n cloud-cost <pod-name>
   ```

2. **Database connection issues**:
   ```bash
   kubectl logs -n cloud-cost deployment/cloud-cost-db
   kubectl exec -it -n cloud-cost deployment/cloud-cost-db -- mysql -u root -p
   ```

3. **Ingress not working**:
   ```bash
   kubectl describe ingress -n cloud-cost
   kubectl get endpoints -n cloud-cost
   ```

4. **Storage issues**:
   ```bash
   kubectl get pvc -n cloud-cost
   kubectl describe pvc mysql-pvc -n cloud-cost
   ```

### Debug Commands

```bash
# Get into backend container
kubectl exec -it -n cloud-cost deployment/cloud-cost-backend -- /bin/bash

# Get into database container
kubectl exec -it -n cloud-cost deployment/cloud-cost-db -- mysql -u root -p

# Port forward for local access
kubectl port-forward -n cloud-cost service/cloud-cost-frontend 3000:80
kubectl port-forward -n cloud-cost service/cloud-cost-backend 8080:8080
```

## Security Considerations

1. **Secrets**: Replace default passwords in production
2. **Network Policies**: Add network policies for pod-to-pod communication
3. **RBAC**: Implement role-based access control
4. **Pod Security**: Add pod security standards
5. **TLS**: Enable TLS termination at ingress

## Production Enhancements

Consider adding:

- **HorizontalPodAutoscaler** for automatic scaling
- **PodDisruptionBudget** for availability during updates
- **NetworkPolicy** for network security
- **ServiceMonitor** for Prometheus monitoring
- **Backup** strategies for database
- **Logging** aggregation (FluentD, Logstash)
- **Monitoring** (Prometheus, Grafana)

## File Descriptions

| File | Purpose |
|------|---------|
| `namespace.yaml` | Creates isolated namespace |
| `secret.yaml` | Database credentials |
| `mysql-pvc.yaml` | Persistent storage claim |
| `mysql-deployment.yaml` | MySQL database deployment |
| `mysql-service.yaml` | Database service |
| `configmap.yaml` | Application configuration |
| `backend-deployment.yaml` | Spring Boot API deployment |
| `frontend-deployment.yaml` | React app deployment |
| `service.yaml` | Frontend/backend services |
| `ingress.yaml` | External access routing |
| `deploy.sh` | Automated deployment script |
| `undeploy.sh` | Cleanup script |