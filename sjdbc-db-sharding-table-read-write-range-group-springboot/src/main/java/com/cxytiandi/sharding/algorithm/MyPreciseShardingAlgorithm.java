package com.cxytiandi.sharding.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import com.cxytiandi.sharding.config.ShardingRangeConfig;

/**
 * 自定义分片算法
 * 
 * @author yinjihuan
 *
 */
public class MyPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

	private static List<ShardingRangeConfig> configs = new ArrayList<>();
	
	static {
		ShardingRangeConfig config = new ShardingRangeConfig();
		config.setStart(0);
		config.setEnd(30);
		config.setDatasourceList(Arrays.asList("master0", "master1"));
		configs.add(config);

		ShardingRangeConfig config2 = new ShardingRangeConfig();
		config2.setStart(31);
		config2.setEnd(60);
		config2.setDatasourceList(Arrays.asList("masters0", "masters1"));
		configs.add(config2);
	}
	
	@Override
	public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
		Optional<ShardingRangeConfig> configOptional = configs.stream().filter(
				c -> shardingValue.getValue() >= c.getStart() && shardingValue.getValue() <= c.getEnd()).findFirst();
		if (configOptional.isPresent()) {
			ShardingRangeConfig rangeConfig = configOptional.get();
			for (String ds : rangeConfig.getDatasourceList()) {
				if (ds.endsWith(shardingValue.getValue() % 2 + "")) {
					System.out.println(ds);
					return ds;
				}
			}
		}
		throw new IllegalArgumentException("无法获取数据源");
	}

}
