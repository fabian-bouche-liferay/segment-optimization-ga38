package com.liferay.samples.fbo.segment.cache;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.segments.context.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SegmentCacheThreadLocal {

	public static long[] getCachedSegmentsEntryIds(long groupId, String className, long userId, Context context, long[] segmentEntryIds) {
		
		if(Validator.isNull(_segmentsArray.get())) {
			return null;
		}
		
		String key = _computeKey(groupId, className, userId, context, segmentEntryIds);
		
		return _segmentsArray.get().get(key);
	}
	
	public static void setCachedSegmentsEntryIds(long groupId, String className, long userId, Context context, long[] segmentEntryIds, long[] cachedSegmentsEntryIds) {

		if(Validator.isNull(_segmentsArray.get())) {
			Map<String, long[]> map = new ConcurrentHashMap<>();
			_segmentsArray.set(map);
		}

		String key = _computeKey(groupId, className, userId, context, segmentEntryIds);

		_segmentsArray.get().put(key, cachedSegmentsEntryIds);
	}
	
	private static String _computeKey(long groupId, String className, long userId, Context context,
			long[] segmentEntryIds) {
		StringBundler sb = new StringBundler();
		sb.append(groupId).append(StringPool.AT)
			.append(className).append(StringPool.AT)
			.append(userId).append(StringPool.AT)
			.append(RandomKeyUtil.generateUniqueKey(context)).append(StringPool.AT)
			.append(RandomKeyUtil.generateUniqueKey(segmentEntryIds));
		return sb.toString();
	}
	
	private static final ThreadLocal<Map<String, long[]>> _segmentsArray =
			new CentralizedThreadLocal<>(
					SegmentCacheThreadLocal.class + "._segmentsArray");
	
}
