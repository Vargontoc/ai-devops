package es.vargontoc.agents.devops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a message to a specific project's log topic.
     * Clients should subscribe to: /topic/project/{projectId}/logs
     * 
     * @param projectId The ID of the project.
     * @param payload   The log fragment or status message to send to the client.
     */
    public void sendProjectLog(String projectId, String payload) {
        String destination = "/topic/project/" + projectId + "/logs";
        if (log.isTraceEnabled()) {
            log.trace("Sending to {}: {}", destination, payload);
        }
        messagingTemplate.convertAndSend(destination, payload);
    }
    
    /**
     * Sends a generic agent status update.
     * Clients should subscribe to: /topic/project/{projectId}/status
     */
    public void sendProjectStatus(String projectId, String status) {
        String destination = "/topic/project/" + projectId + "/status";
        log.debug("Project {} agent status changed to: {}", projectId, status);
        messagingTemplate.convertAndSend(destination, status);
    }
}
