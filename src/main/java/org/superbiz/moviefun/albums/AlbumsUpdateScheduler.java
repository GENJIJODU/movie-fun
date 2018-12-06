package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater) {
        this.albumsUpdater = albumsUpdater;
    }


    @Scheduled(initialDelay = 5 * SECONDS, fixedRate = 5 * SECONDS)
    public void run() {
        if (albumsUpdater.acquireLock()) {
            try {
                logger.debug("Starting albums update");
                albumsUpdater.update();
                logger.debug("Finished albums update");
            } catch (Throwable e) {
                logger.error("Error while updating albums", e);
            }
        } else {
            logger.debug("Another instance is handling the seed process.");
        }
    }
}
