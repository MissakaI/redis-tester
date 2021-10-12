package com.missakai.redistester.config;

import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.List;
import java.util.Objects;

/**
 * A Connection Factory for Redis using Lettuce and not Jedis. The reason for choosing Lettuce
 * is that Jedis currently has no support for Master/Slave mode of Redis. Jedis only supports
 * Redis Cluster Mode and Redis Sentinel Mode. While these work in most cases AWS follows a
 * master slave model. Refer the Spring Redis document <strong>Spring Data Redis: Redis
 * Connections</strong> for differences.
 *
 * @author Missaka Iddamalgoda (@MissakaI)
 */
@Configuration
@Slf4j
public class RedisWriteToMasterReadFromReplicaConfiguration {

    @Value("${spring.redis.mode}")
    private RedisMode redisMode;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(LettuceClientConfiguration clientConfiguration, RedisProperties redisProperties){
        switch (redisMode){
            case MASTER_REPLICA: return redisStaticMasterReplicaConnectionFactory(clientConfiguration,redisProperties);
            case CLUSTER: return redisClusterConnectionFactory(clientConfiguration,redisProperties);
            default: return redisStandaloneConnectionFactory(clientConfiguration,redisProperties);
        }
    }

    /**
     * This creates a LettuceConnectionFactory with a <strong>Static Master Replica Configuration</strong>. This is recommended for a redis model
     * like AWS ElasticCache for Redis. Because AWS has its way of promoting any node as master without a change in the url. This
     * url is provided as <strong>Primary Endpoint and Reader Endpoint</strong> in the Redis Cluster Description.
     *
     * @param redisProperties Spring Boot's native Redis Properties from <code>spring.redis</code> from the application Property Source.
     *                        The Configuration for <strong>Master</strong> is taken from <code>spring.redis.host</code> and <code>spring.redis.port</code> properties
     *                        The <strong>Readers or Slaves</strong> are taken from <code>spring.redis.cluster.nodes</code>
     *
     * @see <a href="https://docs.spring.io/spring-data/redis/docs/2.5.5/reference/html/#redis:connectors:connection">Sprind Data Redis: Redis Connections</a>
     * */
    public LettuceConnectionFactory redisStaticMasterReplicaConnectionFactory(LettuceClientConfiguration clientConfig, RedisProperties redisProperties) {

        RedisStaticMasterReplicaConfiguration masterReplicaConfiguration = new RedisStaticMasterReplicaConfiguration(redisProperties.getHost(),redisProperties.getPort());
        addNodesToRedisConfiguration(masterReplicaConfiguration,redisProperties.getCluster().getNodes());
        setCredentials(masterReplicaConfiguration,redisProperties);

        return new LettuceConnectionFactory(masterReplicaConfiguration, clientConfig);
    }

    /**
     * This creates a LettuceConnectionFactory with a <strong>Cluster Configuration</strong>. This is recommended for Redis Cluster model.
     * In AWS the node urls for this kind of configuration is available in the Redis Cluster's Nodes Tab.
     *
     * @param redisProperties Spring Boot's native Redis Properties from <code>spring.redis</code> from the application Property Source
     *                        The configuration for <strong>Nodes</strong> are taken from <code>spring.redis.cluster.nodes</code>
     *
     * @see <a href="https://docs.spring.io/spring-data/redis/docs/2.5.5/reference/html/#redis:connectors:connection">Sprind Data Redis: Redis Connections</a>
     * */
    public LettuceConnectionFactory redisClusterConnectionFactory(LettuceClientConfiguration clientConfig, RedisProperties redisProperties) {

        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
        setCredentials(clusterConfiguration,redisProperties);

        return new LettuceConnectionFactory(clusterConfiguration, clientConfig);
    }

    /**
     * This creates a LettuceConnectionFactory with a <strong>Standalone Configuration</strong>. This is recommended for Standard Redis model.
     * This is recommended for a Redis deployment in a development environment.
     *
     * @param redisProperties Spring Boot's native Redis Properties from <code>spring.redis</code> from the application Property Source
     *                        The Configuration is taken from <code>spring.redis.host</code> and <code>spring.redis.port</code> properties
     *
     * @see <a href="https://docs.spring.io/spring-data/redis/docs/2.5.5/reference/html/#redis:connectors:connection">Sprind Data Redis: Redis Connections</a>
     * */
    public LettuceConnectionFactory redisStandaloneConnectionFactory(LettuceClientConfiguration clientConfig, RedisProperties redisProperties) {

        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(redisProperties.getHost(),redisProperties.getPort());
        setCredentials(standaloneConfiguration,redisProperties);

        return new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
    }


    private void addNodesToRedisConfiguration(RedisStaticMasterReplicaConfiguration masterReplicaConfiguration,List<String> nodes){
        nodes.stream()
                .map(s -> s.split(":")) //split the host name into host and port
                .forEach(node -> {
                    try {
                        masterReplicaConfiguration.addNode(node[0],Integer.parseInt(node[1]));
                    }catch (ArrayIndexOutOfBoundsException e){
                        masterReplicaConfiguration.addNode(node[0],6379);
                    }
                });
    }

    @Bean
    public LettuceClientConfiguration redisClientConfiguration(RedisProperties redisProperties){
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED);

        if (redisProperties.isSsl())
            clientConfigBuilder.useSsl();

        if (Objects.nonNull(redisProperties.getClientName()) && !redisProperties.getClientName().isBlank())
            clientConfigBuilder.clientName(redisProperties.getClientName());

        if (Objects.nonNull(redisProperties.getLettuce()))
            clientConfigBuilder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());

        return clientConfigBuilder.build();
    }

    private void setCredentials(RedisConfiguration.WithAuthentication configuration, RedisProperties redisProperties){
        if (!Objects.requireNonNullElse(redisProperties.getUsername(),"").isBlank()) {
            log.info("Setting Redis username");
            configuration.setUsername(redisProperties.getUsername());
        }

        if (!Objects.requireNonNullElse(redisProperties.getPassword(),"").isBlank()) {
            log.info("Setting Redis password");
            configuration.setPassword(redisProperties.getPassword());
        }
    }

}
