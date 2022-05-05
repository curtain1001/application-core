package net.pingfang.core.metadata;

import com.alibaba.fastjson.JSONObject;
import com.jiangsonglin.fastbean.beans.FastBeanUtils;
import com.jiangsonglin.fastbean.copier.FastBeanCopier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Jsonable {
	final FastBeanCopier copier = FastBeanUtils.create(JSONObject.class, JSONObject.class);

	default JSONObject toJson() {
		JSONObject target = new JSONObject();
		copier.copy(this, target);
		return target;
	}

	default void fromJson(JSONObject json) {
		copier.copy(json, this);
	}
}
