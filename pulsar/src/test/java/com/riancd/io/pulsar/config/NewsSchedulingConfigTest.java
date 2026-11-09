package com.riancd.io.pulsar.config;

import com.riancd.io.pulsar.service.NewsService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.FixedRateTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NewsSchedulingConfigTest {
    private final NewsService service = mock(NewsService.class);
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(NewsSchedulingConfig.class)
            .withBean(NewsService.class, () -> service);

    @Test
    void doesNotRegisterSchedulingUnlessExplicitlyEnabled() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(NewsSchedulingConfig.class);
            assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            verifyNoInteractions(service);
        });
    }

    @Test
    void disabledCollectionDoesNotNeedValidScheduleSettings() {
        runner.withPropertyValues("pulsar.news.collection.enabled=false",
                "pulsar.news.collection.interval=invalid").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            verifyNoInteractions(service);
        });
    }

    @Test
    void enabledCollectionUsesConfiguredIntervalAndInitialDelay() {
        runner.withPropertyValues("pulsar.news.collection.enabled=true",
                "pulsar.news.collection.interval=PT15M",
                "pulsar.news.collection.initial-delay=PT24H").run(context -> {
            assertThat(context).hasNotFailed();
            var processor = context.getBean(ScheduledAnnotationBeanPostProcessor.class);
            assertThat(processor.getScheduledTasks()).hasSize(1);
            var task = (FixedRateTask) processor.getScheduledTasks().iterator().next().getTask();
            assertThat(task.getIntervalDuration()).isEqualTo(Duration.ofMinutes(15));
            assertThat(task.getInitialDelayDuration()).isEqualTo(Duration.ofHours(24));
            verifyNoInteractions(service);
            context.getBean(NewsSchedulingConfig.class).collectNews();
            verify(service).fetchAndProcessNews();
        });
    }
}
