{{- define "loadDBConnectionDetails" -}}
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    password: {{ .component.datasourcePassword }}
    username: {{ .component.datasourceUsername }}
    url: {{ printf "jdbc:mysql://%s:%v/arrowhead?serverTimezone=Europe/Budapest" .dot.Values.mysql.address .dot.Values.mysql.port | indent 2 }}
{{- end -}}

{{- define "loadCertificateDetails" -}}
server:
  ssl:
    key-store-password: {{ .component.keystorePassword }}
    trust-store-type: {{ .component.truststoreType }}
    key-store: {{ printf "file:%s/keystore/keystore.p12" .mountpath }}
    key-password: {{ .component.keyPassword }}
    trust-store: {{ printf "file:%s/truststore/truststore.p12" .mountpath }}
    key-store-type: {{ .component.keystoreType }}
    key-alias: {{ .component.keyAlias }}
    client-auth: need
    enabled: {{ .component.sslEnabled }}
    trust-store-password: {{ .component.truststorePassword }}
{{- end -}}

{{- define "loadSRDetails" -}}
sr_address: {{ .Values.serviceRegistry.address }}
sr_port: {{ .Values.serviceRegistry.port }}
{{- end -}}

{{- define "flattenYaml" -}}
{{- $map := first . -}}
{{- $concatenatedKeys := last . -}}
{{- range $key, $value := $map -}}
  {{ $addKey := "" }}
  {{- if not (eq $concatenatedKeys "") -}}
  {{ $addKey = printf "%s.%s" $concatenatedKeys $key}}
  {{- else -}}
  {{- $addKey = $key -}}
  {{- end -}}
  {{- if kindOf $value | eq "map" -}}
  {{- include "flattenYaml" (list $value $addKey) -}}
  {{- else -}}
{{ printf "%s=%v" $addKey $value }}
{{ printf ""}}
  {{-  end -}}
{{- end -}}
{{- end -}}