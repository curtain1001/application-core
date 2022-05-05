package net.pingfang.core.device;

import java.util.Collection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 设备注册中心,用于统一管理设备以及产品的基本信息,缓存,进行设备指令下发等操作. 例如获取设备以及设备的配置缓存信息:
 *
 * <pre>
 *    registry
 *    .getDevice(deviceId)
 *    .flatMap(device->device.getSelfConfig("my-config"))
 *    .flatMap(conf-> doSomeThing(...))
 * </pre>
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface DeviceRegistry {

	/**
	 * 获取设备操作接口.如果设备未注册或者注册已失效(缓存丢失).则回返回{@link Mono#empty()},
	 * 可以通过{@link Mono#switchIfEmpty(Mono)}来处理设备不存在的情况.如:
	 *
	 * <pre>
	 *     registry
	 *     .getDevice(deviceId)
	 *     .switchIfEmpty(Mono.error(()->new DeviceNotFoundException(....)))
	 * </pre>
	 *
	 * @param deviceId 设备ID
	 * @return 设备操作接口
	 */
	Mono<org.jetlinks.core.device.DeviceOperator> getDevice(String deviceId);

	/**
	 * 批量检查设备真实状态,建议使用{@link org.jetlinks.core.device.DeviceOperator#checkState()}
	 *
	 * @param id ID
	 * @return 设备状态信息流
	 */
	default Flux<org.jetlinks.core.device.DeviceStateInfo> checkDeviceState(Flux<? extends Collection<String>> id) {
		return Flux.error(new UnsupportedOperationException());
	}

	/**
	 * 获取设备产品操作,请勿缓存返回值,注册中心已经实现本地缓存.
	 *
	 * @param productId 产品ID
	 * @return 产品操作接口
	 */
	Mono<org.jetlinks.core.device.DeviceProductOperator> getProduct(String productId);

	/**
	 * 获取指定版本的产品,在注册产品时,指定了产品版本{@link org.jetlinks.core.device.ProductInfo#getVersion()}的信息,可以通过此方法获取
	 *
	 * @param productId 产品ID
	 * @param version   版本号
	 * @return 对应版本的产品
	 * @since 1.1.9
	 */
	default Mono<org.jetlinks.core.device.DeviceProductOperator> getProduct(String productId, String version) {
		// 默认不支持版本
		return getProduct(productId);
	}

	/**
	 * 注册设备,并返回设备操作接口,请勿缓存返回值,注册中心已经实现本地缓存.
	 *
	 * @param deviceInfo 设备基础信息
	 * @return 设备操作接口
	 * @see DeviceRegistry#getDevice(String)
	 */
	Mono<org.jetlinks.core.device.DeviceOperator> register(org.jetlinks.core.device.DeviceInfo deviceInfo);

	/**
	 * 注册产品(型号)信息
	 *
	 * @param productInfo 产品(型号)信息
	 * @return 注册结果
	 */
	Mono<org.jetlinks.core.device.DeviceProductOperator> register(org.jetlinks.core.device.ProductInfo productInfo);

	/**
	 * 注销设备,注销后将无法通过{@link DeviceRegistry#getDevice(String)}获取到设备信息,
	 * 此操作将触发{@link org.jetlinks.core.ProtocolSupport#onDeviceUnRegister(org.jetlinks.core.device.DeviceOperator)}
	 *
	 * @param deviceId 设备ID
	 * @return void
	 */
	Mono<Void> unregisterDevice(String deviceId);

	/**
	 * 注销产品,注销后将无法通过{@link DeviceRegistry#getProduct(String)} 获取到产品信息 <br>
	 * 此操作只会注销未设置版本的产品. <br>
	 * 此操作将触发{@link org.jetlinks.core.ProtocolSupport#onProductUnRegister(org.jetlinks.core.device.DeviceProductOperator)}
	 *
	 * @param productId 产品ID
	 * @return void
	 */
	Mono<Void> unregisterProduct(String productId);

	/**
	 * 注销指定版本的产品,注销后将无法通过{@link DeviceRegistry#getProduct(String,String)} 获取到产品信息
	 * <br>
	 * 此操作将触发{@link org.jetlinks.core.ProtocolSupport#onProductUnRegister(org.jetlinks.core.device.DeviceProductOperator)}
	 *
	 * @param productId 产品ID
	 * @param version   版本号
	 * @return void
	 * @since 1.1.9
	 */
	default Mono<Void> unregisterProduct(String productId, String version) {
		// 默认不支持版本
		return unregisterProduct(productId);
	}

}
