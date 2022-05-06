package net.pingfang.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.jiangsonglin.fastbean.beans.FastBeanUtils;
import com.jiangsonglin.fastbean.copier.FastBeanCopier;

/**
 * @author 王超
 * @description TODO
 * @date 2022-05-06 0:13
 */
public class BeanCopierUtils {
	final FastBeanCopier copier = FastBeanUtils.create(JSONObject.class, JSONObject.class);

	public static JSONObject copy(JSONObject object) {
		JSONObject jsonObject = new JSONObject();
		copier.copy(object, jsonObject);
		return jsonObject;
	}
}
