package net.pingfang.core.device;

import java.io.Serializable;
import net.pingfang.core.message.codec.Transport;

public interface AuthenticationRequest extends Serializable {
	Transport getTransport();
}
