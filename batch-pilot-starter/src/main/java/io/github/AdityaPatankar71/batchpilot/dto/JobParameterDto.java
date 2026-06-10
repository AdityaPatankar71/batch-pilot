package io.github.AdityaPatankar71.batchpilot.dto;

/**
 * A single Spring Batch job parameter, flattened for transport.
 *
 * @param name        parameter key
 * @param value       string form of the parameter value
 * @param type        fully-qualified type of the parameter value
 * @param identifying whether the parameter contributes to the job instance identity
 */
public record JobParameterDto(String name, String value, String type, boolean identifying) {
}
