package revolut.backendtest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import revolut.backendtest.api.spark.JsonRequestTransformer;
import revolut.backendtest.api.spark.JsonResponseTransformer;
import revolut.backendtest.api.spark.RequestTransformer;
import revolut.backendtest.api.spark.SparkApi;
import spark.ResponseTransformer;

public class RestApiModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(ResponseTransformer.class).to(JsonResponseTransformer.class).asEagerSingleton();
    bind(RequestTransformer.class).to(JsonRequestTransformer.class).asEagerSingleton();

    bind(RestApi.class).to(SparkApi.class).asEagerSingleton();

    bind(RestApiService.class).asEagerSingleton();
    expose(RestApiService.class);
  }

  @Singleton
  @Provides
  ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new ParanamerModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
