package com.liferay.samples.fbo.segment.cache;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.segments.SegmentsEntryRetriever;
import com.liferay.segments.configuration.provider.SegmentsConfigurationProvider;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.context.Context;
import com.liferay.segments.provider.SegmentsEntryProviderRegistry;
import com.liferay.segments.simulator.SegmentsEntrySimulator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(service = SegmentsEntryRetriever.class)
public class CachedSegmentsEntryRetrieverImpl implements SegmentsEntryRetriever {

	@Override
	public long[] getSegmentsEntryIds(
		long groupId, long userId, Context context, long[] segmentEntryIds) {

		try {
			if (!_segmentsConfigurationProvider.isSegmentationEnabled(
					_getCompanyId(groupId))) {

				return new long[] {SegmentsEntryConstants.ID_DEFAULT};
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException);
		}

		return ArrayUtil.toLongArray(
			SetUtil.fromArray(
				ArrayUtil.append(
					_getSegmentEntryIds(
						groupId, userId, context, segmentEntryIds),
					SegmentsEntryConstants.ID_DEFAULT)));
	}

	private long _getCompanyId(long groupId) throws PortalException {
		if (groupId == 0) {
			return _portal.getDefaultCompanyId();
		}

		Group group = _groupLocalService.getGroup(groupId);

		return group.getCompanyId();
	}

	private long[] _getSegmentEntryIds(
		long groupId, long userId, Context context, long[] segmentEntryIds) {
		
		if ((_segmentsEntrySimulator != null) &&
			_segmentsEntrySimulator.isSimulationActive(userId)) {

			return _segmentsEntrySimulator.getSimulatedSegmentsEntryIds(userId);
		}

		try {
			
			long[] cachedSegmentsEntryIds = SegmentCacheThreadLocal.getCachedSegmentsEntryIds(
					groupId, User.class.getName(), userId, context, segmentEntryIds);
			if(Validator.isNotNull(cachedSegmentsEntryIds)) {
				return cachedSegmentsEntryIds;
			}
			
			long[] calculatedSegmentsEntryIds = _segmentsEntryProviderRegistry.getSegmentsEntryIds(
					groupId, User.class.getName(), userId, context,
					segmentEntryIds);
				
			SegmentCacheThreadLocal.setCachedSegmentsEntryIds(
					groupId, User.class.getName(), userId, context, segmentEntryIds,
					calculatedSegmentsEntryIds);
			
			return calculatedSegmentsEntryIds;
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}

			return new long[0];
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
			CachedSegmentsEntryRetrieverImpl.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private SegmentsConfigurationProvider _segmentsConfigurationProvider;

	@Reference
	private SegmentsEntryProviderRegistry _segmentsEntryProviderRegistry;

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(model.class.name=com.liferay.portal.kernel.model.User)"
	)
	private volatile SegmentsEntrySimulator _segmentsEntrySimulator;

}