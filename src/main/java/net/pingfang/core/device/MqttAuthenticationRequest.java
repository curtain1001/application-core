package net.pingfang.core.device;

import org.jetlinks.core.message.codec.Transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author wangchao
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MqttAuthenticationRequest implements org.jetlinks.core.device.AuthenticationRequest {
	private String clientId;

	private String username;

	private String password;

	private Transport transport;
}
