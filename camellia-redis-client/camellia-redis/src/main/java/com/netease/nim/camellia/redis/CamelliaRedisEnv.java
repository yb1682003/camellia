package com.netease.nim.camellia.redis;


import com.netease.nim.camellia.core.client.env.ProxyEnv;
import com.netease.nim.camellia.redis.intercept.RedisInterceptor;
import com.netease.nim.camellia.tools.executor.CamelliaThreadFactory;
import com.netease.nim.camellia.redis.conf.CamelliaRedisConstants;
import com.netease.nim.camellia.redis.jedis.JedisPoolFactory;
import com.netease.nim.camellia.redis.jediscluster.JedisClusterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * Created by caojiajun on 2019/7/23.
 */
public class CamelliaRedisEnv {

    //jedisPool工厂
    private JedisPoolFactory jedisPoolFactory = JedisPoolFactory.DEFAULT;
    //jedisCluster工厂
    private JedisClusterFactory jedisClusterFactory = JedisClusterFactory.DEFAULT;
    //内部并发任务线程池大小
    private int concurrentExecPoolSize = CamelliaRedisConstants.Misc.concurrentExecPoolSize;
    //内部并发线程池
    private ExecutorService concurrentExec;
    //pipeline池大小，可以复用pipeline对象
    private int pipelinePoolSize = CamelliaRedisConstants.Misc.pipelinePoolSize;
    //pipeline是否开启并发，sync的时候
    private boolean pipelineConcurrentEnable = CamelliaRedisConstants.Misc.pipelineConcurrentEnable;
    //redis cluster的pipeline可能触发MOVED/ASK转向，此时重试的次数
    private int pipelineMaxAttempts = CamelliaRedisConstants.Misc.pipelineMaxAttempts;

    private ProxyEnv proxyEnv = ProxyEnv.defaultProxyEnv();

    private List<RedisInterceptor> interceptorList = new ArrayList<>();

    private CamelliaRedisEnv() {
        initExec();
    }

    private CamelliaRedisEnv(JedisPoolFactory jedisPoolFactory, JedisClusterFactory jedisClusterFactory,
                             int concurrentExecPoolSize, int pipelinePoolSize, boolean pipelineConcurrentEnable,
                             int pipelineMaxAttempts, ProxyEnv proxyEnv, List<RedisInterceptor> interceptorList) {
        this.jedisPoolFactory = jedisPoolFactory;
        this.jedisClusterFactory = jedisClusterFactory;
        this.concurrentExecPoolSize = concurrentExecPoolSize;
        this.pipelinePoolSize = pipelinePoolSize;
        this.pipelineConcurrentEnable = pipelineConcurrentEnable;
        this.pipelineMaxAttempts = pipelineMaxAttempts;
        if (proxyEnv != null) {
            this.proxyEnv = proxyEnv;
        }
        this.interceptorList = interceptorList;
        initExec();
    }

    private void initExec() {
        this.concurrentExec = new ThreadPoolExecutor(concurrentExecPoolSize, concurrentExecPoolSize, 0, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new CamelliaThreadFactory(CamelliaRedisEnv.class), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static CamelliaRedisEnv defaultRedisEnv() {
        return new CamelliaRedisEnv();
    }

    public int getPipelinePoolSize() {
        return pipelinePoolSize;
    }

    public JedisPoolFactory getJedisPoolFactory() {
        return jedisPoolFactory;
    }

    public JedisClusterFactory getJedisClusterFactory() {
        return jedisClusterFactory;
    }

    public ExecutorService getConcurrentExec() {
        return concurrentExec;
    }

    public boolean isPipelineConcurrentEnable() {
        return pipelineConcurrentEnable;
    }

    public int getPipelineMaxAttempts() {
        return pipelineMaxAttempts;
    }

    public ProxyEnv getProxyEnv() {
        return proxyEnv;
    }

    public List<RedisInterceptor> getInterceptorList() {
        return interceptorList;
    }

    public static class Builder {
        private final CamelliaRedisEnv redisEnv;
        public Builder() {
            redisEnv = new CamelliaRedisEnv();
        }

        public Builder(CamelliaRedisEnv redisEnv) {
            this.redisEnv = new CamelliaRedisEnv(redisEnv.jedisPoolFactory, redisEnv.jedisClusterFactory,
                    redisEnv.concurrentExecPoolSize, redisEnv.pipelinePoolSize, redisEnv.pipelineConcurrentEnable,
                    redisEnv.pipelineMaxAttempts, redisEnv.proxyEnv, redisEnv.interceptorList);
            this.redisEnv.concurrentExec = redisEnv.concurrentExec;
        }

        public Builder jedisPoolFactory(JedisPoolFactory jedisPoolFactory) {
            redisEnv.jedisPoolFactory = jedisPoolFactory;
            return this;
        }

        public Builder jedisClusterFactory(JedisClusterFactory jedisClusterFactory) {
            redisEnv.jedisClusterFactory = jedisClusterFactory;
            return this;
        }

        public Builder pipelinePoolSize(int pipelinePoolSize) {
            if (pipelinePoolSize > 0) {
                redisEnv.pipelinePoolSize = pipelinePoolSize;
            }
            return this;
        }

        public Builder concurrentExecPoolSize(int concurrentExecPoolSize) {
            if (concurrentExecPoolSize > 0) {
                redisEnv.concurrentExecPoolSize = concurrentExecPoolSize;
            }
            return this;
        }

        public Builder pipelineConcurrentEnable(boolean pipelineConcurrentEnable) {
            redisEnv.pipelineConcurrentEnable = pipelineConcurrentEnable;
            return this;
        }

        public Builder pipelineMaxAttempts(int pipelineMaxAttempts) {
            redisEnv.pipelineMaxAttempts = pipelineMaxAttempts;
            return this;
        }

        public Builder proxyEnv(ProxyEnv proxyEnv) {
            if (proxyEnv != null) {
                redisEnv.proxyEnv = proxyEnv;
            }
            return this;
        }

        public Builder addInterceptor(RedisInterceptor interceptor) {
            redisEnv.interceptorList.add(interceptor);
            return this;
        }

        public CamelliaRedisEnv build() {
            return redisEnv;
        }
    }
}
