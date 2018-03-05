package com.yuntao.platform.common.cache;

import redis.clients.jedis.ShardedJedis;

/**
 * Created by shan on 2016/8/19.
 */
public interface JedisService {

    void execute(String key,JedisExecuteHandler jedisExecuteHandler);

    String getKey(String key);

    ShardedJedis getShardedJedis();

    void returnResource(ShardedJedis jedis);
}
