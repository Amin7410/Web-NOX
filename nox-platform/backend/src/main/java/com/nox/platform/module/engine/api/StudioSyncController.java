package com.nox.platform.module.engine.api;

import com.nox.platform.module.engine.api.request.CursorPayload;
import com.nox.platform.module.iam.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class StudioSyncController {

    // Nơi Client bắn cục data vào: /app/workspace/{id}/cursor
    // Nơi hệ thống phát loa ra cho all Client khác: /topic/workspace/{id}/cursor
    @MessageMapping("/workspace/{workspaceId}/cursor")
    @SendTo("/topic/workspace/{workspaceId}/cursor")
    public CursorPayload syncCursor(
            @DestinationVariable UUID workspaceId, 
            @Payload CursorPayload payload, 
            Authentication authentication) {
        
        // Gắn danh tính người tung chuột vào để phát cho mọi người biết
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        payload.setUserId(user.getId());
        payload.setUserName(user.getUsername());
        
        return payload;
    }
}
