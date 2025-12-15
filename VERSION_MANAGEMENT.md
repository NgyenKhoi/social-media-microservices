# Version Management Strategy

## Overview
This project uses centralized version management through a parent POM to ensure compatibility across all microservices.

## Version Compatibility Matrix

| Component | Version | Compatibility |
|-----------|---------|---------------|
| **Spring Boot** | `3.5.8` | ✅ Latest stable |
| **Spring Cloud** | `2025.0.0` | ✅ Compatible with Boot 3.5.x |
| **Spring Authorization Server** | `1.3.0` | ✅ Compatible with Boot 3.5.x |
| **Java** | `21` | ✅ LTS version |

## Critical Compatibility Rules

### ❌ **INCOMPATIBLE COMBINATIONS**
- Spring Boot `3.5.8` + Spring Cloud `2024.0.0` → **CompatibilityNotMetException**
- Spring Boot `3.4.x` + Spring Cloud `2025.0.0` → Version mismatch

### ✅ **COMPATIBLE COMBINATIONS**  
- Spring Boot `3.5.8` + Spring Cloud `2025.0.0` → **CURRENT SETUP**
- Spring Boot `3.4.x` + Spring Cloud `2024.0.0` → Alternative setup

## Centralized Version Management

### Parent POM Structure
```xml
<properties>
    <spring-boot.version>3.5.8</spring-boot.version>
    <spring-cloud.version>2025.0.0</spring-cloud.version>
    <spring-authorization-server.version>1.3.0</spring-authorization-server.version>
    <jjwt.version>0.13.0</jjwt.version>
    <nimbus-jose-jwt.version>9.37.3</nimbus-jose-jwt.version>
</properties>
```

### Service POM Structure
```xml
<parent>
    <groupId>com.nguyenkhoi</groupId>
    <artifactId>social-media-microservices</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

## Version Update Process

### 1. **Before Any Version Update**
```bash
# Check current compatibility
mvn dependency:tree | grep -E "spring-boot|spring-cloud"

# Verify no conflicts
mvn clean compile
```

### 2. **Update Process**
1. **Update parent POM only** - Never update individual service POMs
2. **Test compatibility** - Run full build after changes
3. **Update all services together** - Maintain consistency

### 3. **Validation Commands**
```bash
# Full build test
mvn clean compile

# Dependency analysis
mvn dependency:analyze

# Version conflicts check
mvn dependency:tree
```

## Service Dependencies

### Discovery Server
- Spring Boot Starter Web
- Spring Cloud Eureka Server
- Spring Boot Actuator

### Config Server  
- Spring Boot Starter Web
- Spring Cloud Config Server
- Spring Boot Security
- Spring Cloud Eureka Client

### Auth Service
- Spring Boot Starter Web
- Spring Boot Starter JPA
- Spring Boot Security
- Spring Authorization Server `1.3.0`
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Nimbus JOSE JWT

### API Gateway
- Spring Boot Starter WebFlux
- Spring Cloud Gateway
- Spring Boot Security
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- JJWT `0.13.0`

## Future Version Updates

### When to Update Spring Boot
- **Major versions**: Only when Spring Cloud compatibility is confirmed
- **Minor versions**: Safe to update within same major version
- **Patch versions**: Generally safe, but test thoroughly

### When to Update Spring Cloud
- **Release trains**: Follow Spring Cloud release train compatibility matrix
- **Always verify**: Spring Boot version compatibility before updating

### Update Checklist
- [ ] Check Spring Cloud compatibility matrix
- [ ] Update parent POM versions
- [ ] Run `mvn clean compile` 
- [ ] Test all services startup
- [ ] Verify service registration with Eureka
- [ ] Test JWT validation functionality
- [ ] Update this documentation

## Troubleshooting

### CompatibilityNotMetException
```
Caused by: org.springframework.cloud.commons.util.CompatibilityNotMetException
```
**Solution**: Verify Spring Boot and Spring Cloud version compatibility

### Version Conflicts
```bash
# Find conflicting versions
mvn dependency:tree | grep -E "spring-boot|spring-cloud"

# Resolve conflicts in parent POM dependencyManagement
```

### Build Failures After Version Update
1. Clean all targets: `mvn clean`
2. Refresh dependencies: `mvn dependency:resolve`
3. Recompile: `mvn compile`

## References
- [Spring Cloud Release Train](https://spring.io/projects/spring-cloud)
- [Spring Boot Version Compatibility](https://spring.io/projects/spring-boot)
- [Spring Authorization Server Compatibility](https://spring.io/projects/spring-authorization-server)