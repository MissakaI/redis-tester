# Redis-Tester

This is a project to check connectivity of Redis in different cloud environments.

The project's Docker Image consist of two applications to test redis.
1. Spring Data Redis Application
2. `redis-cli` Redis client with `--tls` support without the need for additional certificates.

The spring application shows configuration of a Connection Factory for Redis using Lettuce and not Jedis. The reason for choosing Lettuce
is that Jedis currently has no support for Master/Slave mode of Redis. Jedis only supports
Redis Cluster Mode and Redis Sentinel Mode. While these work in most cases AWS follows a
master slave model. Refer the Spring Redis document **[Spring Data Redis: Redis
Connections][1]** for differences.

The project currently supports connecting to 3 different modes of Redis Deployments.

|     Mode     | Description |
|--------------|-------------|
|Standalone    |Standard Redis deployment with no replicas/slave. Usually recommended for development environments. |
|Cluster       |Configuration mode suitable for Redis cluster environments. |
|Master_Replica|Configuration interface suitable for Redis master/slave environments with fixed hosts. This is recommended mode when using **AWS Elasticache for Redis** |

## Configuration

The following fields are common to all of the modes and you may leave them blank or not set the at all depending on your requirement.

```yaml
spring:
  redis:
    username: ${REDIS_USERNAME:default}
    password: ${REDIS_PASSWORD}
    ssl     : ${REDIS_SSL:true}
```

Based on your redis deployment you can set the `mode` in the properties file or in your config map.

|   Property Key    |            Values               |
|-------------------|---------------------------------|
|`spring.redis.mode`|standard, cluster, master_replica|

> This is not a native spring boot property. This is defined via the additional spring metadata file at `src/main/resources/META_INF/additional-spring-configuration-metadata.json` and enum definded in `com.missakai.redistester.config.RedisMode` which is then used by `com.missakai.redistester.config.RedisWriteToMasterReadFromReplicaConfiguration` when creating the Connection Factory Bean.

### Standard Mode

When using the standard mode the following fields must be set. The Connection will be made based on these config.

```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    mode: standard
```

### Cluster Mode

Cluster mode takes an array of nodes at the `spring.redis.cluster.nodes` field in format `hostname:port`

```yaml
spring:
  redis:
    cluster :
      nodes: ${REDIS_NODES}
    mode: cluster
```

### Master/Replica Mode

This creates a Redis Connection Factory with a **Static Master Replica Configuration**. This is recommended for a redis model
like AWS ElasticCache for Redis. Because AWS has its way of promoting any node as master without a change in the url. This 
url is provided as **Primary Endpoint and Reader Endpoint** in the Redis Cluster Description.

Spring Boot's native Redis Properties from `spring.redis` from the application Property Source.
The Configuration for **Master** is taken from `spring.redis.host` and `spring.redis.port` properties
The **Readers or Slaves** are taken from `spring.redis.cluster.nodes`

```yaml
spring:
  redis:
    host: ${REDIS_HOST:master.url}
    port    : ${REDIS_PORT:6379}
    cluster :
      nodes: ${REDIS_NODES:replica.url:6379}
    mode: master_replica
```

## Redis-CLI usage

To use the `redis-cli` access the interactive pod console using

    kubectl exec <pod_name> -it -- /bin/bash

[1]: https://docs.spring.io/spring-data/redis/docs/2.5.5/reference/html/#redis:connectors:connection