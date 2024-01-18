# Instructions

Experimental customization to reduce the cost of fragments with nested lfr-drop-zone.
It seems that the computations within SegmentsEntryRetriever are expensive and recompute the same list for each drop zone with the same input data on ga38 (to be tested with more recent updates).
Thus, the ThreadLocal caching attempt.

Add com.liferay.segments.SegmentsEntryRetriever to System Settings -> Module Container -> Component Blacklist after deploying the module.

Warning: not extensively tested. Use at your own risk and do your QA.
