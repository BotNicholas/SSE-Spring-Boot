package org.botnicholas.projects.serversentevents.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.botnicholas.projects.serversentevents.config.properties.SseProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
@Slf4j
public class SSEController {
    private final SseProperties sseProperties;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getStream() {
        var emitter = new SseEmitter(sseProperties.getTimeout());

        executor.execute(() -> {
            try {
                for (int i=0; i<5; i++) {
                    Thread.sleep(1000);
                    var message = "STANDARD SSE EVENT - " + System.currentTimeMillis();
                    log.info("Sending message '{}'", message);
                    emitter.send(message);

                    log.info("Sending Custom event");
                    emitter.send(SseEmitter.event().name("custom").data(message));
                }

                log.info("Sending Custom closing event");
                emitter.send(SseEmitter.event().name("done").data("Done"));

                log.info("Closing the connection");
                emitter.complete();
            } catch (InterruptedException | IOException e) {
                log.error(e.getMessage());

                log.info("Sending Custom error event");
                try {
                    emitter.send(SseEmitter.event().name("failed").data("Failed"));
                } catch (IOException ex) {
                    log.error("Sending Custom error event failed");
                } finally {
                    emitter.completeWithError(e);
                }
            }
        });

        return ResponseEntity.ok(emitter);
    }

    @GetMapping(value = "/failing-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> getFailingStream() {
        var emitter = new SseEmitter(sseProperties.getTimeout());

        executor.execute(() -> {
            try {
                for (int i=0; i<5; i++) {
                    if (i == 2) {
                        throw new InterruptedException();
                    }

                    Thread.sleep(1000);
                    var message = "STANDARD SSE EVENT - " + System.currentTimeMillis();
                    log.info("Sending message '{}'", message);
                    emitter.send(message);

                    log.info("Sending Custom event");
                    emitter.send(SseEmitter.event().name("custom").data(message));
                }

                log.info("Sending Custom closing event");
                emitter.send(SseEmitter.event().name("done").data("Done"));

                log.info("Closing the connection");
                emitter.complete();
            } catch (InterruptedException | IOException e) {
                log.error(e.getMessage());

                log.info("Sending Custom error event");
                try {
                    emitter.send(SseEmitter.event().name("failed").data("Failed"));
                } catch (IOException ex) {
                    log.error("Sending Custom error event failed");
                } finally {
                    emitter.completeWithError(e);
                }
            }
        });

        return ResponseEntity.ok(emitter);
    }
}
