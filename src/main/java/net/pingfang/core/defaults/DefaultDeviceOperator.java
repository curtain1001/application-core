package net.pingfang.core.defaults;

import static org.jetlinks.core.device.DeviceConfigKey.*;
import static org.jetlinks.core.device.DeviceConfigKey.productId;

import com.alibaba.fastjson.JSONObject;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.i18n.LocaleUtils;
import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.ProtocolSupports;
import org.jetlinks.core.Value;
import org.jetlinks.core.Values;
import org.jetlinks.core.config.ConfigKey;
import org.jetlinks.core.config.ConfigStorage;
import org.jetlinks.core.config.ConfigStorageManager;
import org.jetlinks.core.config.StorageConfigurable;
import org.jetlinks.core.device.*;
import org.jetlinks.core.enums.ErrorCode;
import org.jetlinks.core.exception.DeviceOperationException;
import org.jetlinks.core.exception.ProductNotActivatedException;
import org.jetlinks.core.message.*;
import org.jetlinks.core.message.interceptor.DeviceMessageSenderInterceptor;
import org.jetlinks.core.message.state.DeviceStateCheckMessage;
import org.jetlinks.core.message.state.DeviceStateCheckMessageReply;
import org.jetlinks.core.metadata.DeviceMetadata;
import org.jetlinks.core.things.ThingMetadata;
import org.jetlinks.core.things.ThingRpcSupport;
import org.jetlinks.core.things.ThingRpcSupportChain;
import org.jetlinks.core.utils.IdUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Slf4j
public class DefaultDeviceOperator implements DeviceOperator, StorageConfigurable {
    public static final DeviceStateChecker DEFAULT_STATE_CHECKER = device -> checkState0(((DefaultDeviceOperator) device));

    private static final ConfigKey<Long> lastMetadataTimeKey = ConfigKey.of("lst_metadata_time");

    static final List<String> productIdAndVersionKey = Arrays.asList(productId.getKey(), productVersion.getKey());

