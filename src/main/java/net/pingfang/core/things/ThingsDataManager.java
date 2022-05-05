package net.pingfang.core.things;

import reactor.core.publisher.Mono;

/**
 * 物实例数据管理器
 *
 * @author zhouhao
 * @since 1.1.9
 */
public interface ThingsDataManager {

	/**
	 * 获取基准时间前最新的属性
	 *
	 * @param thingId  物ID
	 * @param property 属性
	 * @param baseTime 基准时间
	 * @return 属性
	 */
	Mono<ThingProperty> getLastProperty(ThingType thingType, String thingId, String property, long baseTime);

	/**
	 * 获取第一次上报的属性
	 *
	 * @param thingId  物ID
	 * @param property 属性ID
	 * @return 属性
	 */
	Mono<ThingProperty> getFirstProperty(ThingType thingType, String thingId, String property);

	/**
	 * 获取最后一次属性变更时间
	 *
	 * @param thingId 物ID
	 * @return 时间戳
	 */
	Mono<Long> getLastPropertyTime(ThingType thingType, String thingId, long baseTime);

	/**
	 * 获取第一次上报数据的时间
	 *
	 * @param thingId 物ID
	 * @return 时间戳
	 */
	Mono<Long> getFirstPropertyTime(ThingType thingType, String thingId);

}
