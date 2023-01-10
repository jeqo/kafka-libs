package kafka.consumer.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.metrics.JmxReporter;
import org.apache.kafka.common.metrics.KafkaMetricsContext;
import org.apache.kafka.common.metrics.MetricConfig;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsContext;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Utils;

public class ConsumerE2ELatencyInterceptor<K, V> implements ConsumerInterceptor<K, V> {

  Metrics metrics;

  @Override
  public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> consumerRecords) {
    for (var record : consumerRecords) {}
    return consumerRecords;
  }

  @Override
  public void close() {}

  @Override
  public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {}

  @Override
  public void configure(Map<String, ?> config) {
    final String clientId = (String) config.get(CommonClientConfigs.CLIENT_ID_CONFIG);
    final MetricConfig metricConfig = new MetricConfig()
      .samples((Integer) config.get(CommonClientConfigs.METRICS_NUM_SAMPLES_CONFIG))
      .recordLevel(
        Sensor.RecordingLevel.forName((String) config.get(CommonClientConfigs.METRICS_RECORDING_LEVEL_CONFIG))
      )
      .timeWindow((Long) config.get(CommonClientConfigs.METRICS_SAMPLE_WINDOW_MS_CONFIG), TimeUnit.MILLISECONDS);
    final List<MetricsReporter> reporters = getConfiguredInstances(
      config,
      CommonClientConfigs.METRIC_REPORTER_CLASSES_CONFIG,
      MetricsReporter.class,
      Collections.singletonMap(CommonClientConfigs.CLIENT_ID_CONFIG, clientId)
    );
    final JmxReporter jmxReporter = new JmxReporter();
    jmxReporter.configure(config);
    //    reporters.add(jmxReporter);
    final Map<String, ?> context = config
      .keySet()
      .stream()
      .filter(k -> k.startsWith(CommonClientConfigs.METRICS_CONTEXT_PREFIX))
      .collect(Collectors.toMap(k -> k, config::get));
    final MetricsContext metricsContext = new KafkaMetricsContext("consumer-e2e-latency", context);
    metrics = new Metrics(metricConfig, reporters, new SystemTime(), metricsContext);
  }

  public <T> List<T> getConfiguredInstances(
    Map<String, ?> config,
    String key,
    Class<T> t,
    Map<String, Object> configOverrides
  ) {
    return getConfiguredInstances(config, (List<String>) config.get(key), t, configOverrides);
  }

  public <T> List<T> getConfiguredInstances(
    Map<String, ?> originals,
    List<String> classNames,
    Class<T> t,
    Map<String, Object> configOverrides
  ) {
    List<T> objects = new ArrayList<>();
    if (classNames == null) {
      return objects;
    }
    Map<String, Object> configPairs = new HashMap<>();
    configPairs.putAll(originals);
    configPairs.putAll(configOverrides);
    for (Object klass : classNames) {
      Object o = getConfiguredInstance(klass, t, configPairs);
      objects.add(t.cast(o));
    }
    return objects;
  }

  private <T> T getConfiguredInstance(Object klass, Class<T> t, Map<String, Object> configPairs) {
    if (klass == null) {
      return null;
    }

    Object o;
    if (klass instanceof String) {
      try {
        o = Utils.newInstance((String) klass, t);
      } catch (ClassNotFoundException e) {
        throw new KafkaException("Class " + klass + " cannot be found", e);
      }
    } else if (klass instanceof Class<?>) {
      o = Utils.newInstance((Class<?>) klass);
    } else {
      throw new KafkaException(
        "Unexpected element of type " + klass.getClass().getName() + ", expected String or Class"
      );
    }
    if (!t.isInstance(o)) {
      throw new KafkaException(klass + " is not an instance of " + t.getName());
    }
    if (o instanceof Configurable) {
      ((Configurable) o).configure(configPairs);
    }

    return t.cast(o);
  }
}
