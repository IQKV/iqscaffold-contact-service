# � IQ Scaffold Contact Service - Deployment Guide

> Comprehensive deployment guide for the Contact Service using Helm charts with multi-environment support and external infrastructure dependencies.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Infrastructure Dependencies](#infrastructure-dependencies)
- [Helm Chart Structure](#helm-chart-structure)
- [Environment Configurations](#environment-configurations)
- [Deployment Instructions](#deployment-instructions)
- [Configuration Management](#configuration-management)
- [Monitoring & Observability](#monitoring--observability)
- [Troubleshooting](#troubleshooting)
- [Security Considerations](#security-considerations)

## Overview

The IQ Scaffold Contact Service is deployed using Helm charts with support for multiple environments (local, development, production). The service requires external infrastructure components (PostgreSQL, Redis, RabbitMQ) and integrates with other IQ Scaffold services.

### Key Features

- **Multi-Environment Support**: Separate configurations for local, development, and production
- **External Infrastructure**: Connects to shared infrastructure services via the `iqscaffold-infra` Helm chart
- **Auto-scaling**: Horizontal Pod Autoscaler (HPA) support for production environments
- **Health Checks**: Comprehensive liveness and readiness probes
- **Monitoring**: Prometheus metrics and alerting rules
- **Security**: Pod security contexts, network policies, and secret management

## Prerequisites

### Required Tools

- **Kubernetes Cluster**: v1.24+ (tested with K3s)
- **Helm**: v3.8+
- **kubectl**: Compatible with your cluster version

### Required Infrastructure

The Contact Service depends on external infrastructure services that must be deployed first:

<details>
<summary>Click to expand infrastructure requirements</summary>

```bash
# Deploy infrastructure services first
helm install iqscaffold-infra ./helm-charts/iqscaffold-infra \
  --namespace iqscaffold-dev-env \
  --create-namespace \
  --values ./helm-charts/iqscaffold-infra/values-dev.yaml
```

**Infrastructure Components:**

- PostgreSQL 15+ with database `iqscaffold_contact`
- Redis 7+ for caching and session management
- RabbitMQ 3.11+ for event messaging
- User Service for JWT validation

</details>

### Container Registry Access

<details>
<summary>Click to expand registry configuration</summary>

```bash
# Create image pull secret for private registry
kubectl create secret docker-registry know-how-download-auth \
  --docker-server=know-how.download \
  --docker-username=<username> \
  --docker-password=<password> \
  --docker-email=<email> \
  --namespace=<target-namespace>
```

</details>

## Infrastructure Dependencies

### External Services Configuration

The Contact Service connects to external infrastructure services deployed by the `iqscaffold-infra` Helm chart:

<details>
<summary>Click to expand external services details</summary>

#### PostgreSQL Database

- **Service**: `iqscaffold-infra-postgresql.iqscaffold-dev-env.svc.cluster.local`
- **Port**: 5432
- **Database**: `iqscaffold_contact`
- **User**: `svc_contact_dev_dba` (development) / `svc_contact_prod_dba` (production)

#### Redis Cache

- **Service**: `iqscaffold-infra-redis-master.iqscaffold-dev-env.svc.cluster.local`
- **Port**: 6379
- **Database**: 2 (dedicated for contact service)

#### RabbitMQ Messaging

- **Service**: `iqscaffold-infra-rabbitmq.iqscaffold-dev-env.svc.cluster.local`
- **Port**: 5672
- **User**: `svc_infra_dev_rmq` (development) / `svc_infra_prod_rmq` (production)
- **Virtual Host**: `/` (default)

#### Service Dependencies

- **User Service**: `http://iqscaffold-user-service` (JWT validation)
- **Lead Service**: `http://iqscaffold-lead-service` (lead conversion)
- **Pipeline Service**: `http://iqscaffold-pipeline-service` (pipeline integration)

</details>

## Helm Chart Structure

### Chart Information

<details>
<summary>Click to expand chart structure</summary>

```
iqscaffold-contact-service/
├── Chart.yaml                 # Chart metadata and version
├── values.yaml                # Default configuration values
├── values-local.yaml          # Local development overrides
├── values-dev.yaml            # Development environment overrides
├── values-production.yaml     # Production environment overrides
└── templates/
    ├── deployment.yaml        # Main application deployment
    ├── service.yaml           # ClusterIP service for app
    ├── configmap.yaml         # Application configuration
    ├── secret.yaml            # Sensitive credentials
    ├── serviceaccount.yaml    # Service account and RBAC
    ├── ingress.yaml           # Ingress configuration (optional)
    ├── hpa.yaml               # Horizontal Pod Autoscaler
    ├── servicemonitor.yaml    # Prometheus monitoring
    ├── prometheusrule.yaml    # Alerting rules
    └── NOTES.txt              # Post-installation notes
```

**Chart Details:**

- **Name**: `iqscaffold-contact-service`
- **Version**: `0.1.0`
- **App Version**: `1.0.0`
- **Type**: `application`

</details>

## Environment Configurations

### Local Development

<details>
<summary>Click to expand local development configuration</summary>

**File**: `values-local.yaml`

**Key Features:**

- Relaxed resource limits (768Mi memory, 1000m CPU)
- Faster health check intervals
- Debug annotations enabled
- CORS enabled for development
- Shorter data retention (90 days)
- Email integration disabled

**Resource Configuration:**

```yaml
resources:
  limits:
    memory: 768Mi
    cpu: 1000m
  requests:
    memory: 384Mi
    cpu: 300m
```

</details>

### Production Environment

<details>
<summary>Click to expand production configuration</summary>

**File**: `values-production.yaml`

**Key Features:**

- 3 replica minimum with auto-scaling (up to 10)
- Higher resource limits (1Gi memory, 1000m CPU)
- Conservative health check timings
- Network policies enabled
- Node affinity and anti-affinity rules
- Extended data retention (3 years)
- Full monitoring and alerting

**Auto-scaling Configuration:**

```yaml
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80
```

</details>

## Deployment Instructions

### Local Development Deployment

<details>
<summary>Click to expand local deployment steps</summary>

```bash
# 1. Ensure infrastructure is running
kubectl get pods -n infrastructure

# 2. Deploy contact service for local development
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --namespace default \
  --values ./helm-charts/IQKV/iqscaffold-contact-service/values-local.yaml \
  --set secrets.database.password="dev_password" \
  --set secrets.rabbitmq.password="dev_password"

# 3. Verify deployment
kubectl get pods -l app.kubernetes.io/name=iqscaffold-contact-service
kubectl logs -l app.kubernetes.io/name=iqscaffold-contact-service -f
```

</details>

### Development Environment Deployment

<details>
<summary>Click to expand development deployment steps</summary>

```bash
# 1. Create namespace
kubectl create namespace iqscaffold-dev-env

# 2. Create image pull secret
kubectl create secret docker-registry know-how-download-auth \
  --docker-server=know-how.download \
  --docker-username=$REGISTRY_USERNAME \
  --docker-password=$REGISTRY_PASSWORD \
  --docker-email=$REGISTRY_EMAIL \
  --namespace=iqscaffold-dev-env

# 3. Deploy with development values
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --namespace iqscaffold-dev-env \
  --values ./helm-charts/IQKV/iqscaffold-contact-service/values-dev.yaml \
  --set secrets.database.password="$DEV_DB_PASSWORD" \
  --set secrets.redis.password="$DEV_REDIS_PASSWORD" \
  --set secrets.rabbitmq.password="$DEV_RABBITMQ_PASSWORD"

# 4. Enable monitoring (optional)
helm upgrade iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --namespace iqscaffold-dev-env \
  --reuse-values \
  --set monitoring.serviceMonitor.enabled=true \
  --set monitoring.prometheusRule.enabled=true
```

</details>

### Production Deployment

<details>
<summary>Click to expand production deployment steps</summary>

```bash
# 1. Create production namespace
kubectl create namespace iqscaffold-prod-env

# 2. Create image pull secret
kubectl create secret docker-registry know-how-download-auth \
  --docker-server=know-how.download \
  --docker-username=$REGISTRY_USERNAME \
  --docker-password=$REGISTRY_PASSWORD \
  --docker-email=$REGISTRY_EMAIL \
  --namespace=iqscaffold-prod-env

# 3. Deploy with production values
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --namespace iqscaffold-prod-env \
  --values ./helm-charts/IQKV/iqscaffold-contact-service/values-production.yaml \
  --set image.tag="1.0.0" \
  --set secrets.database.password="$PROD_DB_PASSWORD" \
  --set secrets.redis.password="$PROD_REDIS_PASSWORD" \
  --set secrets.rabbitmq.password="$PROD_RABBITMQ_PASSWORD"

# 4. Verify production deployment
kubectl get pods -n iqscaffold-prod-env -l app.kubernetes.io/name=iqscaffold-contact-service
kubectl get hpa -n iqscaffold-prod-env
```

</details>

### Upgrade Deployment

<details>
<summary>Click to expand upgrade instructions</summary>

```bash
# Upgrade to new version
helm upgrade iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --namespace <target-namespace> \
  --values ./helm-charts/IQKV/iqscaffold-contact-service/values-<environment>.yaml \
  --set image.tag="<new-version>" \
  --reuse-values

# Rollback if needed
helm rollback iqscaffold-contact-service <revision> --namespace <target-namespace>

# Check rollout status
kubectl rollout status deployment/iqscaffold-contact-service -n <target-namespace>
```

</details>

## Configuration Management

### Environment Variables

<details>
<summary>Click to expand environment variables</summary>

The service is configured through environment variables set via ConfigMap and Secrets:

#### Database Configuration

```bash
IQSCAFFOLD_DATABASE_URL=jdbc:postgresql://host:port/database
IQSCAFFOLD_DATABASE_USERNAME=username
IQSCAFFOLD_DATABASE_PASSWORD=password  # From secret
```

#### Redis Configuration

```bash
IQSCAFFOLD_CACHE_REDIS_HOST=redis-host
IQSCAFFOLD_CACHE_REDIS_PORT=6379
IQSCAFFOLD_CACHE_REDIS_DATABASE=2
IQSCAFFOLD_CACHE_REDIS_PASSWORD=password  # From secret
```

#### RabbitMQ Configuration

```bash
IQSCAFFOLD_MESSAGING_RABBITMQ_HOST=rabbitmq-host
IQSCAFFOLD_MESSAGING_RABBITMQ_PORT=5672
IQSCAFFOLD_MESSAGING_RABBITMQ_USERNAME=username
IQSCAFFOLD_MESSAGING_RABBITMQ_PASSWORD=password  # From secret
```

#### Service Integration

```bash
USER_SERVICE_URL=http://iqscaffold-user-service
JWT_ISSUER=iqkv-user-service
JWT_JWK_SET_URI=http://iqscaffold-user-service/.well-known/jwks.json
```

#### CRM Features

```bash
CRM_ENABLE_LEAD_SCORING=true
CRM_ENABLE_ACTIVITY_TRACKING=true
CRM_ENABLE_EMAIL_INTEGRATION=true
```

</details>

### Secret Management

<details>
<summary>Click to expand secret management</summary>

Sensitive credentials are managed through Kubernetes Secrets:

```bash
# Create secrets manually (if not using Helm --set)
kubectl create secret generic iqscaffold-contact-service-secrets \
  --from-literal=database-password="$DB_PASSWORD" \
  --from-literal=redis-password="$REDIS_PASSWORD" \
  --from-literal=messaging-password="$RABBITMQ_PASSWORD" \
  --namespace=<target-namespace>

# Or use Helm --set flags (recommended for CI/CD)
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --set secrets.database.password="$DB_PASSWORD" \
  --set secrets.redis.password="$REDIS_PASSWORD" \
  --set secrets.rabbitmq.password="$RABBITMQ_PASSWORD"
```

</details>

### Custom Configuration

<details>
<summary>Click to expand custom configuration options</summary>

You can override any configuration value using Helm:

```bash
# Custom resource limits
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --set resources.limits.memory="2Gi" \
  --set resources.limits.cpu="2000m"

# Custom replica count
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --set replicaCount=5

# Custom CRM configuration
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --set config.crm.contact.enableLeadScoring=false \
  --set config.crm.activity.retentionDays=180
```

</details>

## Monitoring & Observability

### Prometheus Monitoring

<details>
<summary>Click to expand monitoring configuration</summary>

The chart includes comprehensive monitoring capabilities:

#### ServiceMonitor Configuration

```yaml
monitoring:
  serviceMonitor:
    enabled: true
    namespace: monitoring
    interval: 30s
    scrapeTimeout: 10s
    path: /actuator/prometheus
```

#### Available Metrics

- HTTP request metrics (latency, throughput, errors)
- JVM metrics (memory, GC, threads)
- Database connection pool metrics
- Custom business metrics (contacts created, activities logged)

#### Alerting Rules

- **ContactServiceDown**: Service unavailable for >1 minute
- **ContactServiceHighMemory**: Memory usage >80% for >5 minutes
- **ContactServiceHighLatency**: 95th percentile latency >2 seconds
- **ContactServiceDatabaseConnectionFailure**: No active DB connections

</details>

### Health Checks

<details>
<summary>Click to expand health check configuration</summary>

#### Liveness Probe

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 90
  periodSeconds: 30
  timeoutSeconds: 10
  failureThreshold: 3
```

#### Readiness Probe

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

#### Health Check Endpoints

- `/actuator/health` - Overall health status
- `/actuator/health/liveness` - Liveness probe endpoint
- `/actuator/health/readiness` - Readiness probe endpoint
- `/actuator/info` - Application information

</details>

### Distributed Tracing

<details>
<summary>Click to expand tracing configuration</summary>

OpenTelemetry integration for distributed tracing:

```yaml
config:
  observability:
    tracing:
      enabled: true
      samplingRate: "0.1" # 10% sampling in production
      endpoint: "http://jaeger.monitoring.svc.cluster.local:4317"
```

**Environment Variables:**

```bash
OTEL_SERVICE_NAME=iqscaffold-contact-service
OTEL_RESOURCE_ATTRIBUTES=service.name=iqscaffold-contact-service,service.version=1.0.0
OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger.monitoring.svc.cluster.local:4317
```

</details>

## Troubleshooting

### Common Issues

<details>
<summary>Click to expand troubleshooting guide</summary>

#### Pod Startup Issues

```bash
# Check pod status
kubectl get pods -l app.kubernetes.io/name=iqscaffold-contact-service -n <namespace>

# Check pod logs
kubectl logs -l app.kubernetes.io/name=iqscaffold-contact-service -n <namespace> --tail=100

# Check pod events
kubectl describe pod <pod-name> -n <namespace>
```

#### Database Connection Issues

```bash
# Test database connectivity
kubectl exec -it <pod-name> -n <namespace> -- \
  psql -h <db-host> -U <username> -d <database> -c "SELECT 1;"

# Check database credentials
kubectl get secret iqscaffold-contact-service-secrets -n <namespace> -o yaml
```

#### Service Discovery Issues

```bash
# Check service endpoints
kubectl get endpoints iqscaffold-contact-service -n <namespace>

# Test service connectivity
kubectl exec -it <test-pod> -n <namespace> -- \
  curl http://iqscaffold-contact-service/actuator/health
```

#### Memory/Resource Issues

```bash
# Check resource usage
kubectl top pods -l app.kubernetes.io/name=iqscaffold-contact-service -n <namespace>

# Check HPA status (if enabled)
kubectl get hpa -n <namespace>
kubectl describe hpa iqscaffold-contact-service -n <namespace>
```

</details>

### Debugging Commands

<details>
<summary>Click to expand debugging commands</summary>

```bash
# Get all resources for the service
kubectl get all -l app.kubernetes.io/name=iqscaffold-contact-service -n <namespace>

# Check configuration
kubectl get configmap iqscaffold-contact-service-config -n <namespace> -o yaml

# Check secrets (base64 encoded)
kubectl get secret iqscaffold-contact-service-secrets -n <namespace> -o yaml

# Port forward for local testing
kubectl port-forward svc/iqscaffold-contact-service 8080:80 -n <namespace>

# Execute commands in pod
kubectl exec -it <pod-name> -n <namespace> -- /bin/bash

# Check Helm release status
helm status iqscaffold-contact-service -n <namespace>
helm get values iqscaffold-contact-service -n <namespace>
```

</details>

## Security Considerations

### Pod Security

<details>
<summary>Click to expand security configuration</summary>

#### Security Context

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  runAsGroup: 1001
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  readOnlyRootFilesystem: false
```

#### Network Policies

Network policies are enabled in production to restrict pod-to-pod communication:

```yaml
networkPolicy:
  enabled: true # Production only
```

#### Service Account

Dedicated service account with minimal required permissions:

```yaml
serviceAccount:
  create: true
  annotations: {}
  name: ""
```

</details>

### Secret Management Best Practices

<details>
<summary>Click to expand secret management best practices</summary>

1. **Never commit secrets to version control**
2. **Use Helm --set flags for CI/CD pipelines**
3. **Rotate secrets regularly**
4. **Use external secret management systems (e.g., HashiCorp Vault)**
5. **Monitor secret access and usage**

```bash
# Example with external secret management
helm install iqscaffold-contact-service \
  ./helm-charts/IQKV/iqscaffold-contact-service \
  --set secrets.database.password="$(vault kv get -field=password secret/contact-service/db)" \
  --set secrets.redis.password="$(vault kv get -field=password secret/contact-service/redis)"
```

</details>

---

**For additional support**, refer to the main [Contact Service README](../../README.md) or contact the IQ Scaffold development team.
