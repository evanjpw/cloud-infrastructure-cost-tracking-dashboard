#!/bin/bash

# Cloud Cost Dashboard - Kubernetes Cleanup Script
# This script removes the cloud cost dashboard from Kubernetes

set -e

echo "🗑️ Removing Cloud Cost Dashboard from Kubernetes..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace cloud-cost &> /dev/null; then
    echo "ℹ️ Namespace 'cloud-cost' does not exist, nothing to clean up"
    exit 0
fi

echo "🔍 Found cloud-cost namespace, proceeding with cleanup..."

# Delete in reverse order
echo "🌍 Removing ingress..."
kubectl delete -f ingress.yaml --ignore-not-found=true

echo "🔗 Removing services..."
kubectl delete -f service.yaml --ignore-not-found=true
kubectl delete -f mysql-service.yaml --ignore-not-found=true

echo "🌐 Removing frontend deployment..."
kubectl delete -f frontend-deployment.yaml --ignore-not-found=true

echo "🖥️ Removing backend deployment..."
kubectl delete -f backend-deployment.yaml --ignore-not-found=true

echo "⚙️ Removing ConfigMap..."
kubectl delete -f configmap.yaml --ignore-not-found=true

echo "🗄️ Removing MySQL database..."
kubectl delete -f mysql-deployment.yaml --ignore-not-found=true

echo "💾 Removing persistent volume claims..."
kubectl delete -f mysql-pvc.yaml --ignore-not-found=true

echo "🔐 Removing secrets..."
kubectl delete -f secret.yaml --ignore-not-found=true

echo "📦 Removing namespace..."
kubectl delete -f namespace.yaml --ignore-not-found=true

echo ""
echo "✅ Cleanup completed successfully!"
echo ""
echo "⚠️ Note: Persistent volumes may still exist depending on your storage class"
echo "   Check with: kubectl get pv"