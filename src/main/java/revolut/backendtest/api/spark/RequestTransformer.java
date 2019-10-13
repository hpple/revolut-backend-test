package revolut.backendtest.api.spark;

import spark.Request;

@FunctionalInterface
public interface RequestTransformer {

  <T> T transform(Request request, Class<T> targetClass);
}