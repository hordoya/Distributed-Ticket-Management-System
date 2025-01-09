package de.uniba.rz.entities.general;

public enum RequestMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    final String value;
    RequestMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