    private static final AtomicReferenceFieldUpdater<DefaultDeviceOperator, DeviceMetadata> METADATA_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultDeviceOperator.class, DeviceMetadata.class, "metadataCache");
    private static final AtomicLongFieldUpdater<DefaultDeviceOperator> METADATA_TIME_UPDATER =
            AtomicLongFieldUpdater.newUpdater(DefaultDeviceOperator.class, "lastMetadataTime");

    @Getter
    private final String id;

    private final DeviceOperationBroker handler;

    private final DeviceRegistry registry;

    private final DeviceMessageSender messageSender;

    private final Mono<ConfigStorage> storageMono;

    private final Mono<ProtocolSupport> protocolSupportMono;

    private final Mono<DeviceMetadata> metadataMono;

    private final DeviceStateChecker stateChecker;

    private final Mono<DeviceProductOperator> parent;

    private volatile long lastMetadataTime = -1;

    private volatile DeviceMetadata metadataCache;

    @Setter
    private ThingRpcSupportChain rpcChain;


    public DefaultDeviceOperator(String id,
                                 ProtocolSupports supports,
                                 ConfigStorageManager storageManager,
                                 DeviceOperationBroker handler,
                                 DeviceRegistry registry) {
        this(id, supports, storageManager, handler, registry, DeviceMessageSenderInterceptor.DO_NOTING);

    }

    public DefaultDeviceOperator(String id,
                                 ProtocolSupports supports,
                                 ConfigStorageManager storageManager,
                                 DeviceOperationBroker handler,
                                 DeviceRegistry registry,
                                 DeviceMessageSenderInterceptor interceptor) {
        this(id, supports, storageManager, handler, registry, interceptor, DEFAULT_STATE_CHECKER);
    }

    public DefaultDeviceOperator(String id,
                                 ProtocolSupports supports,
                                 ConfigStorageManager storageManager,
                                 DeviceOperationBroker handler,
                                 DeviceRegistry registry,
                                 DeviceMessageSenderInterceptor interceptor,
                                 DeviceStateChecker deviceStateChecker) {
        this.id = id;
        this.registry = registry;
        this.handler = handler;
        this.messageSender = new DefaultDeviceMessageSender(handler, this, registry, interceptor);
        this.storageMono = storageManager.getStorage("device:" + id);
        this.parent = getReactiveStorage()
                .flatMap(store -> store.getConfigs(productIdAndVersionKey))
                .flatMap(productIdAndVersion -> {
                    //????????????????????????
                    String _productId = productIdAndVersion.getString(productId.getKey(), (String) null);
                    String _version = productIdAndVersion.getString(productVersion.getKey(), (String) null);
                    return registry.getProduct(_productId, _version);
                });
        //???????????????????????????
        this.protocolSupportMono = this
                .getSelfConfig(protocol)
                .flatMap(supports::getProtocol)
                .switchIfEmpty(this.parent.flatMap(DeviceProductOperator::getProtocol));

        this.stateChecker = deviceStateChecker;
        this.metadataMono = this
                //????????????????????????????????????
                .getSelfConfig(lastMetadataTimeKey)
                .flatMap(i -> {
                    //???????????????,????????????????????????????????????.
                    //??????????????????,??????????????????????????????.
                    if (i.equals(lastMetadataTime) && metadataCache != null) {
                        return Mono.just(metadataCache);
                    }
                    METADATA_TIME_UPDATER.set(this, i);
                    //????????????????????????
                    return Mono
                            .zip(getSelfConfig(metadata),
                                 protocolSupportMono)
                            .flatMap(tp2 -> tp2
                                    .getT2()
                                    .getMetadataCodec()
                                    .decode(tp2.getT1())
                                    .doOnNext(metadata -> METADATA_UPDATER.set(this, metadata)));

                })
                //??????????????????,???????????????????????????
                .switchIfEmpty(this.getParent()
                                   .switchIfEmpty(Mono.defer(this::onProductNonexistent))
                                   .flatMap(DeviceProductOperator::getMetadata)
                );
    }

    private Mono<DeviceProductOperator> onProductNonexistent() {
        return getReactiveStorage()
                .flatMap(store -> store.getConfig(productId.getKey()))
                .map(Value::asString)
                .flatMap(productId -> Mono.error(new ProductNotActivatedException(productId)));
    }

    @Override
    public Mono<ConfigStorage> getReactiveStorage() {
        return storageMono;
    }

    @Override
    public String getDeviceId() {
        return id;
    }

    @Override
    public Mono<String> getConnectionServerId() {
        return getSelfConfig(connectionServerId.getKey())
                .map(Value::asString);
    }

    @Override
    public Mono<String> getSessionId() {
        return getSelfConfig(sessionId.getKey())
                .map(Value::asString);
    }

    @Override
    public Mono<String> getAddress() {
        return getConfig("address")
                .map(Value::asString);
    }

    @Override
    public Mono<Void> setAddress(String address) {
        return setConfig("address", address)
                .then();
    }

    @Override
    public Mono<Boolean> putState(byte state) {
        return setConfig("state", state);
    }

    private final static List<String> stateCacheKeys = Arrays
            .asList("state",
                    parentGatewayId.getKey(),
                    selfManageState.getKey(),
                    connectionServerId.getKey());

    @Override
    public Mono<Byte> getState() {
        return this
                .getSelfConfigs(stateCacheKeys)
                .flatMap(values -> {
                    //??????????????????
                    Byte state = values
                            .getValue("state")
                            .map(val -> val.as(Byte.class))
                            .orElse(DeviceState.unknown);
                    //????????????????????????,?????????????????????????????????
                    boolean isSelfManageState = values
                            .getValue(selfManageState)
                            .orElse(false);

                    String server = values
                            .getValue(connectionServerId)
                            .orElse(null);

                    //????????????????????????,?????????????????????
                    if (StringUtils.hasText(server)) {
                        return Mono.just(state);
                    }
                    //??????ID
                    String parentGatewayId = values
                            .getValue(DeviceConfigKey.parentGatewayId)
                            .orElse(null);
                    //?????????????????????????????????
                    if (getDeviceId().equals(parentGatewayId)) {
                        log.warn(LocaleUtils.resolveMessage("validation.parent_id_and_id_can_not_be_same", parentGatewayId));
                        return Mono.just(state);
                    }
                    //????????????????????????????????????????????????????
                    if (isSelfManageState) {
                        return Mono.just(state);
                    }
                    //????????????????????????
                    if (StringUtils.hasText(parentGatewayId)) {
                        return registry
                                .getDevice(parentGatewayId)
                                .flatMap(DeviceOperator::getState);
                    }
                    return Mono.just(state);
                })
                .defaultIfEmpty(DeviceState.unknown);
    }

    private Mono<Byte> doCheckState() {
        return Mono
                .defer(() -> this
                        .getSelfConfigs(stateCacheKeys)
                        .flatMap(values -> {

                            //?????????????????????????????????
                            String server = values
                                    .getValue(connectionServerId)
                                    .orElse(null);

                            //?????????????????????
                            Byte state = values.getValue("state")
                                               .map(val -> val.as(Byte.class))
                                               .orElse(DeviceState.unknown);


                            //?????????????????????????????????????????????????????????????????????????????????
                            if (StringUtils.hasText(server)) {
                                return handler
                                        .getDeviceState(server, Collections.singletonList(id))
                                        .map(DeviceStateInfo::getState)
                                        .singleOrEmpty()
                                        .timeout(Duration.ofSeconds(1), Mono.just(state))
                                        .defaultIfEmpty(state);
                            }

                            //????????????ID
                            String parentGatewayId = values
                                    .getValue(DeviceConfigKey.parentGatewayId)
                                    .orElse(null);

                            if (getDeviceId().equals(parentGatewayId)) {
                                log.warn(LocaleUtils.resolveMessage("validation.parent_id_and_id_can_not_be_same", parentGatewayId));
                                return Mono.just(state);
                            }
                            boolean isSelfManageState = values.getValue(selfManageState).orElse(false);
                            //?????????????????????????????????????????????????????????
                            if (StringUtils.hasText(parentGatewayId)) {
                                return registry
                                        .getDevice(parentGatewayId)
                                        .flatMap(device -> {
                                            //???????????????????????????????????????????????????
                                            if (!isSelfManageState) {
                                                return device.checkState();
                                            }
                                            //?????????????????????????????????????????????
                                            return device
                                                    .messageSender()
                                                    .<ChildDeviceMessageReply>
                                                            send(ChildDeviceMessage
                                                                         .create(parentGatewayId,
                                                                                 DeviceStateCheckMessage.create(getDeviceId())
                                                                         )
                                                                         .addHeader(Headers.timeout, 5000L)
                                                    )
                                                    .singleOrEmpty()
                                                    .map(msg -> {
                                                        if (msg.getChildDeviceMessage() instanceof DeviceStateCheckMessageReply) {
                                                            return ((DeviceStateCheckMessageReply) msg.getChildDeviceMessage())
                                                                    .getState();
                                                        }
                                                        log.warn("State check return error {}", msg);
                                                        //??????????????????,??????????????????????????????,?????????????????????????????????
                                                        return DeviceState.online;
                                                    })
                                                    .onErrorResume(err -> {
                                                        if (err instanceof DeviceOperationException) {
                                                            ErrorCode code = ((DeviceOperationException) err).getCode();
                                                            if (code == ErrorCode.CLIENT_OFFLINE) {
                                                                //????????????????????????
                                                                return Mono.just(DeviceState.offline);
                                                            } else if (code == ErrorCode.UNSUPPORTED_MESSAGE) {
                                                                //???????????????????????????????????????????????????????????????????????????????????????
                                                                return Mono.just(DeviceState.online);
                                                            }
                                                        }
                                                        //??????????????????,??????????????????????????????,????????????????????????
                                                        return Mono.just(state);
                                                    });
                                        })
                                        //???????????????????????????????
                                        .defaultIfEmpty(state.equals(DeviceState.online) ? DeviceState.offline : state);
                            }

                            //?????????????????????,???????????????,????????????????????????
                            if (state.equals(DeviceState.online)) {
                                return Mono.just(DeviceState.offline);
                            } else {
                                return Mono.just(state);
                            }
                        }));
    }

    @Override
    public Mono<Byte> checkState() {
        return Mono
                .zip(
                        stateChecker
                                .checkState(this)
                                .switchIfEmpty(Mono.defer(() -> DEFAULT_STATE_CHECKER.checkState(this)))
                                .defaultIfEmpty(DeviceState.online),
                        this.getState()
                )
                .flatMap(tp2 -> {
                    byte newer = tp2.getT1();
                    byte old = tp2.getT2();
                    //?????????????????????????????????????????????.
                    if (newer != old) {
                        log.info("device[{}] state changed from {} to {}", this.getDeviceId(), old, newer);
                        Map<String, Object> configs = new HashMap<>();
                        configs.put("state", newer);
                        if (newer == DeviceState.online) {
                            configs.put("onlineTime", System.currentTimeMillis());
                        } else if (newer == DeviceState.offline) {
                            configs.put("offlineTime", System.currentTimeMillis());
                        }
                        return this
                                .setConfigs(configs)
                                .thenReturn(newer);
                    }
                    return Mono.just(newer);
                });
    }

    @Override
    public Mono<Long> getOnlineTime() {
        return this
                .getSelfConfig("onlineTime")
                .map(val -> val.as(Long.class))
                .switchIfEmpty(Mono.defer(() -> this
                        .getSelfConfig(parentGatewayId)
                        .flatMap(registry::getDevice)
                        .flatMap(DeviceOperator::getOnlineTime)));
    }

    @Override
    public Mono<Long> getOfflineTime() {
        return this
                .getSelfConfig("offlineTime")
                .map(val -> val.as(Long.class))
                .switchIfEmpty(Mono.defer(() -> this
                        .getSelfConfig(parentGatewayId)
                        .flatMap(registry::getDevice)
                        .flatMap(DeviceOperator::getOfflineTime)));
    }

    @Override
    public Mono<Boolean> offline() {
        return this
                .setConfigs(
                        //selfManageState.value(true),
                        connectionServerId.value(""),
                        sessionId.value(""),
                        ConfigKey.of("offlineTime").value(System.currentTimeMillis()),
                        ConfigKey.of("state").value(DeviceState.offline)
                )
                .doOnError(err -> log.error("offline device error", err));
    }

    @Override
    public Mono<Boolean> online(String serverId, String sessionId, String address) {
        return this
                .setConfigs(
                        //  selfManageState.value(true),
                        connectionServerId.value(serverId),
                        DeviceConfigKey.sessionId.value(sessionId),
                        ConfigKey.of("address").value(address),
                        ConfigKey.of("onlineTime").value(System.currentTimeMillis()),
                        ConfigKey.of("state").value(DeviceState.online)
                )
                .doOnError(err -> log.error("online device error", err));
    }

    @Override
    public Mono<Value> getSelfConfig(String key) {
        return getConfig(key, false);
    }

    @Override
    public Mono<Values> getSelfConfigs(Collection<String> keys) {
        return getConfigs(keys, false);
    }


    @Override
    public Mono<Boolean> disconnect() {
        DisconnectDeviceMessage disconnect = new DisconnectDeviceMessage();
        disconnect.setDeviceId(getDeviceId());
        disconnect.setMessageId(IdUtils.newUUID());
        return messageSender()
                .send(Mono.just(disconnect))
                .next()
                .map(DeviceMessageReply::isSuccess);
    }

    @Override
    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        return getProtocol()
                .flatMap(protocolSupport -> protocolSupport.authenticate(request, this));
    }

    @Override
    public Mono<DeviceMetadata> getMetadata() {
        return metadataMono;
    }


    @Override
    public Mono<DeviceProductOperator> getParent() {
        return parent;
    }

    @Override
    public Mono<ProtocolSupport> getProtocol() {
        return protocolSupportMono;
    }

    @Override
    public Mono<DeviceProductOperator> getProduct() {
        return getParent();
    }

    @Override
    public DeviceMessageSender messageSender() {
        return messageSender;
    }

    @Override
    public Mono<Boolean> updateMetadata(String metadata) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(DeviceConfigKey.metadata.getKey(), metadata);
        return setConfigs(configs);
    }

    @Override
    public Mono<Void> resetMetadata() {
        METADATA_UPDATER.set(this, null);
        METADATA_TIME_UPDATER.set(this, -1);
        return removeConfigs(metadata, lastMetadataTimeKey)
                .then(this.getProtocol()
                          .flatMap(support -> support.onDeviceMetadataChanged(this))
                );
    }

    @Override
    public Mono<Boolean> updateMetadata(ThingMetadata metadata) {
        if (metadata instanceof DeviceMetadata) {
            return getProtocol()
                    .flatMap(protocol -> protocol.getMetadataCodec().encode((DeviceMetadata) metadata))
                    .flatMap(this::updateMetadata);
        }
        // FIXME: 2021/11/3
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> setConfigs(Map<String, Object> conf) {
        Map<String, Object> configs = new HashMap<>(conf);
        if (conf.containsKey(metadata.getKey())) {
            configs.put(lastMetadataTimeKey.getKey(), lastMetadataTime = System.currentTimeMillis());

            return StorageConfigurable.super
                    .setConfigs(configs)
                    .doOnNext(suc -> {
                        this.metadataCache = null;
                    })
                    .then(this.getProtocol()
                              .flatMap(support -> support.onDeviceMetadataChanged(this))
                    )
                    .thenReturn(true);
        }
        return StorageConfigurable.super.setConfigs(configs);
    }

    private static Mono<Byte> checkState0(DefaultDeviceOperator operator) {
        return operator
                .getProtocol()
                .flatMap(ProtocolSupport::getStateChecker) //????????????????????????????????????
                .flatMap(deviceStateChecker -> deviceStateChecker.checkState(operator))
                .switchIfEmpty(operator.doCheckState()) //???????????????
                ;
    }

    @Override
    public ThingRpcSupport rpc() {
        ThingRpcSupport support = (msg) -> messageSender.send(convertToDeviceMessage(msg));
        if (rpcChain != null) {
            return msg -> rpcChain.call(msg, support);
        }
        return support;
    }

    private DeviceMessage convertToDeviceMessage(ThingMessage message) {
        if (message instanceof DeviceMessage) {
            return ((DeviceMessage) message);
        }
        //??????DeviceMessage??????DeviceMessage

        JSONObject msg = message.toJson();
        msg.remove("thingId");
        msg.remove("thingType");
        msg.put("deviceId", message.getThingId());
        return MessageType
                .convertMessage(msg)
                .filter(DeviceMessage.class::isInstance)
                .map(DeviceMessage.class::cast)
                .orElseThrow(() -> new UnsupportedOperationException("unsupported message type " + message.getMessageType()));
    }
}
