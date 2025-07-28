#!/bin/bash

# Cloud Cost Dashboard - Kubernetes Deployment Script
# This script deploys the complete cloud cost dashboard to Kubernetes

set -e

echo "🚀 Deploying Cloud Cost Dashboard to Kubernetes..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster"
    exit 1
fi

echo "✅ Kubernetes cluster connection verified"

# Apply manifests in the correct order
echo "📦 Creating namespace..."
kubectl apply -f namespace.yaml

echo "🔐 Creating secrets..."
kubectl apply -f secret.yaml

echo "💾 Creating persistent volume claims..."
kubectl apply -f mysql-pvc.yaml

echo "🗄️ Deploying MySQL database..."
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-service.yaml

echo "⚙️ Creating ConfigMap..."
kubectl apply -f configmap.yaml

echo "🖥️ Deploying backend application..."
kubectl apply -f backend-deployment.yaml

echo "🌐 Deploying frontend application..."
kubectl apply -f frontend-deployment.yaml

echo "🔗 Creating services..."
kubectl apply -f service.yaml

echo "🌍 Creating ingress..."
kubectl apply -f ingress.yaml

echo "⏳ Waiting for deployments to be ready..."

# Wait for MySQL to be ready
echo "  📊 Waiting for MySQL..."
kubectl wait --namespace=cloud-cost --for=condition=available --timeout=300s deployment/cloud-cost-db

# Wait for backend to be ready
echo "  🖥️ Waiting for backend..."
kubectl wait --namespace=cloud-cost --for=condition=available --timeout=300s deployment/cloud-cost-backend

# Wait for frontend to be ready
echo "  🌐 Waiting for frontend..."
kubectl wait --namespace=cloud-cost --for=condition=available --timeout=300s deployment/cloud-cost-frontend

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Deployment Summary:"
echo "  • Namespace: cloud-cost"
echo "  • Database: MySQL 8.0 with persistent storage"
echo "  • Backend: 2 replicas with health checks"
echo "  • Frontend: 2 replicas with health checks"
echo "  • Ingress: nginx with routing rules"
echo ""
echo "🔍 View resources:"
echo "  kubectl get all -n cloud-cost"
echo ""
echo "🌐 Access the application:"
echo "  Add to /etc/hosts: <INGRESS_IP> cloud-cost.local"
echo "  Then visit: http://cloud-cost.local"
echo ""
echo "📊 Get ingress IP:"
echo "  kubectl get ingress -n cloud-cost"
echo ""
echo "🔧 Debug commands:"
echo "  kubectl logs -n cloud-cost deployment/cloud-cost-backend"
echo "  kubectl logs -n cloud-cost deployment/cloud-cost-frontend"
echo "  kubectl describe pod -n cloud-cost -l app=backend"