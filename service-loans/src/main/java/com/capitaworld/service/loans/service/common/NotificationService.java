package com.capitaworld.service.loans.service.common;

import java.util.Map;

import com.capitaworld.service.loans.model.teaser.primaryview.CommonRequest;
import com.capitaworld.service.loans.utils.CommonNotificationUtils.NotificationTemplate;

public interface NotificationService {

	public void sendViewNotification(String toUserId, Long fromUserId, Long fromUserTypeId, Long notificationId,Long applicationId,Long fpProductId, NotificationTemplate notificationTemplate,Long loginUserType,Object subject);

	public void createEmailNotificationForUBI(String[] toIds, Long fromId, Long fromUserTypeId, Long templateId,
			Long fromUserId, Map<String, Object> parameters, Long applicationId, Long fpProductId,
			NotificationTemplate notificationTemplate, String fpName);
	
	public CommonRequest extractArrayToCommonRequest(Object[] obj) throws Exception;
	
}
