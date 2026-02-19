# 👥 IQ Scaffold Contact Service

> Comprehensive CRM contact management microservice providing multi-tenant contact lifecycle management, lead conversion tracking, bulk operations, and intelligent contact scoring with event-driven integration.

## Table of Contents

- [Business Purpose](#business-purpose)
- [Overview](#overview)
- [What It Demonstrates](#what-it-demonstrates)
- [Architecture Patterns](#architecture-patterns)
- [Technical Highlights](#technical-highlights)
- [Use Cases Implemented](#use-cases-implemented)
- [API Endpoints](#api-endpoints)
- [API Examples](#api-examples)
- [Learning Points](#learning-points)
- [Adapting for Your Domain](#adapting-for-your-domain)
- [Integration with Other Services](#integration-with-other-services)
- [Deployment Guide](docs/deployment/README.md)

## Business Purpose

CRM Contact management service that handles:

- **Contact Lifecycle Management** - Complete CRUD operations for contact management with lead scoring and conversion tracking
- **Lead Conversion Tracking** - Seamless conversion of qualified leads to contacts with complete audit trail and timestamps
- **Bulk Operations** - Efficient bulk processing for contact creation, updates, and deletions (up to 100 items per request)
- **Contact Scoring** - Lead scoring system integration for contact qualification and prioritization
- **Event-Driven Integration** - RabbitMQ-based event publishing for contact lifecycle events and cross-service communication
- **Multi-Tenancy** - Complete tenant isolation ensuring data segregation across organizations with schema-per-tenant strategy
- **Contact Search & Filtering** - Advanced search capabilities with pagination and status-based filtering

## Overview

This is the contact management hub for the IQ Scaffold CRM platform. It centralizes contact data management, enabling sales teams to maintain comprehensive customer records, track lead conversions, and manage contact relationships efficiently while providing event-driven integration with other CRM services.

## What It Demonstrates

### 👥 Contact Management & Lifecycle

- Complete contact CRUD operations with comprehensive data model
- Lead conversion tracking with timestamps and source attribution
- Contact status management (ACTIVE, INACTIVE, LEAD, PROSPECT, CUSTOMER, ARCHIVED)
- Company association and relationship management
- Advanced search and filtering capabilities with pagination
- Bulk operations for efficient data management (max 100 items per request)

### 📊 Lead Scoring & Analytics

- Integrated lead scoring system (0-100) for contact qualification
- Conversion tracking from leads to contacts with complete audit trail
- Score-based contact prioritization and segmentation

### 🔄 Event-Driven Architecture

- RabbitMQ event publishing for contact lifecycle (created, updated, deleted)
- Event-driven integration with Lead Service and Pipeline Service
- Asynchronous processing for improved performance and scalability
- Event sourcing patterns for audit trail and data consistency

## Architecture Patterns

### Key Design Patterns

- Repository pattern for data access with custom queries
- Service layer for business logic and event publishing
- DTO pattern with Java records for API contracts
- Event-driven architecture with RabbitMQ integration
- Bulk operation patterns for efficient data processing
- Multi-tenant context management with schema isolation

### API Design

- RESTful endpoints with proper HTTP methods and status codes
- Versioning support (URL-based: `/api/v1/`)
- OpenAPI/Swagger documentation with comprehensive examples
- Bulk operation endpoints with detailed success/failure reporting
- Consistent error response format with Problem Details (RFC 7807)
- Pagination and sorting support for list operations

## Technical Highlights

### Contact Management Features

- Comprehensive contact data model with lead scoring integration
- Lead conversion tracking with source attribution and timestamps
- Contact status lifecycle management with business rules
- Company association and relationship management
- Advanced search capabilities (first name, last name, email matching)
- Bulk operations with atomic success/failure reporting (max 100 items)

### Performance Optimization

- Hibernate second-level caching with Ehcache for improved query performance
- Efficient bulk operations with batch processing
- Optimized database queries with proper indexing
- Connection pooling for database connections
- Async event publishing for non-blocking operations

### Data Management

- Liquibase for database migrations with tenant-specific schemas
- PostgreSQL with proper indexing and constraints
- Transaction management with rollback support
- Audit fields for tracking creation and modification
- Soft deletes for maintaining data integrity

### Technology Stack

- **Framework**: Spring Boot 4.0
- **Java**: 21
- **Database**: PostgreSQL 15 with Liquibase migrations
- **Cache**: Hibernate second-level cache with Ehcache 3
- **Messaging**: RabbitMQ for event-driven communication
- **Security**: Spring Security with OAuth2 JWT Resource Server
- **Documentation**: SpringDoc OpenAPI 2.x
- **Observability**: Micrometer, OpenTelemetry, Prometheus
- **Testing**: JUnit 5, Mockito, H2 (test), Testcontainers, ArchUnit

### Quick Start

#### Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.9+

#### Local Development

1. **Start infrastructure services**:

   <details>
   <summary>Click to expand bash commands</summary>

   ```bash
   docker compose up -d postgres-contact redis-contact rabbitmq-contact
   ```

   </details>

2. **Run the application**:

   <details>
   <summary>Click to expand bash commands</summary>

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

   </details>

3. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs
   - Health Check: http://localhost:8080/actuator/health
   - Prometheus Metrics: http://localhost:8080/actuator/prometheus

#### Docker Development

<details>
<summary>Click to expand bash commands</summary>

```bash
# Build and run all services
docker compose up --build

# Run in detached mode
docker compose up -d
```

</details>

### Configuration

#### Environment Variables

Key environment variables for configuration:

<details>
<summary>Click to expand environment variables</summary>

```bash
# Database
IQSCAFFOLD_DATABASE_URL=jdbc:postgresql://localhost:5434/iqscaffold_contact_local
IQSCAFFOLD_DATABASE_USERNAME=iqscaffold_contact
IQSCAFFOLD_DATABASE_PASSWORD=iqscaffold_password

# RabbitMQ
IQSCAFFOLD_MESSAGING_RABBITMQ_HOST=localhost
IQSCAFFOLD_MESSAGING_RABBITMQ_PORT=5673
IQSCAFFOLD_MESSAGING_RABBITMQ_USERNAME=iqscaffold
IQSCAFFOLD_MESSAGING_RABBITMQ_PASSWORD=iqscaffold_password

# Security
USER_SERVICE_URL=http://iqscaffold-user-service:8080
JWT_ISSUER=iqscaffold-user-service

# CRM Features
CRM_ENABLE_LEAD_SCORING=true
CRM_ENABLE_ACTIVITY_TRACKING=true
CRM_ENABLE_EMAIL_INTEGRATION=true
```

</details>

#### Profiles

- `local` - Local development with debug logging
- `staging` - Staging environment configuration
- `production` - Production environment with JSON logging

### Multi-tenancy

The service uses schema-per-tenant isolation:

1. **Tenant Identification**: Via `X-Tenant-ID` header
2. **Schema Management**: Automatic schema creation and migration
3. **Data Isolation**: Complete separation between tenants

### Database Schema

#### System Schema (public)

- `tenant_info` - Tenant metadata and schema mapping (managed by user service)

#### Tenant Schemas

Each tenant has its own schema with:

- `contacts` - Contact information with lead scoring and conversion tracking
  - Basic info: first name, last name, email, phone, job title
  - Lead scoring: lead_score field for qualification
  - Conversion tracking: converted_from_lead_id, converted_at
  - Company association: company_id reference
  - Status tracking: ACTIVE, INACTIVE, LEAD, PROSPECT, CUSTOMER, ARCHIVED
  - Audit fields: created_at, updated_at, created_by, updated_by

### Development

#### Running Tests

<details>
<summary>Click to expand bash commands</summary>

```bash
# Unit tests
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify

# With coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

</details>

#### Test Configuration

- **Unit Tests**: Use H2 in-memory database
- **Integration Tests**: Disabled by default (require tenant schema setup)
- **Cache**: Disabled in tests to avoid ehcache.xml dependency
- **Coverage**: 70% minimum instruction coverage, 50% branch coverage

#### Code Quality

The project enforces:

- **Checkstyle**: Google Java Style Guide compliance
- **JaCoCo**: 70% instruction coverage, 50% branch coverage minimum
- **ArchUnit**: Architecture rules and layer dependencies
- **Maven Enforcer**: Java 21+ and Maven 3.9+ requirements

#### Adding New Features

1. Create feature branch from `main`
2. Implement feature with tests
3. Update documentation
4. Submit pull request

### Monitoring

#### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

#### Metrics

- Prometheus: `/actuator/prometheus`
- Application metrics: `/actuator/metrics`

#### Tracing

- OpenTelemetry integration
- Distributed tracing support

### Troubleshooting

#### Common Issues

1. **Database Connection**: Verify PostgreSQL is running on port 5434 and credentials are correct
2. **RabbitMQ Connection**: Check RabbitMQ service on port 5673 and credentials
3. **JWT Validation**: Ensure user service is accessible at configured URL
4. **Tenant Context**: Verify `X-Tenant-ID` header is present in requests
5. **Cache Issues**: In tests, cache is disabled; in production, ensure Ehcache configuration is valid

#### Logs

<details>
<summary>Click to expand bash commands</summary>

```bash
# View application logs
docker-compose logs contact-service

# Follow logs
docker-compose logs -f contact-service
```

</details>
- PostgreSQL with proper indexing and constraints
- Transaction management with rollback support
- Audit fields for tracking creation and modification
- Soft deletes for maintaining data integrity

### Event-Driven Integration

- RabbitMQ event publishing for contact lifecycle events
- Event consumers in Lead Service and Pipeline Service
- Structured event payloads with metadata and context
- Event-driven lead conversion workflow
- Cross-service communication patterns

### Multi-Tenancy Implementation

- Schema-per-tenant isolation with automatic context resolution
- Tenant context extraction from JWT tokens and headers
- Tenant-scoped repositories and queries
- Cross-tenant data isolation and security
- Tenant-aware event publishing

### Testing Approach

- Unit tests with JUnit 5 and Mockito
- Integration tests with H2 in-memory database
- Architecture tests with ArchUnit for layer validation
- Code coverage with JaCoCo (70% instruction, 50% branch minimum)
- Testcontainers for integration testing (when enabled)

### Operational Features

- Docker containerization with multi-stage builds
- Environment-specific profiles (local, staging, production)
- Structured JSON logging with correlation IDs
- Health checks and actuator endpoints
- Prometheus metrics integration
- OpenTelemetry distributed tracing

## Use Cases Implemented

### Contact Management

- Create new contacts with comprehensive data validation
- Update existing contact information with audit tracking
- Delete contacts with proper cleanup and event publishing
- Search contacts by name, email, or other criteria
- Filter contacts by status, company, or other attributes
- Paginated contact listing with sorting options

### Lead Conversion Workflow

- Convert qualified leads to contacts via Lead Service integration
- Track conversion source and timestamp for analytics
- Maintain lead-to-contact relationship mapping
- Publish conversion events for downstream processing
- Update lead status upon successful conversion

### Bulk Operations

- Bulk contact creation with validation and error reporting
- Bulk status updates for contact lifecycle management
- Bulk contact deletion with proper cleanup
- Bulk lead score updates for qualification management
- Detailed success/failure reporting for each operation

### Event-Driven Integration

- Publish contact lifecycle events (created, updated, deleted)
- Enable Lead Service to track conversion success
- Trigger Pipeline Service updates for won opportunities
- Support audit trail and compliance requirements

## API Endpoints

<details>
<summary>Click to expand API endpoints</summary>

### Contacts

**Base Path:** `/api/v1/contacts`

- `GET /api/v1/contacts` - List contacts (paginated, with search and filtering)
- `POST /api/v1/contacts` - Create contact
- `GET /api/v1/contacts/{id}` - Get contact by ID
- `PUT /api/v1/contacts/{id}` - Update contact
- `DELETE /api/v1/contacts/{id}` - Delete contact
- `GET /api/v1/contacts/company/{companyId}` - Get contacts by company
- `PATCH /api/v1/contacts/{id}/score` - Update lead score

### Bulk Operations

**Base Path:** `/api/v1/contacts`

- `POST /api/v1/contacts/bulk` - Bulk create contacts (max 100)
- `PATCH /api/v1/contacts/bulk/status` - Bulk update contact status (max 100)
- `DELETE /api/v1/contacts/bulk` - Bulk delete contacts (max 100)
- `PATCH /api/v1/contacts/bulk/scores` - Bulk update lead scores (max 100)

### Query Parameters

**List Contacts (`GET /api/v1/contacts`):**

- `search` - Search term (matches first name, last name, email)
- `status` - Filter by contact status (ACTIVE, INACTIVE, LEAD, PROSPECT, CUSTOMER, ARCHIVED)
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `sort` - Sort field and direction (e.g., `lastName,asc`)

</details>

## API Examples

### Contact Management Endpoints

#### Create Contact

- `POST /api/v1/contacts` - Create new contact

**Request:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "jobTitle": "Software Engineer",
  "companyId": 123,
  "status": "ACTIVE",
  "leadScore": 85,
  "convertedFromLeadId": "lead-456"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "jobTitle": "Software Engineer",
  "companyId": 123,
  "status": "ACTIVE",
  "leadScore": 85,
  "convertedFromLeadId": "lead-456",
  "convertedAt": "2026-02-03T10:30:00Z",
  "createdAt": "2026-02-03T10:30:00Z",
  "updatedAt": "2026-02-03T10:30:00Z"
}
```

#### List Contacts with Search and Filtering

- `GET /api/v1/contacts?search=john&status=ACTIVE&page=0&size=20&sort=lastName,asc`

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "leadScore": 85
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "orders": [{ "property": "lastName", "direction": "ASC" }]
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Bulk Operations

#### Bulk Create Contacts

- `POST /api/v1/contacts/bulk` - Create multiple contacts

**Request:**

```json
{
  "contacts": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE"
    },
    {
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "status": "PROSPECT"
    }
  ]
}
```

**Response (200 OK):**

```json
{
  "successCount": 2,
  "failureCount": 0,
  "results": [
    {
      "contactId": 1,
      "email": "john.doe@example.com",
      "success": true,
      "message": "Contact created successfully",
      "contact": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "status": "ACTIVE"
      }
    },
    {
      "contactId": 2,
      "email": "jane.smith@example.com",
      "success": true,
      "message": "Contact created successfully",
      "contact": {
        "id": 2,
        "firstName": "Jane",
        "lastName": "Smith",
        "email": "jane.smith@example.com",
        "status": "PROSPECT"
      }
    }
  ]
}
```

#### Bulk Update Contact Status

- `PATCH /api/v1/contacts/bulk/status` - Update status for multiple contacts

**Request:**

```json
{
  "contactIds": [1, 2, 3],
  "status": "CUSTOMER"
}
```

#### Bulk Update Lead Scores

- `PATCH /api/v1/contacts/bulk/scores` - Update lead scores for multiple contacts

**Request:**

```json
{
  "updates": [
    { "contactId": 1, "score": 85 },
    { "contactId": 2, "score": 90 },
    { "contactId": 3, "score": 75 }
  ]
}
```

### Event Publishing Examples

#### Contact Created Event

Published to RabbitMQ exchange `crm.events` with routing key `contact.created`:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "contact.created",
  "timestamp": "2026-02-03T10:30:00Z",
  "tenantId": "tenant-123",
  "userId": "user-456",
  "contactId": "1",
  "metadata": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "status": "ACTIVE",
    "convertedFromLeadId": "lead-789"
  }
}
```

### Monitoring Endpoints

- `/actuator/health` - Health status with database and RabbitMQ checks
- `/actuator/metrics` - Application metrics including contact operations
- `/actuator/prometheus` - Prometheus-formatted metrics
- `/swagger-ui.html` - Interactive API documentation
- `/api-docs` - OpenAPI specification

## Learning Points

This implementation serves as a reference for:

- Building contact management systems with comprehensive data models
- Implementing bulk operations with detailed success/failure reporting
- Designing event-driven architectures with RabbitMQ integration
- Multi-tenant data isolation with schema-per-tenant patterns
- Lead conversion tracking and CRM workflow integration
- Performance optimization with caching and bulk processing
- Comprehensive testing strategies for microservices

## Adapting for Your Domain

This contact management service demonstrates patterns applicable to various scenarios:

### Customer Relationship Management

- Customer contact databases
- Lead conversion tracking systems
- Sales pipeline management
- Customer lifecycle management

### Event-Driven Architectures

- Cross-service communication patterns
- Event sourcing for audit trails
- Asynchronous processing workflows
- Service integration patterns

### Bulk Processing Systems

- Batch data import/export operations
- Mass update operations with reporting
- Data migration and synchronization
- ETL pipeline integration

### Multi-Tenant Applications

- Schema-per-tenant isolation
- Tenant context management
- Cross-tenant data security
- Tenant-aware event publishing

The patterns demonstrated here apply to any domain requiring comprehensive contact management, event-driven integration, bulk operations, and multi-tenant data isolation.

## Integration with Other Services

### Lead Service Integration

The Contact Service integrates with the Lead Service for lead conversion workflows:

**Lead Conversion Flow:**

1. Lead Service calls `POST /api/v1/contacts` with lead data
2. Contact Service creates contact and publishes `contact.created` event
3. Lead Service consumes event and updates lead status to `CONVERTED`
4. Pipeline Service consumes event and moves opportunity to "Won" stage

**Event Consumption:**
Lead Service listens for contact events to maintain conversion tracking:

```java
@RabbitListener(queues = "lead.service.contact.events")
public void handleContactCreated(ContactCreatedEvent event) {
  if (event.getMetadata().getConvertedFromLeadId() != null) {
    leadService.markAsConverted(event.getMetadata().getConvertedFromLeadId(), event.getContactId(), event.getTimestamp());
  }
}
```

### Pipeline Service Integration

Pipeline Service consumes contact events for opportunity management:

```java
@RabbitListener(queues = "pipeline.service.contact.events")
public void handleContactCreated(ContactCreatedEvent event) {
  if (event.getMetadata().getConvertedFromLeadId() != null) {
    pipelineService.markOpportunityAsWon(event.getMetadata().getConvertedFromLeadId(), event.getContactId(), event.getTimestamp());
  }
}
```

### User Service Integration

Contact Service validates JWT tokens and extracts tenant context:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://iqscaffold-user-service:8080/api/v1/auth/.well-known/jwks.json
```

Extract user and tenant context from JWT:

```java
@GetMapping("/contacts")
public ResponseEntity<Page<ContactDto>> getContacts(Authentication auth, @RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
  // Tenant context automatically resolved
  // User context available from JWT claims
  return ResponseEntity.ok(contactService.findAll(pageable));
}
```

---

**Use this as a blueprint** for building contact management services and implementing event-driven CRM systems in your microservices architecture. The code demonstrates production-ready patterns for contact lifecycle management, bulk operations, and cross-service integration.
