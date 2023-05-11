//package wdm.payment.config;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.util.Map;
//
//@Configuration
//public class RedisConfig {
//
//    @Value("${payment.db.host}")
//    private String host;
//
//    @Value("${payment.db.port}")
//    private int port;
//
//    @Value("${payment.db.pw}")
//    private String pw;
//
//    @Value("${payment.db.db}")
//    private int db;
//
//    @Bean
//    public LettuceConnectionFactory redisStandAloneConnectionFactory() {
//        RedisStandaloneConfiguration conn = new RedisStandaloneConfiguration(host, port);
//        conn.setPassword(pw);
//        conn.setDatabase(db);
//        return new LettuceConnectionFactory(conn);
//    }
//
//    @Bean
//    public RedisTemplate<String, Map<String, Object>> redisTemplateStandAlone(@Qualifier("redisStandAloneConnectionFactory")LettuceConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Map<String, Object>> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//
//}
